
package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;

    @NotNull(message = "Hàng ghế không được để trống")
    @Min(value = 1, message = "Hàng ghế phải lớn hơn 0")
    private Integer seatRow;

    @NotBlank(message = "Cột ghế không được để trống")
    @Pattern(regexp = "^[A-Z]$", message = "Cột ghế phải là một chữ cái in hoa từ A-Z")
    private String seatColumn;

    @NotNull(message = "Trạng thái ghế không được để trống")
    private Boolean isActive;

    @NotNull(message = "Loại ghế không được để trống")
    private SeatTypeDTO seatType;

    @NotNull(message = "Giá ghế không được để trống")
    private Double seatPrice;

    @NotNull(message = "Phòng chiếu không được để trống")
    private CinemaRoomDTO room;

}