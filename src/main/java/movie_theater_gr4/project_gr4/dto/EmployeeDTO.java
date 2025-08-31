package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private int employeeId;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Pattern(regexp = "^\\d{12}$", message = "CMND/CCCD phải là 12 chữ số")
    private String identityCard;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{9,10}$", message = "Số điện thoại phải là 9 hoặc 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, max = 200, message = "Địa chỉ phải từ 5 đến 200 ký tự")
    private String address;

    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^[MF]$", message = "Giới tính phải là 'M' (Nam) hoặc 'F' (Nữ)")
    private String gender;

    @NotNull(message = "Ngày tuyển dụng không được để trống")
    @PastOrPresent(message = "Ngày tuyển dụng không được sau ngày hiện tại")
    private LocalDate hireDate;

    @NotBlank(message = "Chức vụ không được để trống")
    @Size(min = 2, max = 50, message = "Chức vụ phải từ 2 đến 50 ký tự")
    private String position;

    private String avatarUrl;
}