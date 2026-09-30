package vn.iotstar.service;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.*;
import vn.iotstar.security.CustomUserDetails;

@Service
@Transactional(readOnly=true)
public class ProductService {
    private final ProductRepository products;
    private final UserRepository users;
    private final ProductMapper mapper;
    private final CloudinaryService images;
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(ProductService.class);
    public ProductService(ProductRepository products,UserRepository users,ProductMapper mapper,CloudinaryService images) {
        this.products=products;this.users=users;this.mapper=mapper;this.images=images;
    }
    public Page<ProductDTO> search(String keyword,int page,int size,CustomUserDetails actor) {
        return products.search(keyword==null?"":keyword,actor.isAdmin()?null:actor.getId(),
            PageRequest.of(Math.max(0,page),Math.min(50,Math.max(1,size)),Sort.by("id").descending())).map(mapper::toDTO);
    }
    private Product owned(Long id,CustomUserDetails actor) {
        Product p=products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy sản phẩm"));
        if (!actor.isAdmin() && !p.getUser().getId().equals(actor.getId()))
            throw new AccessDeniedException("Bạn chỉ được sửa hoặc xóa sản phẩm của mình.");
        return p;
    }
    public ProductDTO find(Long id,CustomUserDetails actor) { return mapper.toDTO(owned(id,actor)); }
    public long countAll() { return products.count(); }
    public long countByUser(Long id) { return products.countByUserId(id); }
    private void cleanupAfterCompletion(String oldId,String newId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                String remove=status==STATUS_COMMITTED ? oldId : newId;
                if (remove!=null) try { images.delete(remove); }
                catch (RuntimeException ex) { log.warn("Cloudinary cleanup failed; publicId={}",remove); }
            }
        });
    }
    @Transactional
    public void save(Long id,ProductDTO dto,MultipartFile image,CustomUserDetails actor) {
        Product p=id==null?mapper.toEntity(dto):owned(id,actor);
        if (id==null) p.setUser(users.findById(actor.getId()).orElseThrow());
        else mapper.update(dto,p);
        if (image!=null && !image.isEmpty()) {
            var upload=images.upload(image);
            cleanupAfterCompletion(p.getImagePublicId(),upload.publicId());
            p.setImageUrl(upload.url());p.setImagePublicId(upload.publicId());
        }
        products.saveAndFlush(p);
    }
    @Transactional
    public void delete(Long id,CustomUserDetails actor) {
        Product p=owned(id,actor);
        cleanupAfterCompletion(p.getImagePublicId(),null);products.delete(p);
    }
}
