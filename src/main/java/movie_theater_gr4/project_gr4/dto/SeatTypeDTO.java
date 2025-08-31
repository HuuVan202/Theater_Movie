
package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTypeDTO {
    @NotNull(message = "ID loại ghế không được để trống")
    private Integer seatTypeId;

    @NotBlank(message = "Tên loại ghế không được để trống")
    @Pattern(regexp = "^(Normal|VIP|Couple)$", message = "Loại ghế không hợp lệ")
    private String typeName;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

}