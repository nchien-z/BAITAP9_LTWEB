package vn.iotstar.service;
import org.springframework.web.multipart.MultipartFile;
public interface CloudinaryService {
    record UploadResult(String url,String publicId) {}
    UploadResult upload(MultipartFile file);
    void delete(String publicId);
}
