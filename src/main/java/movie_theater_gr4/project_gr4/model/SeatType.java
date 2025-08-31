package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_type_id")
    private Integer seatTypeId;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @Column(name = "description")
    private String description;
}
