package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "promotion")
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer promotionId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "discount_level")
    private Double discountLevel;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "min_tickets")
    private Integer minTickets;

    @Column(name = "max_tickets")
    private Integer maxTickets;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "ticket_type_id")
    private Integer ticketTypeId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Override
    public String toString() {
        return "Promotion{promotionId=" + promotionId + ", title='" + title + "'}";
    }
}