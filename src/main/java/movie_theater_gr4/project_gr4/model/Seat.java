
package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private CinemaRoom cinemaRoom;

    @Column(name = "seat_row", nullable = false)
    private Integer seatRow;


    @Column(name = "seat_column", nullable = false)
    private String seatColumn;

    @ManyToOne
    @JoinColumn(name = "seat_type_id")
    private
    SeatType seatType;

    @Column(name = "seat_price")
    private Double seatPrice;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

