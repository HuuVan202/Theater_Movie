package movie_theater_gr4.project_gr4.repository;
import movie_theater_gr4.project_gr4.dto.*;
import movie_theater_gr4.project_gr4.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface StatisticRepository extends JpaRepository<Invoice, Long>{
    // Lấy doanh thu theo 12 tháng của 1 năm cụ thể
    @Query(value = "SELECT TO_CHAR(i.booking_date, 'MM') AS month, " +
            "SUM(i.total_amount) AS totalRevenue " +
            "FROM invoice i " +
            "WHERE i.status = 1 AND TO_CHAR(i.booking_date, 'YYYY') = :year " +
            "GROUP BY TO_CHAR(i.booking_date, 'MM') " +
            "ORDER BY month", nativeQuery = true)
    List<RevenueStatsDTO> getRevenueByMonth(@Param("year") String year);

    // Lấy doanh thu từ năm 2023 đến hiện tại theo từng năm
    @Query(value = "SELECT TO_CHAR(i.booking_date, 'YYYY') AS year, " +
            "SUM(i.total_amount) AS totalRevenue " +
            "FROM invoice i " +
            "WHERE i.status = 1 AND TO_CHAR(i.booking_date, 'YYYY') >= '2023' " +
            "GROUP BY TO_CHAR(i.booking_date, 'YYYY') " +
            "ORDER BY year", nativeQuery = true)
    List<RevenueStatsDTO> getRevenueByYearFrom2023();

    @Query(value = "SELECT m.movie_name,\n" +
            "       SUM(t.price) as total_revenue,\n" +
            "       COUNT(t.ticket_id) as ticket_count,\n" +
            "       COUNT(DISTINCT st.showtime_id) as showtime_count\n" +
            "FROM invoice i\n" +
            "JOIN ticket t ON i.invoice_id = t.invoice_id\n" +
            "JOIN schedule_seat ss ON t.schedule_seat_id = ss.schedule_seat_id\n" +
            "JOIN showtime st ON ss.showtime_id = st.showtime_id\n" +
            "JOIN movie m ON st.movie_id = m.movie_id\n" +
            "WHERE i.status = 1 AND DATE(i.booking_date) = CURRENT_DATE\n" +
            "GROUP BY m.movie_name\n" +
            "ORDER BY SUM(t.price) DESC;", nativeQuery = true)
    List<Object[]> getDailyRevenueStatsRaw();

    @Query(value = "SELECT TO_CHAR(i.booking_date, 'YYYY-MM-DD') AS date, " +
            "SUM(i.total_amount) AS totalRevenue " +
            "FROM invoice i " +
            "WHERE i.status = CAST(1 AS numeric) " +
            "AND EXTRACT(YEAR FROM i.booking_date) = CAST(:year AS numeric) " +
            "AND EXTRACT(MONTH FROM i.booking_date) = CAST(:month AS numeric) " +
            "GROUP BY TO_CHAR(i.booking_date, 'YYYY-MM-DD') " +
            "ORDER BY date", nativeQuery = true)
    List<RevenueStatsDTO> getRevenueByMonthYear(@Param("year") String year, @Param("month") String month);


        @Query(value = "WITH all_quarters AS (\n" +
                "    SELECT 1 AS quarter_num UNION ALL\n" +
                "    SELECT 2 AS quarter_num UNION ALL\n" +
                "    SELECT 3 AS quarter_num UNION ALL\n" +
                "    SELECT 4 AS quarter_num\n" +
                ")\n" +
                "SELECT\n" +
                "    CONCAT('Q', aq.quarter_num, ' - ', :year) AS quarter,\n" +
                "    COALESCE(SUM(i.total_amount), 0) AS totalRevenue\n" +
                "FROM all_quarters aq\n" +
                "LEFT JOIN invoice i\n" +
                "    ON TO_CHAR(i.booking_date, 'Q') = CAST(aq.quarter_num AS TEXT)\n" +
                "    AND TO_CHAR(i.booking_date, 'YYYY') = :year\n" +
                "    AND i.status = 1\n" +
                "GROUP BY aq.quarter_num\n" +
                "ORDER BY aq.quarter_num", nativeQuery = true)
    List<RevenueStatsDTO> getRevenueByQuarter(@Param("year") String year, @Param("quarter") String quarter);

    @Query(value = """
    SELECT t.type_name AS genreName, 
           COALESCE(SUM(tkt.price), 0) AS totalRevenue 
    FROM type t 
    JOIN movie_type mt ON t.type_id = mt.type_id 
    JOIN movie m ON mt.movie_id = m.movie_id 
    JOIN showtime s ON m.movie_id = s.movie_id 
    LEFT JOIN schedule_seat ss ON s.showtime_id = ss.showtime_id 
    LEFT JOIN ticket tkt ON ss.schedule_seat_id = tkt.schedule_seat_id 
    LEFT JOIN invoice i ON tkt.invoice_id = i.invoice_id AND i.status = 1
    GROUP BY t.type_name 
    ORDER BY totalRevenue DESC
    """, nativeQuery = true)
    List<GenreRevenueDTO> findGenreRevenueStats();

    @Query(value = """
    SELECT time_slots.timeSlot, COALESCE(SUM(revenue_data.ticket_price), 0) AS totalRevenue
    FROM (
        SELECT '08:00-11:00' AS timeSlot
        UNION ALL SELECT '11:00-14:00'
        UNION ALL SELECT '14:00-17:00'
        UNION ALL SELECT '17:00-20:00'
        UNION ALL SELECT '20:00-23:00'
    ) time_slots
    LEFT JOIN (
        SELECT CASE
            WHEN EXTRACT(HOUR FROM s.schedule_time) BETWEEN 8 AND 10 THEN '08:00-11:00'
            WHEN EXTRACT(HOUR FROM s.schedule_time) BETWEEN 11 AND 13 THEN '11:00-14:00'
            WHEN EXTRACT(HOUR FROM s.schedule_time) BETWEEN 14 AND 16 THEN '14:00-17:00'
            WHEN EXTRACT(HOUR FROM s.schedule_time) BETWEEN 17 AND 19 THEN '17:00-20:00'
            WHEN EXTRACT(HOUR FROM s.schedule_time) BETWEEN 20 AND 22 THEN '20:00-23:00'
        END AS timeSlot, t.price AS ticket_price
        FROM schedule s
        JOIN showtime st ON s.schedule_id = st.schedule_id
        JOIN schedule_seat ss ON st.showtime_id = ss.showtime_id
        JOIN ticket t ON ss.schedule_seat_id = t.schedule_seat_id
        JOIN invoice i ON t.invoice_id = i.invoice_id
        WHERE i.status = 1
        AND EXTRACT(HOUR FROM s.schedule_time) BETWEEN 8 AND 22
    ) revenue_data ON time_slots.timeSlot = revenue_data.timeSlot
    GROUP BY time_slots.timeSlot
    """, nativeQuery = true)
    List<PeakHourRevenueDTO> findPeakHourRevenueStats();

    @Query(value = """
    SELECT 
        m.movie_name AS movieName, 
        COUNT(t.ticket_id) AS preBookedTickets, 
        SUM(t.price) AS totalRevenue 
    FROM 
        ticket t 
    JOIN invoice i ON t.invoice_id = i.invoice_id 
    JOIN schedule_seat ss ON t.schedule_seat_id = ss.schedule_seat_id 
    JOIN showtime s ON ss.showtime_id = s.showtime_id 
    JOIN show_dates sd ON s.show_date_id = sd.show_date_id 
    JOIN movie m ON s.movie_id = m.movie_id 
    WHERE 
        DATE(i.booking_date) < sd.show_date 
        AND i.status = 1 
    GROUP BY 
        m.movie_name 
    ORDER BY 
        m.movie_name
    """, nativeQuery = true)
    List<PreBookedTicketStatsDTO> findPreBookedTicketStats();

    @Query(value = "SELECT a.username AS username, " +
            "SUM(invoice_stats.ticket_count) AS ticketCount, " +
            "SUM(invoice_stats.total_amount) AS totalSpent " +
            "FROM account a " +
            "JOIN ( " +
            "    SELECT i.account_id, i.total_amount, COUNT(t.ticket_id) as ticket_count " +
            "    FROM invoice i " +
            "    JOIN ticket t ON i.invoice_id = t.invoice_id " +
            "    WHERE i.status = 1 " +
            "    GROUP BY i.invoice_id, i.account_id, i.total_amount " +
            ") invoice_stats ON a.account_id = invoice_stats.account_id " +
            "GROUP BY a.username " +
            "ORDER BY ticketCount DESC, totalSpent DESC " +
            "LIMIT 10", nativeQuery = true)
    List<TopCustomerStatsDTO> findTopCustomerStats(); // Trả về danh sách top 5

    @Query(value = """
    SELECT\s
                                             movie_stats.movieName,
                                             genre_data.genreNames,
                                             movie_stats.ticketsSold,
                                             movie_stats.showCount,
                                             movie_stats.preBookedTickets,
                                             movie_stats.totalRevenue
                                         FROM (
                                             SELECT\s
                                                 m.movie_name AS movieName,
                                                 CASE\s
                                                     WHEN COUNT(DISTINCT CASE WHEN DATE(i.booking_date) >= sd.show_date THEN tkt.ticket_id END) = 0
                                                     THEN 0
                                                     ELSE COUNT(DISTINCT CASE WHEN DATE(i.booking_date) >= sd.show_date THEN tkt.ticket_id END)
                                                 END AS ticketsSold,
                                                 COUNT(DISTINCT s.showtime_id) AS showCount,
                                                 COUNT(DISTINCT CASE\s
                                                     WHEN DATE(i.booking_date) < sd.show_date THEN tkt.ticket_id
                                                 END) AS preBookedTickets,
                                                 SUM(tkt.price) AS totalRevenue
                                             FROM\s
                                                 movie m
                                             JOIN showtime s ON m.movie_id = s.movie_id
                                             JOIN show_dates sd ON s.show_date_id = sd.show_date_id
                                             JOIN schedule_seat ss ON s.showtime_id = ss.showtime_id
                                             JOIN ticket tkt ON ss.schedule_seat_id = tkt.schedule_seat_id
                                             JOIN invoice i ON tkt.invoice_id = i.invoice_id
                                             WHERE\s
                                                 i.status = 1
                                                 AND DATE(i.booking_date) BETWEEN CAST(? AS DATE) AND CAST(? AS DATE)
                                             GROUP BY m.movie_name, m.movie_id
                                         ) movie_stats
                                         JOIN (
                                             SELECT\s
                                                 m.movie_name,
                                                 STRING_AGG(DISTINCT t.type_name, ', ') AS genreNames
                                             FROM movie m
                                             JOIN movie_type mt ON m.movie_id = mt.movie_id
                                             JOIN type t ON mt.type_id = t.type_id
                                             GROUP BY m.movie_name
                                         ) genre_data ON movie_stats.movieName = genre_data.movie_name
                                         ORDER BY movie_stats.ticketsSold DESC;
                                         
""", nativeQuery = true)
    List<Object[]> getMovieExcelStats(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // Cho 'day' (nhóm theo ngày)
    @Query(value = "SELECT TO_CHAR(i.booking_date, 'YYYY-MM-DD') AS label, SUM(i.total_amount) " +
            "FROM invoice i WHERE i.status = 1 AND i.booking_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getRevenueGroupedByDay(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Cho 'month' (nhóm theo tháng)
    @Query(value = "SELECT TO_CHAR(i.booking_date, 'YYYY-MM') AS label, SUM(i.total_amount) " +
            "FROM invoice i WHERE i.status = 1 AND i.booking_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getRevenueGroupedByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Cho 'year' (nhóm theo năm)
    @Query(value = "SELECT TO_CHAR(i.booking_date, 'YYYY') AS label, SUM(i.total_amount) " +
            "FROM invoice i WHERE i.status = 1 AND i.booking_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getRevenueGroupedByYear(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Cho 'week' (chia theo tuần trong tháng)
    @Query(value = "SELECT 'Tuân ' || FLOOR((EXTRACT(DAY FROM i.booking_date) - 1) / 7) + 1 AS label, SUM(i.total_amount) " +
            "FROM invoice i WHERE i.status = 1 AND i.booking_date BETWEEN :start AND :end " +
            "GROUP BY label ORDER BY label", nativeQuery = true)
    List<Object[]> getRevenueGroupedByWeek(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
