package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private Integer promotionId;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 150, message = "Tiêu đề không được dài quá 150 ký tự")
    private String title;

    @Size(max = 1000, message = "Chi tiết không được dài quá 1000 ký tự")
    private String detail;

    @Min(value = 0, message = "Mức giảm giá phải lớn hơn hoặc bằng 0")
    @Max(value = 100, message = "Mức giảm giá phải nhỏ hơn hoặc bằng 100")
    private Double discountLevel;

    @Min(value = 0, message = "Giảm giá cố định phải lớn hơn hoặc bằng 0")
    private Double discountAmount;

    @Min(value = 0, message = "Số vé tối thiểu phải lớn hơn hoặc bằng 0")
    private Integer minTickets;

    @Min(value = 0, message = "Số vé tối đa phải lớn hơn hoặc bằng số vé tối thiểu")
    private Integer maxTickets;

    @Min(value = 1, message = "Ngày áp dụng phải từ 1 đến 7")
    @Max(value = 7, message = "Ngày áp dụng phải từ 1 đến 7")
    private Integer dayOfWeek;

    private Integer ticketTypeId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @Future(message = "Thời gian kết thúc không được trong quá khứ")
    private LocalDateTime endTime;

    @Size(max = 255, message = "URL ảnh không được dài quá 255 ký tự")
    private String imageUrl;

    private boolean active;

    @Min(value = 0, message = "Giới hạn sử dụng phải lớn hơn hoặc bằng 0")
    private Integer maxUsage;

    private Long movieId;
}