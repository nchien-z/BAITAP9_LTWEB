package vn.iotstar.controller;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;
@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService products;
    public ProductController(ProductService products) { this.products=products; }
    @GetMapping public String list(@RequestParam(defaultValue="") String keyword,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,
            @AuthenticationPrincipal CustomUserDetails user,Model model) {
        var result=products.search(keyword,page,size,user);
        model.addAttribute("products",result);model.addAttribute("keyword",keyword);model.addAttribute("size",result.getSize());
        model.addAttribute("ownCount",products.countByUser(user.getId()));return "products/list";
    }
    @GetMapping("/create") public String create(Model model) {
        model.addAttribute("productDTO",new ProductDTO());model.addAttribute("mode","create");return "products/form";
    }
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetails user,Model model) {
        model.addAttribute("productDTO",products.find(id,user));model.addAttribute("mode","edit");return "products/form";
    }
    @PostMapping({"/create","/edit/{id}"}) public String save(@PathVariable(required=false) Long id,
            @Valid @ModelAttribute("productDTO") ProductDTO dto,BindingResult errors,
            @RequestParam(required=false) MultipartFile image,@AuthenticationPrincipal CustomUserDetails user,
            Model model,RedirectAttributes redirect) {
        dto.setId(id);model.addAttribute("mode",id==null?"create":"edit");
        if (id!=null) dto.setImageUrl(products.find(id,user).getImageUrl());
        if (errors.hasErrors()) return "products/form";
        try { products.save(id,dto,image,user); }
        catch (IllegalArgumentException | IllegalStateException ex) { errors.reject("product",ex.getMessage());return "products/form"; }
        redirect.addFlashAttribute("successMessage","Đã lưu sản phẩm.");return "redirect:/products";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetails user,RedirectAttributes redirect) {
        products.delete(id,user);redirect.addFlashAttribute("successMessage","Đã xóa sản phẩm.");return "redirect:/products";
    }
}
