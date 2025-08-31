package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "schedule_time", nullable = false)
    private LocalTime scheduleTime;

//    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Showtime> showtimes;
}
