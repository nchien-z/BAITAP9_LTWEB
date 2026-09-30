package vn.iotstar.repository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query(value="select p from Product p join fetch p.user u where (:owner is null or u.id=:owner) and "
        + "(lower(p.name) like lower(concat('%',:keyword,'%')) or lower(coalesce(p.description,'')) like lower(concat('%',:keyword,'%')))",
        countQuery="select count(p) from Product p where (:owner is null or p.user.id=:owner) and "
        + "(lower(p.name) like lower(concat('%',:keyword,'%')) or lower(coalesce(p.description,'')) like lower(concat('%',:keyword,'%')))")
    Page<Product> search(@Param("keyword") String keyword, @Param("owner") Long owner, Pageable pageable);
    long countByUserId(Long id);
}
