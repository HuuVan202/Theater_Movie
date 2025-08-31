package movie_theater_gr4.project_gr4.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.*;

@Data
public class SeatMapDTO {
    @NotNull(message = "Số hàng không được để trống")
    @Min(value = 1, message = "Số hàng phải lớn hơn 0")
    private int rows;

    @NotNull(message = "Số cột không được để trống")
    @Min(value = 1, message = "Số cột phải lớn hơn 0")
    private int cols;

    @NotNull(message = "Bản đồ loại ghế không được để trống")
    private String[][] seatTypes;

    @NotNull(message = "Cột cầu thang không được để trống")
    private String staircaseColumns; // Thêm trường này để lưu cột cầu thang (ví dụ: "1,3")

    @NotNull(message = "Giá ghế không được để trống")
    private Map<String, Double> seatPrices = new HashMap<>(); // Thêm trường để lưu giá của các loại ghế

    public void validate() {
        // Kiểm tra số hàng và cột
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Số hàng và cột phải lớn hơn 0");
        }
        // Kiểm tra kích thước mảng seatTypes
        if (seatTypes == null || seatTypes.length != rows || (seatTypes.length > 0 && seatTypes[0].length != cols)) {
            throw new IllegalArgumentException("Kích thước mảng loại ghế không hợp lệ");
        }

        // Danh sách các loại ghế hợp lệ
        List<String> validTypes = Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty");
        Set<String> actualSeatTypes = new HashSet<>(); // Các loại ghế cần giá

        // Kiểm tra loại ghế và thu thập các loại cần giá
        for (String[] row : seatTypes) {
            for (String type : row) {
                if (type == null || !validTypes.contains(type)) {
                    throw new IllegalArgumentException("Loại ghế không hợp lệ: " + type);
                }
                // Chỉ thêm các loại ghế cần giá (bỏ qua Staircase và Empty)
                if (!"Staircase".equals(type) && !"Empty".equals(type)) {
                    actualSeatTypes.add(type);
                }
            }
        }

        // Kiểm tra định dạng cột cầu thang
        if (staircaseColumns != null && !staircaseColumns.isEmpty()) {
            try {
                String[] columns = staircaseColumns.split(",");
                for (String col : columns) {
                    int colIndex = Integer.parseInt(col.trim()) - 1;
                    if (colIndex < 0 || colIndex >= cols) {
                        throw new IllegalArgumentException("Cột cầu thang không hợp lệ: " + col);
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Định dạng cột cầu thang không hợp lệ: " + staircaseColumns);
            }
        }

        // Khởi tạo seatPrices nếu null
        if (seatPrices == null) {
            seatPrices = new HashMap<>();
        }

        // Kiểm tra giá cho các loại ghế cần giá
        System.out.println("Kiểm tra giá, actualSeatTypes: " + actualSeatTypes + ", seatPrices: " + seatPrices);
        for (String type : actualSeatTypes) {
            String lowerCaseType = type.toLowerCase();
            System.out.println("Kiểm tra giá cho loại ghế: " + type + ", lowerCaseType: " + lowerCaseType);
            if (!seatPrices.containsKey(lowerCaseType) ||
                    seatPrices.get(lowerCaseType) == null ||
                    seatPrices.get(lowerCaseType) < 0) {
                throw new IllegalArgumentException("Giá cho loại ghế " + type + " không hợp lệ hoặc chưa được đặt");
            }
        }

        // Log để kiểm tra
        System.out.println("Validate thành công: actualSeatTypes=" + actualSeatTypes + ", seatPrices=" + seatPrices);
    }

    public int calculateActualSeats() {
        int count = 0;
        for (String[] row : seatTypes) {
            for (String type : row) {
                if (!("Staircase".equals(type))) {
                    count++;
                }
            }
        }
        return count;
    }
}