package movie_theater_gr4.project_gr4.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "showtime")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showtime_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "show_date_id", nullable = false)
    private ShowDate showDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private CinemaRoom room;

//    @Column(name = "room_id", nullable = false)
//    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private Version version;

    @Column(name = "available_seats")
    private Integer availableSeats;
//
//    @Column(name = "room_number")
//    private String roomNumber;

}
