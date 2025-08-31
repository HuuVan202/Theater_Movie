package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.dto.CinemaRoomDTO;
import movie_theater_gr4.project_gr4.dto.MovieShowtimeDTO;
import movie_theater_gr4.project_gr4.dto.VersionDTO;
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.repository.*;
import movie_theater_gr4.project_gr4.service.ShowtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/showtime")
public class ShowtimeController {
    private static final Logger logger = LoggerFactory.getLogger(ShowtimeController.class);

    @Autowired
    private ShowtimeService showtimeService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private VersionRepository versionRepository;
    @Autowired
    private ShowDateRepository showDateRepository;

    @GetMapping
    public String showCalendar(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false, defaultValue = "week") String viewType,
            Model model) {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusDays(6);

        if ("month".equalsIgnoreCase(viewType)) {
            start = today.withDayOfMonth(1);
            end = today.withDayOfMonth(today.lengthOfMonth());
        }

        List<Showtime> showtimes = showtimeService.getShowtimesByDateRange(start, end);

        List<Map<String, Object>> events = showtimes.stream()
                .filter(s -> roomId == null || (s.getRoom() != null && s.getRoom().getRoomId().equals(roomId)))
                .filter(s -> s.getMovie() != null && s.getRoom() != null && s.getVersion() != null &&
                        s.getShowDate() != null && s.getShowDate().getShowDate() != null &&
                        s.getSchedule() != null && s.getSchedule().getScheduleTime() != null)
                .map(s -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", s.getId());
                    event.put("title", String.format("%s (%s, %s)",
                            s.getMovie().getMovieName(),
                            s.getRoom().getRoomName(),
                            s.getVersion().getVersionName()));
                    event.put("start", s.getShowDate().getShowDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" +
                            s.getSchedule().getScheduleTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
                    event.put("end", s.getShowDate().getShowDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" +
                            s.getSchedule().getScheduleTime().plusMinutes(s.getMovie().getDuration())
                                    .format(DateTimeFormatter.ISO_LOCAL_TIME));
                    return event;
                }).collect(Collectors.toList());

        List<CinemaRoomDTO> roomDTOs = cinemaRoomRepository.findAll().stream()
                .map(room -> new CinemaRoomDTO(room.getRoomId(), room.getRoomName()))
                .collect(Collectors.toList());

        List<MovieShowtimeDTO> movieDTOs = movieRepository.findAllWithVersions().stream()
                .map(movie -> {
                    MovieShowtimeDTO dto = new MovieShowtimeDTO();
                    dto.setMovieId(movie.getMovieId());
                    dto.setMovieName(movie.getMovieName());
                    dto.setVersions(movie.getVersions().stream()
                            .map(version -> new VersionDTO(version.getVersionId(), version.getVersionName(), version.getDescription()))
                            .collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());

        logger.info("View Type: {}, Date Range: {} to {}, Room ID: {}, Events: {}", viewType, start, end, roomId, events.size());
        logger.info("Rooms: {}", roomDTOs);

        model.addAttribute("showtimeRequest", new ShowtimeRequest());
        model.addAttribute("movies", movieDTOs);
        model.addAttribute("rooms", roomDTOs);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("viewType", viewType);
        model.addAttribute("events", events);
        return "showtime/calendar";
    }

    @PostMapping("/delete/{id}")
    public String deleteShowtime(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false, defaultValue = "week") String viewType,
            RedirectAttributes redirectAttributes) {
        try {
            showtimeService.deleteShowtime(id);
            redirectAttributes.addFlashAttribute("success", "Xóa suất chiếu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirectUrl = String.format("/admin/showtime?startDate=%s&endDate=%s&viewType=%s",
                startDate, endDate, viewType);
        if (roomId != null) {
            redirectUrl += "&roomId=" + roomId;
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/delete-by-date-and-room")
    public ResponseEntity<?> deleteShowtimesByDateAndRoom(
            @RequestParam("showDate") LocalDate showDate,
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "viewType", required = false) String viewType,
            @RequestParam(value = "selectedRoomId", required = false) String selectedRoomId) {
        try {
            showtimeService.deleteShowtimesByDateAndRoom(showDate, roomId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi không xác định khi xóa suất chiếu"));
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long roomId) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Showtime> showtimes = showtimeService.getShowtimesByDateRange(start, end);
        return showtimes.stream()
                .filter(s -> roomId == null || (s.getRoom() != null && s.getRoom().getRoomId().equals(roomId)))
                .filter(s -> s.getMovie() != null && s.getRoom() != null && s.getVersion() != null &&
                        s.getShowDate() != null && s.getShowDate().getShowDate() != null &&
                        s.getSchedule() != null && s.getSchedule().getScheduleTime() != null)
                .map(s -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", s.getId());
                    event.put("title", String.format("%s (%s, %s)",
                            s.getMovie().getMovieName(),
                            s.getRoom().getRoomName(),
                            s.getVersion().getVersionName()));
                    event.put("start", s.getShowDate().getShowDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" +
                            s.getSchedule().getScheduleTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
                    event.put("end", s.getShowDate().getShowDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T" +
                            s.getSchedule().getScheduleTime().plusMinutes(s.getMovie().getDuration())
                                    .format(DateTimeFormatter.ISO_LOCAL_TIME));
                    return event;
                }).collect(Collectors.toList());
    }

    @GetMapping("/statistics")
    public String showStatistics(
            @RequestParam(required = false) String showDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "day") String statsViewType,
            Model model) {
        LocalDate selectedDate = LocalDate.now();
        LocalDate startDateParsed = null;
        LocalDate endDateParsed = null;

        try {
            if ("range".equalsIgnoreCase(statsViewType)) {
                if (startDate == null || endDate == null) {
                    startDateParsed = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    endDateParsed = startDateParsed.plusDays(6);
                } else {
                    startDateParsed = LocalDate.parse(startDate);
                    endDateParsed = LocalDate.parse(endDate);
                    if (startDateParsed.isAfter(endDateParsed)) {
                        throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
                    }
                    selectedDate = startDateParsed; // Use startDate as selectedDate for consistency
                }
            } else if ("month".equalsIgnoreCase(statsViewType)) {
                if (month == null || year == null) {
                    selectedDate = LocalDate.now();
                    month = selectedDate.getMonthValue();
                    year = selectedDate.getYear();
                } else {
                    selectedDate = LocalDate.of(year, month, 1);
                }
            } else if ("year".equalsIgnoreCase(statsViewType)) {
                if (year == null) {
                    selectedDate = LocalDate.now();
                    year = selectedDate.getYear();
                } else {
                    selectedDate = LocalDate.of(year, 1, 1);
                }
            } else { // day
                selectedDate = showDate != null ? LocalDate.parse(showDate) : LocalDate.now();
            }

            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("startDate", startDateParsed);
            model.addAttribute("endDate", endDateParsed);
            model.addAttribute("statsViewType", statsViewType);
            model.addAttribute("month", month);
            model.addAttribute("year", year);
            model.addAttribute("statistics", showtimeService.getRoomStatistics(selectedDate));
            model.addAttribute("movieStats", showtimeService.getMovieShowtimeStats(selectedDate, statsViewType, startDateParsed, endDateParsed));
            return "showtime/statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            model.addAttribute("selectedDate", LocalDate.now());
            model.addAttribute("statsViewType", "day");
            model.addAttribute("statistics", showtimeService.getRoomStatistics(LocalDate.now()));
            model.addAttribute("movieStats", showtimeService.getMovieShowtimeStats(LocalDate.now(), "day", null, null));
            return "showtime/statistics";
        }
    }

    @GetMapping("/show-date-id")
    public String getShowDateId(@RequestParam String showDate, Model model) {
        try {
            LocalDate date = LocalDate.parse(showDate);
            Long showDateId = showtimeService.getShowDateIdByDate(date);
            model.addAttribute("showDateId", showDateId);
            return "showtime/show-date-id";
        } catch (Exception e) {
            model.addAttribute("showDateId", -1L);
            return "showtime/show-date-id";
        }
    }

    @GetMapping("/suggest")
    @ResponseBody
    public String suggestNextShowtime(
            @RequestParam Long roomId,
            @RequestParam Long showDateId,
            @RequestParam Long movieId) {
        try {
            LocalTime suggestedTime = showtimeService.suggestNextShowtime(roomId, showDateId, movieId);
            return suggestedTime.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @GetMapping("/movie-duration")
    @ResponseBody
    public Integer getMovieDuration(@RequestParam Long movieId) {
        return movieRepository.findById(movieId)
                .map(movie -> movie.getDuration())
                .orElse(null);
    }

    @GetMapping("/has-showtimes")
    @ResponseBody
    public boolean hasShowtimes(
            @RequestParam Long roomId,
            @RequestParam String showDate) {
        try {
            LocalDate date = LocalDate.parse(showDate);
            return showtimeService.hasShowtimesInRoomAndDate(roomId, date);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/create")
    public String createShowtime(
            @ModelAttribute ShowtimeRequest showtimeRequest,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate date = LocalDate.now();
            if (showtimeRequest.getShowDate() == null || showtimeRequest.getScheduleTime() == null ||
                    showtimeRequest.getMovieId() == null || showtimeRequest.getRoomId() == null ||
                    showtimeRequest.getVersionId() == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
                redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
                return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
            }

            if(showtimeRequest.getShowDate().isBefore(date.plusDays(4))) {
                redirectAttributes.addFlashAttribute("error","Chỉ có thể tạo lịch chiếu bắt đầu từ ngày "+ date.plusDays(4) +". Vui lòng chọn một ngày hợp lệ!");
                redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
                return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
            }

            if(showtimeRequest.getScheduleTime().isBefore(LocalTime.of(8,0))
                    || showtimeRequest.getScheduleTime().isAfter(LocalTime.of(23,0))) {
                redirectAttributes.addFlashAttribute("error","Giờ khởi chiếu không được nhỏ hơn 8h hoặc lớn hơn 23h!");
                redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
                return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
            }

            Long showDateId = showtimeService.getShowDateIdByDate(showtimeRequest.getShowDate());
            LocalTime suggestedTime = showtimeService.suggestNextShowtime(
                    showtimeRequest.getRoomId(), showDateId, showtimeRequest.getMovieId());

            boolean hasExistingShowtimes = showtimeService.hasShowtimesInRoomAndDate(
                    showtimeRequest.getRoomId(), showtimeRequest.getShowDate());

            if (!hasExistingShowtimes) {
                LocalTime requestedTime = showtimeRequest.getScheduleTime();
                if (requestedTime.isBefore(LocalTime.of(8, 0)) || requestedTime.isAfter(LocalTime.of(9, 0))) {
                    redirectAttributes.addFlashAttribute("error", "Suất chiếu đầu tiên chỉ có thể tạo trong khoảng 8h-9h");
                    redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
                    return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
                }
            } else {
                if (!showtimeRequest.getScheduleTime().equals(suggestedTime)) {
                    redirectAttributes.addFlashAttribute("error", "Giờ chiếu không hợp lệ. Gợi ý: " + suggestedTime);
                    redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
                    return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
                }
            }

            showtimeService.createShowtime(showtimeRequest);
            redirectAttributes.addFlashAttribute("success", "Thêm suất chiếu thành công");
            return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate() +
                    "&roomId=" + showtimeRequest.getRoomId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("showtimeRequest", showtimeRequest);
            return "redirect:/admin/showtime?showDate=" + showtimeRequest.getShowDate();
        }
    }
}
