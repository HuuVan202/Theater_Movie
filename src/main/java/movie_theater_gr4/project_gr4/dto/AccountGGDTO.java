package movie_theater_gr4.project_gr4.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountGGDTO {
    private String fullName;
    private String email;
    private String avatarUrl;
}
