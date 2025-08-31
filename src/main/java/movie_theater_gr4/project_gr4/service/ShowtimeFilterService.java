package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.model.Showtime;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowtimeFilterService {

    public enum TimeSlot {
        MORNING("morning", 8, 13, "Sáng"),
        AFTERNOON("afternoon", 13, 18, "Chiều"),
        EVENING("evening", 18, 23, "Tối");

        private final String code;
        private final int startHour;
        private final int endHour;
        private final String displayName;

        TimeSlot(String code, int startHour, int endHour, String displayName) {
            this.code = code;
            this.startHour = startHour;
            this.endHour = endHour;
            this.displayName = displayName;
        }

        public String getCode() { return code; }
        public int getStartHour() { return startHour; }
        public int getEndHour() { return endHour; }
        public String getDisplayName() { return displayName; }

        public static TimeSlot fromCode(String code) {
            if (code == null) return null;
            for (TimeSlot slot : values()) {
                if (slot.code.equalsIgnoreCase(code)) {
                    return slot;
                }
            }
            return null;
        }
    }

    public List<Showtime> filterByMovie(List<Showtime> showtimes, Long movieId) {
        if (movieId == null || showtimes == null) {
            return showtimes != null ? showtimes : Collections.emptyList();
        }
        return showtimes.stream()
                .filter(s -> s.getMovie() != null && s.getMovie().getMovieId().equals(movieId))
                .collect(Collectors.toList());
    }

    public List<Showtime> filterByTimeSlot(List<Showtime> showtimes, String timeSlotCode) {
        if (timeSlotCode == null || showtimes == null) {
            return showtimes != null ? showtimes : Collections.emptyList();
        }
        TimeSlot timeSlot = TimeSlot.fromCode(timeSlotCode);
        if (timeSlot == null) {
            return showtimes;
        }
        return showtimes.stream()
                .filter(s -> s.getSchedule() != null && isTimeInSlot(s.getSchedule().getScheduleTime(), timeSlot))
                .collect(Collectors.toList());
    }

    public List<Showtime> applyFilters(List<Showtime> showtimes, Long movieId, String timeSlotCode) {
        if (showtimes == null) {
            return Collections.emptyList();
        }
        List<Showtime> filtered = showtimes;
        filtered = filterByMovie(filtered, movieId);
        filtered = filterByTimeSlot(filtered, timeSlotCode);
        return filtered;
    }

    private boolean isTimeInSlot(LocalTime time, TimeSlot timeSlot) {
        if (time == null || timeSlot == null) {
            return false;
        }
        int hour = time.getHour();
        return hour >= timeSlot.getStartHour() && hour < timeSlot.getEndHour();
    }

    public Map<String, Object> getFilterStatistics(List<Showtime> allShowtimes, List<Showtime> filteredShowtimes) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShowtimes", allShowtimes != null ? allShowtimes.size() : 0);
        stats.put("filteredShowtimes", filteredShowtimes != null ? filteredShowtimes.size() : 0);
        stats.put("filterPercentage",
                allShowtimes != null && !allShowtimes.isEmpty() ?
                        Math.round((double) filteredShowtimes.size() / allShowtimes.size() * 100) : 0);

        Map<String, Long> timeSlotStats = new HashMap<>();
        for (TimeSlot slot : TimeSlot.values()) {
            long count = filteredShowtimes != null ? filteredShowtimes.stream()
                    .filter(s -> s.getSchedule() != null && isTimeInSlot(s.getSchedule().getScheduleTime(), slot))
                    .count() : 0;
            timeSlotStats.put(slot.getDisplayName(), count);
        }
        stats.put("timeSlotDistribution", timeSlotStats);

        Map<String, Long> movieStats = filteredShowtimes != null ? filteredShowtimes.stream()
                .filter(s -> s.getMovie() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getMovie().getMovieName(),
                        Collectors.counting()
                )) : new HashMap<>();
        stats.put("movieDistribution", movieStats);

        return stats;
    }

    public Map<String, String> getTimeSlotOptions() {
        Map<String, String> options = new HashMap<>();
        for (TimeSlot slot : TimeSlot.values()) {
            String label = String.format("%s (%02d:00 - %02d:00)",
                    slot.getDisplayName(),
                    slot.getStartHour(),
                    slot.getEndHour());
            options.put(slot.getCode(), label);
        }
        return options;
    }

    public Map<String, String> validateFilters(String movieId, String timeSlot) {
        Map<String, String> errors = new HashMap<>();
        if (movieId != null && !movieId.isEmpty()) {
            try {
                Long.parseLong(movieId);
            } catch (NumberFormatException e) {
                errors.put("movieId", "Định dạng ID phim không hợp lệ");
            }
        }
        if (timeSlot != null && !timeSlot.isEmpty() && TimeSlot.fromCode(timeSlot) == null) {
            errors.put("timeSlot", "Khung giờ không hợp lệ");
        }
        return errors;
    }

    public String buildFilterUrl(String baseUrl, String date, Long movieId, String timeSlot) {
        StringBuilder url = new StringBuilder(baseUrl);
        boolean hasParams = false;

        if (date != null && !date.isEmpty()) {
            url.append("?date=").append(date);
            hasParams = true;
        }

        if (movieId != null) {
            url.append(hasParams ? "&" : "?").append("movieId=").append(movieId);
            hasParams = true;
        }

        if (timeSlot != null && !timeSlot.isEmpty()) {
            url.append(hasParams ? "&" : "?").append("timeSlot=").append(timeSlot);
        }

        return url.toString();
    }
}