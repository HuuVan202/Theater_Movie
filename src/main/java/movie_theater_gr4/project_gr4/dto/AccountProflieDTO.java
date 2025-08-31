package movie_theater_gr4.project_gr4.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.enums.Roles;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProflieDTO {

    private String username;

    private String fullName;

    @Email(message = "Emxample: abc@gmail.com")
    private String email;

    @Pattern(regexp = "^(0\\d{9})?$", message = "Phone number must start with 0 and have exactly 10 digits.")
    private String phoneNumber;

    private String address;

    private LocalDate dateOfBirth;

    private LocalDate registerDate;

    private String gender;

    @Pattern(regexp = "^([0-9]{12})?$", message = "Citizen ID must be exactly 12 digits or be empty")
    private String identityCard;

    private String avatarUrl;

    private Integer status;

    private Roles role;

    private Boolean isGoogle;
}
