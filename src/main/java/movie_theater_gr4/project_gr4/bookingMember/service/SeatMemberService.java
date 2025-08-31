package movie_theater_gr4.project_gr4.bookingMember.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoicePromotionRepository;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.dto.SeatMapDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.SeatSelectionInfoDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.ShowtimeDetailDTO;
import movie_theater_gr4.project_gr4.mapper.SeatMapper;
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.bookingMember.repository.SeatRepository;
import movie_theater_gr4.project_gr4.service.SeatTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional

public class SeatMemberService {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatTypeService seatTypeService;
    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private InvoicePromotionRepository invoicePromotionRepository;



    @Transactional
    public void saveSeats(CinemaRoom cinemaRoom, SeatMapDTO seatMapDTO) {
        seatMapDTO.validate();
        int rows = seatMapDTO.getRows();
        int cols = seatMapDTO.getCols();
        String[][] seatTypes = seatMapDTO.getSeatTypes();
        System.out.println("Số dòng: " + rows);
        System.out.println("Số cột: " + cols);
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < seatTypes.length && j < seatTypes[i].length && seatTypes[i][j] != null) {
                    String type = seatTypes[i][j];
                    if ("Normal".equals(type) || "VIP".equals(type) || "Couple".equals(type)) {
                        System.out.println("Đang lưu ghế: hàng " + (i + 1) + ", cột " + ((char) ('A' + j)) + ", loại: " + type);
                        Seat seat = Seat.builder()
                                .cinemaRoom(cinemaRoom)
                                .seatRow(i + 1)
                                .seatColumn(Character.toString((char) ('A' + j)))
                                .seatType(findOrCreateSeatType(type, getDescription(type)))
                                .isActive(true)
                                .build();
                        try {
                            seatRepository.save(seat);
                        } catch (DataIntegrityViolationException e) {
                            throw new IllegalArgumentException("Duplicate seat at row " + (i + 1) + ", column " + ((char) ('A' + j)));
                        }
                    }
                }
            }
        }

    }

    private SeatType findOrCreateSeatType(String typeName, String description) {
        return seatTypeService.findOrCreateSeatType(typeName, description);
    }

    //    public ShowtimeDetailDTO getMovieInfoOfSelectSeatByShowtimeId(long id) {
//        Object[] row = (Object[]) seatRepository.getMovieInfoOfSelectSeatByShowtimeId(id);
//        if (row == null) return null;
//
//        return ShowtimeDetailDTO.builder()
//                .movieName((String) row[0])
//                .versionName((String) row[1])
//                .ratingCode((String) row[2])
//                .ratingName((String) row[3])
//                .ratingDescription((String) row[4])
//                .showDateId(((Number) row[5]).longValue())
//                .showDate(((java.sql.Date) row[6]).toLocalDate())
//                .scheduleId(((Number) row[7]).longValue())
//                .scheduleTime(LocalTime.parse(row[8].toString()))
//                .roomId(((Number) row[9]).longValue())
//                .roomName((String) row[10])
//                .seatQuantity((Integer) row[11])
//                .screenType((String) row[12])
//                .status((Integer) row[13])
//                .build();
//    }



    public ScheduleSeat getScheduleSeatsByIds(Long id) {
        return seatRepository.findByScheduleSeatIds(id);
    }

    public ShowtimeDetailDTO getMovieInfoOfSelectSeatByShowtimeId(long id) {
        List<Object[]> results = seatRepository.getMovieInfoOfSelectSeatByShowtimeId(id);
        if (results == null || results.isEmpty()) return null;

        Object[] row = results.get(0);

        return ShowtimeDetailDTO.builder()
                .movieName((String) row[0])
                .versionName((String) row[1])
                .ratingCode((String) row[2])
                .ratingName((String) row[3])
                .ratingDescription((String) row[4])
                .showDateId(((Number) row[5]).longValue())
                .showDate(((java.sql.Date) row[6]).toLocalDate())
                .scheduleId(((Number) row[7]).longValue())
                .scheduleTime(LocalTime.parse(row[8].toString()))
                .roomId(((Number) row[9]).longValue())
                .roomName((String) row[10])
                .seatQuantity((Integer) row[11])
                .screenType((String) row[12])
                .status((Integer) row[13])
                .build();
    }

    public List<Promotion> getAllPromotions() {
        return seatRepository.findAllPromotions();
    }


    public List<SeatSelectionInfoDTO> getSeatsByShowtime(Long scheduleId, Long movieId, Long versionId) {
        List<Object[]> rows = seatRepository.findSeatSelectionInfo(scheduleId, movieId, versionId);

        return rows.stream().map(r -> new SeatSelectionInfoDTO(
                (Number) r[0],        // seat_id
                (Integer) r[1],       // seat_row
                (String) r[2],        // seat_column
                (Boolean) r[3],       // is_active

                (Number) r[4],        // seat_type_id
                (String) r[5],        // seat_type_name

                (Number) r[6],        // room_id
                (String) r[7],        // room_name
                (Integer) r[8],       // seat_quantity
                (String) r[9],        // room_type
                (Integer) r[10],      // room_status

                r[11] != null ? (Number) r[11] : null,   // schedule_seat_id (nullable)
                r[12] != null ? toBigDecimal(r[12]) : null,
                (Integer) r[13],      // booking_status
                (Long)r[14],
                (Boolean) r[15]       // is_booked
        )).collect(Collectors.toList());
    }
    private BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } else if (val instanceof Double) {
            return BigDecimal.valueOf((Double) val);
        } else if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        } else if (val instanceof String) {
            return new BigDecimal((String) val);
        } else {
            return null;
        }
    }

    public List<SeatSelectionInfoDTO> getListSelectSeats(
            Long scheduleId,

            List<Long> scheduleSeatIds
    ) {
        List<Object[]> rows = seatRepository.findSeatSelectionInfo(scheduleId, scheduleSeatIds);

        return rows.stream().map(r -> {
            return new SeatSelectionInfoDTO(
                    (Number) r[0],                             // seat_id
                    (Integer) r[1],                            // seat_row
                    (String) r[2],                             // seat_column
                    (Boolean) r[3],                            // is_active

                    (Number) r[4],                             // seat_type_id
                    (String) r[5],                             // type_name

                    (Number) r[6],                             // room_id
                    (String) r[7],                             // room_name
                    (Integer) r[8],                            // seat_quantity
                    (String) r[9],                             // room_type
                    (Integer) r[10],                           // room_status

                    r[11] != null ? (Number) r[11] : null,     // schedule_seat_id
                    r[12] != null ? toBigDecimal(r[12]) : null,
                    r[13] != null ? (Integer) r[13] : null,    // seat_status
                    ((Number) r[14]).longValue(),              // showtime_id
                    (Boolean) r[15]                            // is_booked
            );
        }).collect(Collectors.toList());
    }


    public MemberDTO getMemberByUsername(String username) {
        return seatRepository.findMemberByUsername(username);
    }

    public MemberDTO getMemberByMemberId(String memberID) {
        return seatRepository.findMemberByMemberId(memberID);
    }
    public TicketType getSeatTypeById(long id) {
        return seatRepository.getTicketType(id);
    }

    public List<InvoicePromotion> findInvoicePromotionAllByUsername (String username) {
        return invoicePromotionRepository.findInvoicePromotionAllByUsername(username);
    }
    private String getDescription(String typeName) {
        switch (typeName) {
            case "Normal":
                return "Standard seat with basic comfort";
            case "VIP":
                return "Premium seat with extra legroom";
            case "Couple":
                return "Double seat for couples";
            default:
                return "";
        }

    }
}
