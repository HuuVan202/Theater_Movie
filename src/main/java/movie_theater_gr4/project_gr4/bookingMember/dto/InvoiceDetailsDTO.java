package movie_theater_gr4.project_gr4.bookingMember.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date; // thêm import này

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetailsDTO {

    private Long invoiceId;

    private Long accountId;
    private Long employee_Id;

    private Timestamp bookingDate; // TIMESTAMP

    private BigDecimal totalAmount;

    private String paymentMethod;

    private Integer useScore;

    private Integer addScore;

    private Integer status;

    private String movieName;

    private String seatNumber;

    private Date showDate;     // DATE
    private Time scheduleTime; // TIME
}
