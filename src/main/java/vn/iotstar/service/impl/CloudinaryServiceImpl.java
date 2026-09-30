package vn.iotstar.service.impl;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import java.util.Map;
import javax.imageio.ImageIO;
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    private final boolean configured;
    public CloudinaryServiceImpl(@Value("${cloudinary.cloud-name:}") String name,
            @Value("${cloudinary.api-key:}") String key,@Value("${cloudinary.api-secret:}") String secret) {
        configured=!name.isBlank() && !key.isBlank() && !secret.isBlank();
        cloudinary=new Cloudinary(Map.of("cloud_name",name,"api_key",key,"api_secret",secret,"secure",true));
    }
    public UploadResult upload(MultipartFile file) {
        if (file==null || file.isEmpty() || file.getSize()>10*1024*1024)
            throw new IllegalArgumentException("Ảnh phải có dung lượng từ 1 byte đến 10 MB.");
        if (!configured) throw new IllegalStateException("Chưa cấu hình tài khoản Cloudinary.");
        try {
            try (var stream=ImageIO.createImageInputStream(file.getInputStream())) {
                var readers=ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException("Chỉ hỗ trợ ảnh PNG, JPEG hoặc GIF hợp lệ.");
                var reader=readers.next();
                try {
                    reader.setInput(stream);
                    String format=reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                    if (!java.util.Set.of("png","jpeg","jpg","gif").contains(format)
                        || (long)reader.getWidth(0)*reader.getHeight(0)>40_000_000)
                        throw new IllegalArgumentException("Ảnh không hợp lệ hoặc kích thước vượt 40 triệu pixel.");
                } finally { reader.dispose(); }
            }
            Map<?,?> result=cloudinary.uploader().upload(file.getBytes(),Map.of("folder","secureportal/products","resource_type","image"));
            return new UploadResult((String)result.get("secure_url"),(String)result.get("public_id"));
        } catch (java.io.IOException ex) { throw new IllegalStateException("Upload Cloudinary thất bại. Vui lòng thử lại.",ex); }
    }
    public void delete(String id) {
        if (id==null || id.isBlank()) return;
        if (!configured) throw new IllegalStateException("Chưa cấu hình Cloudinary.");
        try { cloudinary.uploader().destroy(id,Map.of("resource_type","image")); }
        catch (java.io.IOException ex) { throw new IllegalStateException("Không xóa được ảnh Cloudinary.",ex); }
    }
}
