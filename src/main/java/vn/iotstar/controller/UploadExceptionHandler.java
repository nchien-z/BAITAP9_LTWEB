package vn.iotstar.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@ControllerAdvice
public class UploadExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge(RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", "File quá lớn. Vui lòng chọn ảnh dưới 10 MB và nhập lại sản phẩm.");
        return "redirect:/products";
    }
}
