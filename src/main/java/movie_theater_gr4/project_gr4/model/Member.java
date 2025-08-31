package movie_theater_gr4.project_gr4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberId;

    private Integer score;
    private String tier;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;
}