package movie_theater_gr4.project_gr4.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import movie_theater_gr4.project_gr4.bookingMember.repository.SeatRepository;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShowtimeService {
    @Autowired
    private ShowTimeRepository showtimeRepository;
    @Autowired
    private ShowDateRepository showDateRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private VersionRepository versionRepository;
    @Autowired
    private MovieVersionRepository movieVersionRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ScheduleSeatRepository scheduleSeatRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Transactional // For write operations
    public Showtime createShowtime(ShowtimeRequest request) {
        ShowDate showDate = showDateRepository.findByShowDate(request.getShowDate())
                .orElseGet(() -> {
                    ShowDate newShowDate = new ShowDate();
                    newShowDate.setShowDate(request.getShowDate());
                    return showDateRepository.save(newShowDate);
                });

        Schedule schedule = scheduleRepository.findByScheduleTime(request.getScheduleTime())
                .orElseGet(() -> {
                    Schedule newSchedule = new Schedule();
                    newSchedule.setScheduleTime(request.getScheduleTime());
                    return scheduleRepository.save(newSchedule);
                });

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Phim không tồn tại"));
        CinemaRoom room = cinemaRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại"));
        Version version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new IllegalArgumentException("Phiên bản không tồn tại"));

        if (!movieVersionRepository.existsByMovieMovieIdAndVersionVersionId(request.getMovieId(), request.getVersionId())) {
            throw new IllegalArgumentException("Phiên bản phim không hợp lệ");
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setShowDate(showDate);
        showtime.setSchedule(schedule);
        showtime.setRoom(room);
        showtime.setVersion(version);
        showtime.setAvailableSeats(room.getSeatQuantity());

        showtimeRepository.save(showtime);

        entityManager.createNativeQuery("SELECT insert_schedule_seats(?1, ?2, ?3)")
                .setParameter(1, showtime.getId())
                .setParameter(2, room.getRoomId())
                .setParameter(3, new BigDecimal("100000"))
                .getSingleResult();

        System.out.println("DEBUG - showtimeId: " + showtime.getId());
        System.out.println("DEBUG - roomId: " + room.getRoomId());
        System.out.println("DEBUG - seatPrice: " + new BigDecimal("100000"));
        return showtime;
    }

    @Transactional(readOnly = true) // For read-only operations
    public List<Showtime> getShowtimesByDate(LocalDate showDate) {
        return showtimeRepository.findByShowDate(showDate);
    }

    @Transactional(readOnly = true) // For read-only operations
    public List<Showtime> getShowtimesByDateRange(LocalDate startDate, LocalDate endDate) {
        return showtimeRepository.findByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true) // For read-only operations
    public boolean hasShowtimesInRoomAndDate(Long roomId, LocalDate showDate) {
        return showtimeRepository.existsByRoomIdAndShowDate(roomId, showDate);
    }

    @Transactional(readOnly = true) // For read-only operations
    public LocalTime suggestNextShowtime(Long roomId, Long showDateId, Long movieId) {
        if (!cinemaRoomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Phòng chiếu không tồn tại");
        }
        if (!showDateRepository.existsById(showDateId)) {
            throw new IllegalArgumentException("Ngày chiếu không tồn tại");
        }
        if (!movieRepository.existsById(movieId)) {
            throw new IllegalArgumentException("Phim không tồn tại");
        }
        return showtimeRepository.suggestNextShowtime(roomId, showDateId, movieId);
    }

    @Transactional // For write operations
    public void deleteShowtime(Long showtimeId) {
        if (ticketRepository.existsByShowtimeId(showtimeId)) {
            throw new IllegalStateException("Không thể xóa suất chiếu đã có vé đặt");
        }
        showtimeRepository.deleteById(showtimeId);
    }

    @Transactional
    public void deleteShowtimesByDate(LocalDate showDate) {
        List<Showtime> showtimes = showtimeRepository.findByShowDate(showDate);
        for (Showtime showtime : showtimes) {
            if (ticketRepository.existsByShowtimeId(showtime.getId())) {
                throw new IllegalStateException("Không thể xóa suất chiếu của ngày " + showDate + " vì đã có vé đặt");
            }
        }
        showtimeRepository.deleteAll(showtimes);
    }

    @Transactional // For write operations
    public Long getShowDateIdByDate(LocalDate showDate) {
        return showDateRepository.findByShowDate(showDate)
                .map(ShowDate::getShowDateId)
                .orElseGet(() -> {
                    ShowDate newShowDate = new ShowDate();
                    newShowDate.setShowDate(showDate);
                    return showDateRepository.save(newShowDate).getShowDateId();
                });
    }

    @Transactional(readOnly = true) // For read-only operations
    public List<Map<String, Object>> getRoomStatistics(LocalDate showDate) {
        List<Map<String, Object>> statistics = showtimeRepository.getRoomStatisticsByDate(showDate);
        List<Map<String, Object>> mutableStatistics = new ArrayList<>();
        for (Map<String, Object> room : statistics) {
            Map<String, Object> mutableRoom = new HashMap<>(room);
            Object moviesObj = mutableRoom.get("movies");
            if (moviesObj instanceof String) {
                try {
                    String moviesJson = (String) moviesObj;
                    List<Map<String, Object>> movies = objectMapper.readValue(moviesJson, new TypeReference<List<Map<String, Object>>>() {});
                    mutableRoom.put("movies", movies);
                } catch (Exception e) {
                    mutableRoom.put("movies", new ArrayList<>());
                }
            }
            mutableStatistics.add(mutableRoom);
        }
        return mutableStatistics;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMovieShowtimeStats(LocalDate selectedDate, String statsViewType, LocalDate startDate, LocalDate endDate) {
        switch (statsViewType.toLowerCase()) {
            case "day":
                return showtimeRepository.getMovieShowtimeStats(selectedDate);
            case "range":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Vui lòng chọn khoảng ngày");
                }
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
                }
                return showtimeRepository.getMovieShowtimeStatsByRange(startDate, endDate);
            case "month":
                startDate = selectedDate.withDayOfMonth(1);
                endDate = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth());
                break;
            case "year":
                startDate = selectedDate.withDayOfYear(1);
                endDate = selectedDate.withDayOfYear(selectedDate.lengthOfYear());
                break;
            default:
                throw new IllegalArgumentException("Chế độ xem không hợp lệ: " + statsViewType);
        }
        return showtimeRepository.getMovieShowtimeStatsByRange(startDate, endDate);
    }

    @Transactional
    public void deleteShowtimesByDateAndRoom(LocalDate showDate, Long roomId) {
        // Validate inputs
        if (showDate == null) {
            throw new IllegalArgumentException("Ngày chiếu không được để trống");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("Phòng chiếu không được để trống");
        }
        if (!cinemaRoomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Phòng chiếu không tồn tại");
        }

        // Fetch showtimes for the given date and room
        List<Showtime> showtimes = showtimeRepository.findByShowDateAndRoomId(showDate, roomId);
        if (showtimes.isEmpty()) {
            throw new IllegalStateException("Không có suất chiếu nào để xóa cho phòng " + roomId + " vào ngày " + showDate);
        }

        // Check for booked tickets
        for (Showtime showtime : showtimes) {
            if (ticketRepository.existsByShowtimeId(showtime.getId())) {
                throw new IllegalStateException("Không thể xóa suất chiếu của phòng " + roomId + " vào ngày " + showDate + " vì đã có vé đặt");
            }
        }

        // Delete all showtimes for the specified date and room
        showtimeRepository.deleteAll(showtimes);
    }
}
