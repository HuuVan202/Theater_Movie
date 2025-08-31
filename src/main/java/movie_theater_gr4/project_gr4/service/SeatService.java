package movie_theater_gr4.project_gr4.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.bookingMember.repository.SeatRepository;
import movie_theater_gr4.project_gr4.dto.SeatMapDTO;
//import movie_theater_gr4.project_gr4.dto.SeatSelectionInfoDTO;
import movie_theater_gr4.project_gr4.mapper.SeatMapper;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import movie_theater_gr4.project_gr4.dto.SeatDTO;
import movie_theater_gr4.project_gr4.model.Seat;
import movie_theater_gr4.project_gr4.model.SeatType;
import movie_theater_gr4.project_gr4.repository.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatService implements ISeatService {
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatTypeRepository seatTypeRepository;
    @Autowired
    private SeatTypeService seatTypeService;
    @Autowired
    private SeatMapper seatMapper;

    @Transactional
    public void saveSeats(CinemaRoom cinemaRoom, SeatMapDTO seatMapDTO, Map<String, Double> seatPrices) {

        // Kiểm tra dữ liệu đầu vào
        int rows = seatMapDTO.getRows();
        int cols = seatMapDTO.getCols();
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Số hàng và cột phải lớn hơn 0");
        }
        String[][] seatTypes = seatMapDTO.getSeatTypes();
        if (seatTypes == null || seatTypes.length != rows || (seatTypes.length > 0 && seatTypes[0].length != cols)) {
            throw new IllegalArgumentException("Kích thước mảng loại ghế không hợp lệ");
        }

        // Kiểm tra loại ghế hợp lệ
        List<String> validTypes = Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty");
        for (String[] row : seatTypes) {
            for (String type : row) {
                if (type == null || !validTypes.contains(type)) {
                    throw new IllegalArgumentException("Loại ghế không hợp lệ: " + type);
                }
            }
        }

        // Xử lý cột cầu thang (nếu có)
        Set<Integer> staircaseSet = new HashSet<>();
        if (seatMapDTO.getStaircaseColumns() != null && !seatMapDTO.getStaircaseColumns().isEmpty()) {
            for (String colStr : seatMapDTO.getStaircaseColumns().split(",")) {
                try {
                    int colIndex = Integer.parseInt(colStr.trim()) - 1; // Chuyển từ 1-based sang 0-based
                    if (colIndex < 0 || colIndex >= cols) {
                        throw new IllegalArgumentException("Cột cầu thang không hợp lệ: " + colStr);
                    }
                    staircaseSet.add(colIndex);
                } catch (NumberFormatException e) {
                    System.out.println("Cột cầu thang không hợp lệ: " + colStr);
                    throw new IllegalArgumentException("Định dạng cột cầu thang không hợp lệ: " + colStr);
                }
            }
        }

        // Trước tiên, lấy tất cả các ghế hiện có của phòng này (nếu có)
        List<Seat> existingSeats = seatRepository.findByCinemaRoom(cinemaRoom);
        Map<String, Seat> existingSeatMap = new HashMap<>();
        for (Seat seat : existingSeats) {
            String key = seat.getSeatRow() + "-" + seat.getSeatColumn();
            existingSeatMap.put(key, seat);
        }

        // Xác định các loại ghế thực sự xuất hiện trong seatMap
        Set<String> validSeatTypes = new HashSet<>();
        for (String[] row : seatTypes) {
            for (String type : row) {
                if (type != null && !type.isEmpty() && !"Staircase".equals(type) && !"Empty".equals(type)) {
                    validSeatTypes.add(type);
                }
            }
        }

        // Kiểm tra xem seatPrices có chứa giá cho tất cả các loại ghế trong seatMap không
        if (seatPrices == null) {
            System.out.println("seatPrices is null!");
            throw new IllegalArgumentException("Giá ghế không được để trống");
        }
        for (String seatType : validSeatTypes) {
            String lowerCaseType = seatType.toLowerCase();
            if (!seatPrices.containsKey(lowerCaseType)) {
                System.out.println("Thiếu giá cho loại ghế: " + seatType + ", seatPrices: " + seatPrices);
                throw new IllegalArgumentException("Giá ghế không được để trống cho loại " + seatType);
            }
        }

        // Tạo danh sách các ghế cần lưu
        List<Seat> seatsToSave = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Kiểm tra nếu cột này là cột cầu thang
                String type;
                if (staircaseSet.contains(j)) {
                    type = "Staircase";
                } else if (i < seatTypes.length && j < seatTypes[i].length && seatTypes[i][j] != null) {
                    type = seatTypes[i][j];
                } else {
                    type = "Normal"; // Mặc định
                }

                if ("Normal".equals(type) || "VIP".equals(type) || "Couple".equals(type) || "Empty".equals(type) || "Staircase".equals(type)) {
                    int row = i + 1;
                    String column = Character.toString((char) ('A' + j));
                    String key = row + "-" + column;

                    System.out.println("Đang xử lý ghế: hàng " + row + ", cột " + column + ", loại: " + type);

                    // Kiểm tra xem ghế đã tồn tại chưa
                    if (existingSeatMap.containsKey(key)) {
                        // Nếu đã tồn tại, cập nhật loại ghế và giá
                        Seat existingSeat = existingSeatMap.get(key);
                        existingSeat.setSeatType(findOrCreateSeatType(type, getDescription(type)));
                        // Chỉ gán giá nếu là loại ghế hợp lệ và có trong seatPrices
                        if (validSeatTypes.contains(type)) {
                            Double price = seatPrices.get(type.toLowerCase());
                            if (price == null) {
                                System.out.println("Giá không tìm thấy cho loại " + type + ", seatPrices: " + seatPrices);
                                throw new IllegalArgumentException("Giá ghế không được để trống cho loại " + type);
                            }
                            existingSeat.setSeatPrice(price);
                            System.out.println("Gán giá " + price + " cho ghế: hàng " + row + ", cột " + column);
                        } else {
                            System.out.println("Không gán giá cho ghế: hàng " + row + ", cột " + column + " (loại " + type + " không trong validSeatTypes)");
                        }
                        seatsToSave.add(existingSeat);
                    } else {
                        // Nếu chưa tồn tại, tạo mới với giá
                        Seat seat = Seat.builder()
                                .cinemaRoom(cinemaRoom)
                                .seatRow(row)
                                .seatColumn(column)
                                .seatType(findOrCreateSeatType(type, getDescription(type)))
                                .seatPrice(validSeatTypes.contains(type) ? seatPrices.get(type.toLowerCase()) : 0.0)
                                .isActive(true)
                                .build();
                        if (validSeatTypes.contains(type)) {
                            System.out.println("Tạo mới ghế: hàng " + row + ", cột " + column + " với giá " + seat.getSeatPrice());
                        } else {
                            System.out.println("Tạo mới ghế: hàng " + row + ", cột " + column + " với giá mặc định 0.0");
                        }
                        seatsToSave.add(seat);
                    }
                }
            }
        }

        try {
            // Lưu tất cả các ghế trong một batch
            seatRepository.saveAll(seatsToSave);
            System.out.println("Đã lưu thành công " + seatsToSave.size() + " ghế cho phòng: " + cinemaRoom.getRoomId());
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu bản đồ ghế: " + e.getMessage() + ", Stack trace: " + Arrays.toString(e.getStackTrace()));
            throw new IllegalArgumentException("Lỗi khi lưu bản đồ ghế: " + e.getMessage());
        }
    }

    @Transactional
    public void updateSeats(CinemaRoom room, SeatMapDTO seatMap, Map<String, Double> seatPrices) {
        System.out.println("=== Bắt đầu updateSeats cho phòng: " + room.getRoomId() + " ===");

        int rows = seatMap.getRows();
        int cols = seatMap.getCols();
        String[][] types = seatMap.getSeatTypes();

        if (rows <= 0 || cols <= 0 || types == null || types.length != rows || types[0].length != cols) {
            throw new IllegalArgumentException("Kích thước bản đồ ghế không hợp lệ");
        }

        // Lấy các loại ghế hợp lệ cần giá
        Set<String> validSeatTypes = new HashSet<>();
        for (String[] row : types) {
            for (String type : row) {
                if (type != null && !type.equals("Staircase") && !type.equals("Empty")) {
                    validSeatTypes.add(type);
                }
            }
        }

        // Kiểm tra seatPrices đầy đủ
        if (seatPrices == null) {
            throw new IllegalArgumentException("Giá ghế không được để trống");
        }
        for (String type : validSeatTypes) {
            String key = type.toLowerCase();
            if (!seatPrices.containsKey(key) || seatPrices.get(key) == null || seatPrices.get(key) < 0) {
                throw new IllegalArgumentException("Thiếu hoặc sai giá cho loại ghế: " + type);
            }
        }

        // Parse cột cầu thang
        Set<Integer> staircaseCols = new HashSet<>();
        if (seatMap.getStaircaseColumns() != null && !seatMap.getStaircaseColumns().isEmpty()) {
            for (String colStr : seatMap.getStaircaseColumns().split(",")) {
                int colIdx = Integer.parseInt(colStr.trim()) - 1;
                if (colIdx < 0 || colIdx >= cols)
                    throw new IllegalArgumentException("Cột cầu thang không hợp lệ: " + colStr);
                staircaseCols.add(colIdx);
            }
        }

        // Chuẩn bị dữ liệu ghế cũ
        List<Seat> oldSeats = seatRepository.findByCinemaRoom(room);
        Map<String, Seat> oldSeatMap = new HashMap<>();
        for (Seat seat : oldSeats) {
            oldSeatMap.put(seat.getSeatRow() + "-" + seat.getSeatColumn(), seat);
        }

        // Cache SeatType
        Set<String> allowedTypes = Set.of("Normal", "VIP", "Couple", "Empty", "Staircase");
        Map<String, SeatType> seatTypeCache = new HashMap<>();
        for (String type : allowedTypes) {
            seatTypeRepository.findByTypeName(type).ifPresent(t -> seatTypeCache.put(type, t));
        }

        Set<String> newKeys = new HashSet<>();
        List<Seat> toCreate = new ArrayList<>();
        List<Seat> toUpdate = new ArrayList<>();
        List<Seat> toDelete = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String type = staircaseCols.contains(j) ? "Staircase" : types[i][j];
                if (type == null || !allowedTypes.contains(type)) continue;

                int rowNum = i + 1;
                String colChar = Character.toString((char) ('A' + j));
                String key = rowNum + "-" + colChar;
                newKeys.add(key);

                SeatType seatType = seatTypeCache.computeIfAbsent(type, t ->
                        seatTypeRepository.findByTypeName(t).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại ghế: " + t))
                );

                double price = validSeatTypes.contains(type) ? seatPrices.get(type.toLowerCase()) : 0.0;

                if (oldSeatMap.containsKey(key)) {
                    Seat existing = oldSeatMap.get(key);
                    if (!existing.getSeatType().getTypeName().equals(type) || !existing.getSeatPrice().equals(price)) {
                        existing.setSeatType(seatType);
                        existing.setSeatPrice(price);
                        toUpdate.add(existing);
                        System.out.println("→ Cập nhật: " + key + ", loại: " + type + ", giá: " + price);
                    }
                } else {
                    Seat seat = Seat.builder()
                            .cinemaRoom(room)
                            .seatRow(rowNum)
                            .seatColumn(colChar)
                            .seatType(seatType)
                            .seatPrice(price)
                            .isActive(true)
                            .build();
                    toCreate.add(seat);
                    System.out.println("→ Thêm mới: " + key + ", loại: " + type + ", giá: " + price);
                }
            }
        }

        // Tìm ghế bị xóa
        for (Map.Entry<String, Seat> entry : oldSeatMap.entrySet()) {
            if (!newKeys.contains(entry.getKey())) {
                toDelete.add(entry.getValue());
                System.out.println("→ Xóa ghế: " + entry.getKey());
            }
        }

        // Lưu DB
        try {
            if (!toCreate.isEmpty()) seatRepository.saveAll(toCreate);
            if (!toUpdate.isEmpty()) seatRepository.saveAll(toUpdate);
            if (!toDelete.isEmpty()) seatRepository.deleteAll(toDelete);

            System.out.printf("✔ Update xong: Thêm %d, Cập nhật %d, Xóa %d ghế%n",
                    toCreate.size(), toUpdate.size(), toDelete.size());
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi cập nhật ghế: " + e.getMessage());
        }
    }


    private SeatType findOrCreateSeatType(String typeName, String description) {
        return seatTypeService.findOrCreateSeatType(typeName, description);
    }

    public List<SeatDTO> getSeatsByScheduleMovieVersion(Long scheduleId, Long movieId, Long versionId) {
        List<Seat> listSeats = seatRepository.findSeatsByScheduleMovieVersion(scheduleId, movieId, versionId);
        return listSeats.stream()
                .map(seatMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateScheduleSeatStatus(List<Long> scheduleSeatId, int status) {
        int updated = seatRepository.updateStatusByscheduleSeatIds(status, scheduleSeatId);
//        System.out.println("Đã cập nhật trạng thái " + status + " cho " + updated + " ghế");
    }

    private String getDescription(String typeName) {
        switch (typeName) {
            case "Normal":
                return "Standard seat with basic comfort";
            case "VIP":
                return "Premium seat with extra legroom";
            case "Couple":
                return "Double seat for couples";
            case "Empty":
                return "Empty space, no seat available";
            case "Staircase":
                return "Staircase or aisle for movement";
            default:
                return "";
        }
    }
}
