package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới, không chứa dấu cách hoặc ký tự đặc biệt")
    private String username;

//    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8 đến 100 ký tự")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt")
    private String password;

//    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String passwordConfirm;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 150, message = "Họ và tên phải từ 2 đến 150 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "[MF]", message = "Giới tính phải là 'M' hoặc 'F'")
    private String gender;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải đúng 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, max = 200, message = "Địa chỉ phải từ 5 đến 200 ký tự")
    private String address;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Pattern(regexp = "^\\d{12}$", message = "CMND/CCCD phải đúng 12 chữ số")
    private String identityCard;

    @NotNull(message = "Ngày đăng ký không được để trống")
    private LocalDate registerDate;

    @NotNull(message = "Trạng thái không được để trống")
    @Min(value = 0, message = "Trạng thái phải là 0 hoặc 1")
    @Max(value = 1, message = "Trạng thái phải là 0 hoặc 1")
    private Integer status;

    private String avatarUrl;

//    @NotNull(message = "Bạn phải đồng ý với các điều khoản")
    private Boolean termsAgreed;

    public void trimFields() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
        if (passwordConfirm != null) passwordConfirm = passwordConfirm.trim();
        if (fullName != null) fullName = fullName.trim();
        if (email != null) email = email.trim();
        if (phoneNumber != null) phoneNumber = phoneNumber.trim();
        if (address != null) address = address.trim();
        if (identityCard != null) identityCard = identityCard.trim();
        if (gender != null) gender = gender.trim();
    }
}