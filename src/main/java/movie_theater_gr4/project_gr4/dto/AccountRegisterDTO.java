package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRegisterDTO {

    @Size(min = 3, max = 50, message = "✗ Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "✗ Username must contain only letters, numbers, and underscores")
    private String username;

//    @NotBlank(message = "✗ Password is required")
    @Pattern(regexp = "^.{8,}$",
            message = "✗ Password must be at least 8 characters")
    @Pattern(regexp = ".*[0-9].*",
            message = "✗ Include a digit")
    @Pattern(regexp = ".*[!@#$%^&*].*",
            message = "✗ Include a special character")
    @Pattern(regexp = ".*[A-Z].*",
            message = "✗ Include an uppercase letter")
    @Pattern(regexp = "^\\S*$",
            message = "✗ Include no spaces")
    private String password;

//    @NotBlank(message = "Password Again is required")
    @Size(max = 255, message = "Password hash must not exceed 255 characters")
    private String passwordConfirm;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    public void trimFields() {
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }
        if (passwordConfirm != null) {
            passwordConfirm = passwordConfirm.trim();
        }
        if (email != null) {
            email = email.trim();
        }
    }

}
