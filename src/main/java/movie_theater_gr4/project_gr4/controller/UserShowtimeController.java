package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.model.Showtime;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import movie_theater_gr4.project_gr4.repository.ShowTimeRepository;
import movie_theater_gr4.project_gr4.repository.CinemaRoomRepository;
import movie_theater_gr4.project_gr4.service.ShowtimeFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/showtime")
public class UserShowtimeController {

    @Autowired
    private ShowTimeRepository showTimeRepository;

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;

    @Autowired
    private ShowtimeFilterService showtimeFilterService;

    @GetMapping
    public String viewShowtimes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) String timeSlot,
            Model model) {
        System.out.println("viewShowtimes called with: date=" + date + ", movieId=" + movieId + ", timeSlot=" + timeSlot);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, String> errors = new HashMap<>();

        // Validate filters
        errors.putAll(showtimeFilterService.validateFilters(movieId, timeSlot));

        List<Showtime> showtimes;
        Object selectedDate;
        List<LocalDate> datesWithShowtimes;
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7); // Limit to 7 days

        // Collect all unique dates with showtimes within 7 days
        List<Showtime> allShowtimes = showTimeRepository.findByShowDate_ShowDateBetween(today, endDate);
        System.out.println("All showtimes count: " + allShowtimes.size());
        datesWithShowtimes = allShowtimes.stream()
                .map(s -> s.getShowDate().getShowDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Handle date parameter (default to today if invalid or not provided)
        try {
            if (date != null && date.equalsIgnoreCase("all")) {
                showtimes = allShowtimes;
                selectedDate = "all";
            } else {
                LocalDate parsedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : today;
                if (parsedDate.isAfter(endDate)) {
                    parsedDate = today;
                    errors.put("date", "Chỉ hiển thị suất chiếu trong 7 ngày từ hôm nay.");
                }
                showtimes = showTimeRepository.findByShowDate(parsedDate);
                selectedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            System.out.println("Showtimes after date filter: " + showtimes.size());
        } catch (DateTimeParseException e) {
            System.err.println("Date parse error: " + e.getMessage());
            errors.put("date", "Định dạng ngày không hợp lệ. Sử dụng ngày hiện tại.");
            showtimes = showTimeRepository.findByShowDate(today);
            selectedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Apply movie filter
        if (movieId != null && !movieId.isEmpty() && !errors.containsKey("movieId")) {
            try {
                Long movieIdLong = Long.parseLong(movieId);
                showtimes = showtimes.stream()
                        .filter(s -> s.getMovie() != null && s.getMovie().getMovieId().equals(movieIdLong))
                        .collect(Collectors.toList());
                System.out.println("Showtimes after movie filter: " + showtimes.size());
            } catch (NumberFormatException e) {
                System.err.println("Movie ID parse error: " + e.getMessage());
                errors.put("movieId", "ID phim không hợp lệ.");
            }
        }

        // Apply time slot filter
        if (timeSlot != null && !timeSlot.isEmpty() && !errors.containsKey("timeSlot")) {
            showtimes = showtimeFilterService.filterByTimeSlot(showtimes, timeSlot);
            System.out.println("Showtimes after time slot filter: " + showtimes.size());
        }

        // Get all rooms that have showtimes
        List<CinemaRoom> rooms = showtimes.stream()
                .filter(s -> s.getRoom() != null)
                .map(Showtime::getRoom)
                .distinct()
                .collect(Collectors.toList());

        // If no showtimes, show all rooms
        if (rooms.isEmpty()) {
            rooms = cinemaRoomRepository.findAll();
        }

        // Collect all unique time slots (sorted)
        List<String> timeSlots = showtimes.stream()
                .filter(s -> s.getSchedule() != null)
                .map(s -> s.getSchedule().getScheduleTime().format(timeFormatter))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Get all movies that have showtimes
        List<Long> movieIds = showtimes.stream()
                .filter(s -> s.getMovie() != null)
                .map(s -> s.getMovie().getMovieId())
                .distinct()
                .collect(Collectors.toList());

        // Group showtimes by movie
        Map<Long, Map<String, Object>> movieMap = new HashMap<>();
        for (Long movieIdItem : movieIds) {
            Map<String, Object> movieData = new HashMap<>();
            Showtime firstShowtime = showtimes.stream()
                    .filter(s -> s.getMovie() != null && s.getMovie().getMovieId().equals(movieIdItem))
                    .findFirst().orElse(null);
            if (firstShowtime == null) continue;

            movieData.put("id", movieIdItem);
            movieData.put("title", firstShowtime.getMovie().getMovieName());
            movieData.put("duration", firstShowtime.getMovie().getDuration());

            // Map: roomId-time -> showtime
            Map<String, Map<String, Object>> showtimeMap = new HashMap<>();
            for (Showtime s : showtimes) {
                if (s.getMovie() != null && s.getRoom() != null && s.getSchedule() != null && s.getMovie().getMovieId().equals(movieIdItem)) {
                    String timeStr = s.getSchedule().getScheduleTime().format(timeFormatter);
                    String key = s.getRoom().getRoomId() + "-" + timeStr;
                    Map<String, Object> showtimeProps = new HashMap<>();
                    showtimeProps.put("id", s.getId());
                    showtimeProps.put("version", s.getVersion());
                    showtimeMap.put(key, showtimeProps);
                }
            }
            movieData.put("showtimes", showtimeMap);
            movieMap.put(movieIdItem, movieData);
        }

        // Get all unique movies for filter dropdown
        List<Showtime> allShowtimesForFilters = (date != null && !date.equalsIgnoreCase("all"))
                ? showTimeRepository.findByShowDate(date != null ? LocalDate.parse(date) : LocalDate.now())
                : allShowtimes;

        Map<Long, String> allMoviesMap = allShowtimesForFilters.stream()
                .filter(s -> s.getMovie() != null)
                .collect(Collectors.toMap(
                        s -> s.getMovie().getMovieId(),
                        s -> s.getMovie().getMovieName(),
                        (existing, replacement) -> existing
                ));

        model.addAttribute("movies", movieMap.values());
        model.addAttribute("allMovies", allMoviesMap);
        model.addAttribute("rooms", rooms);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedMovieId", movieId);
        model.addAttribute("selectedTimeSlot", timeSlot);
        model.addAttribute("datesWithShowtimes", datesWithShowtimes);
        model.addAttribute("errors", errors.isEmpty() ? null : errors);

        return "showtime";
    }

    @GetMapping("/filter")
    @ResponseBody
    public Map<String, Object> getFilteredShowtimes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) String timeSlot) {
        System.out.println("getFilteredShowtimes called with: date=" + date + ", movieId=" + movieId + ", timeSlot=" + timeSlot);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = showtimeFilterService.validateFilters(movieId, timeSlot);

        try {
            List<Showtime> showtimes;
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(7); // Limit to 7 days

            if (date != null && date.equalsIgnoreCase("all")) {
                showtimes = showTimeRepository.findByShowDate_ShowDateBetween(today, endDate);
            } else {
                LocalDate parsedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
                showtimes = showTimeRepository.findByShowDate(parsedDate);
            }
            System.out.println("Initial showtimes count: " + showtimes.size());

            // Apply filters
            if (movieId != null && !movieId.isEmpty() && !errors.containsKey("movieId")) {
                try {
                    Long movieIdLong = Long.parseLong(movieId);
                    showtimes = showtimes.stream()
                            .filter(s -> s.getMovie() != null && s.getMovie().getMovieId().equals(movieIdLong))
                            .collect(Collectors.toList());
                    System.out.println("After movie filter, showtimes count: " + showtimes.size());
                } catch (NumberFormatException e) {
                    System.err.println("Movie ID parse error: " + e.getMessage());
                    errors.put("movieId", "ID phim không hợp lệ.");
                }
            }

            if (timeSlot != null && !timeSlot.isEmpty() && !errors.containsKey("timeSlot")) {
                showtimes = showtimeFilterService.filterByTimeSlot(showtimes, timeSlot);
                System.out.println("After time slot filter, showtimes count: " + showtimes.size());
            }

            // Prepare response data
            List<Map<String, Object>> showtimeData = showtimes.stream()
                    .filter(s -> s.getMovie() != null && s.getRoom() != null && s.getSchedule() != null)
                    .map(showtime -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", showtime.getId());
                        data.put("movieId", showtime.getMovie().getMovieId());
                        data.put("movieName", showtime.getMovie().getMovieName());
                        data.put("roomId", showtime.getRoom().getRoomId());
                        data.put("roomName", showtime.getRoom().getRoomName());
                        data.put("roomType", showtime.getRoom().getType());
                        data.put("scheduleTime", showtime.getSchedule().getScheduleTime().format(timeFormatter));
                        data.put("versionId", showtime.getVersion().getVersionId());
                        data.put("versionName", showtime.getVersion().getVersionName());
                        data.put("duration", showtime.getMovie().getDuration());
                        return data;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("showtimes", showtimeData);
            response.put("count", showtimes.size());
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            System.out.println("Final showtimes count in response: " + showtimeData.size());

        } catch (Exception e) {
            System.err.println("Error in getFilteredShowtimes: " + e.getMessage());
            response.put("success", false);
            response.put("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
            response.put("errors", errors);
        }

        return response;
    }
}