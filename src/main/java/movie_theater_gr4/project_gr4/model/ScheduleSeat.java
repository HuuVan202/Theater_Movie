package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "schedule_seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_seat_id")
    private Long scheduleSeatId;

    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "seat_price", nullable = false)
    private BigDecimal seatPrice;

    @Column(name = "status")
    private Integer status = 0; // 0: available, 1: booked...
}

