package movie_theater_gr4.project_gr4.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MemberDTO {
    private int memberId;
    private int accountId;
    private String username;
    private String fullName;
    private String email;
    private Integer score;
    private String tier;
    private String avatarUrl;

    public MemberDTO(int memberId, int accountId, String username, String fullName, String email, Integer score, String tier) {
        this.memberId = memberId;
        this.accountId = accountId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.score = score;
        this.tier = tier;
    }

}