package vn.iotstar.mapper;
import org.mapstruct.*;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.ERROR)
public interface ProductMapper {
    @Mapping(target="userId", source="user.id")
    @Mapping(target="username", source="user.username")
    ProductDTO toDTO(Product product);
    @Mapping(target="id", ignore=true)
    @Mapping(target="user", ignore=true)
    @Mapping(target="imageUrl", ignore=true)
    @Mapping(target="imagePublicId", ignore=true)
    @Mapping(target="createdAt", ignore=true)
    Product toEntity(ProductDTO dto);
    @BeanMapping(ignoreByDefault=true)
    @Mapping(target="name", source="name")
    @Mapping(target="description", source="description")
    @Mapping(target="price", source="price")
    void update(ProductDTO dto, @MappingTarget Product entity);
}
