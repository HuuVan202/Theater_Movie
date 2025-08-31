package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.enums.Roles;

import java.time.LocalDate;


@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int accountId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$",
            message = "Password has at least 8 char (include 1 number, 1 uppercase, 1 lowercase and 1 special char)")
    private String password;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @Email(message = "Email is not valid")
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "identity_card", length = 50)
    private String identityCard;

    @Column(name = "image")
    private String avatarUrl;

    @Column(name = "register_date")
    private LocalDate registerDate = LocalDate.now();

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "role_id", nullable = false)
    private Roles role;

    @Column(name = "is_google", nullable = false)
    private Boolean isGoogle;

}
