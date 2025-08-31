package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "show_dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_date_id")
    private Long showDateId;

    @Column(name = "show_date", nullable = false, unique = true)
    private LocalDate showDate;

//    @OneToMany(mappedBy = "showDate", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Showtime> showtime;
}
