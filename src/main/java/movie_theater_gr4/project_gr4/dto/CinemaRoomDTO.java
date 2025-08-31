package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaRoomDTO {
    private Long roomId;

    @NotBlank(message = "Tên phòng không được để trống")
    @Size(min = 2, max = 50, message = "Tên phòng phải từ 2 đến 50 ký tự")
    private String roomName;

    @NotNull(message = "Số lượng ghế không được để trống")
    @Min(value = 1, message = "Số lượng ghế ít nhất là 1")
    private Integer seatQuantity;

    @NotBlank(message = "Loại phòng không được để trống")
    @Pattern(regexp = "^(2D|3D|IMAX|4DX)$", message = "Loại phòng không hợp lệ")
    private String type;

    @NotNull(message = "Trạng thái không được để trống")
    @Min(value = 0, message = "Trạng thái không hợp lệ")
    @Max(value = 1, message = "Trạng thái không hợp lệ")
    private Integer status;

    @NotNull(message = "Bản đồ ghế không được để trống")
    private SeatMapDTO seatMap;

    @NotNull(message = "Giá ghế không được để trống")
    private Map<String, Double> seatPrices = new HashMap<>(); // Thêm trường để lưu giá của các loại ghế

    public CinemaRoomDTO(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }
}