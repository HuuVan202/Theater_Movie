package movie_theater_gr4.project_gr4.bookingMember.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatSelectionInfoDTO {

    private Long seatId;
    private Integer seatRow;
    private String seatColumn;
    private Boolean isActive;

    private Long seatTypeId;
    private String seatTypeName;

    private Long roomId;
    private String roomName;
    private Integer seatQuantity;
    private String roomType;
    private Integer roomStatus;

    private Long scheduleSeatId;
    private BigDecimal seatPrice;
    private Integer bookingStatus;
    private Boolean isBooked;
    private Long showTimeId;
    private Boolean selected;

    public SeatSelectionInfoDTO(
            Number seatId,
            Integer seatRow,
            String seatColumn,
            Boolean isActive,
            Number seatTypeId,
            String seatTypeName,
            Number roomId,
            String roomName,
            Integer seatQuantity,
            String roomType,
            Integer roomStatus,
            Number scheduleSeatId,
            BigDecimal seatPrice,
            Integer bookingStatus,
            Long showTimeId,
            Boolean isBooked
    ) {
        this.seatId = seatId != null ? seatId.longValue() : null;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.isActive = isActive;

        this.seatTypeId = seatTypeId != null ? seatTypeId.longValue() : null;
        this.seatTypeName = seatTypeName;

        this.roomId = roomId != null ? roomId.longValue() : null;
        this.roomName = roomName;
        this.seatQuantity = seatQuantity;
        this.roomType = roomType;
        this.roomStatus = roomStatus;

        this.scheduleSeatId = scheduleSeatId != null ? scheduleSeatId.longValue() : null;
        this.seatPrice = seatPrice;
        this.bookingStatus = bookingStatus;
        this.isBooked = isBooked;
        this.showTimeId = showTimeId;
        this.selected = false; // mặc định chưa chọn
    }

}


