package movie_theater_gr4.project_gr4.employee.repository;

import movie_theater_gr4.project_gr4.employee.dto.BookingInfoDTO;
import movie_theater_gr4.project_gr4.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectMovieRepository extends JpaRepository<Showtime, Long> {

    // Truy vấn tất cả thông tin showtime (giống getAllShowtimeMovieData)
    @Query(value = """
        SELECT 
            s.showtime_id,
            s.movie_id,
            s.show_date_id,
            s.schedule_id,
            s.room_id,
            s.version_id,
            s.available_seats,

            v.version_name AS version_name,
            v.description AS version_description,

            sd.show_date,

            m.movie_name,
            m.movie_name_en,
            m.movie_name_vn,
            m.director,
            m.actor,
            m.content,
            m.duration,
            m.production_company,
            m.rating_code,
            m.from_date,
            m.to_date,
            m.large_image_url,
            m.small_image_url,
            m.trailer_url,

            mar.rating_name,
            mar.description AS rating_description,
            s2.schedule_time,
            r.room_name
        FROM public.showtime s
        JOIN public.schedule s2 ON s.schedule_id = s2.schedule_id
        LEFT JOIN public.version v ON s.version_id = v.version_id
        LEFT JOIN public.show_dates sd ON s.show_date_id = sd.show_date_id
        LEFT JOIN public.movie m ON s.movie_id = m.movie_id
        LEFT JOIN public.movie_age_rating mar ON m.rating_code = mar.rating_code
        LEFT JOIN public.cinema_room r ON s.room_id = r.room_id
        """, nativeQuery = true)
    List<Object[]> getAllShowtimeMovieData();


    // Truy vấn thông tin showtime theo movie_id và showtime_id
    @Query(value = """
        SELECT 
            s.showtime_id,
            s.movie_id,
            s.show_date_id,
            s.schedule_id,
            s.room_id,
            s.version_id,
            s.available_seats,

            v.version_name AS version_name,
            v.description AS version_description,

            sd.show_date,
            m.movie_name,
            m.movie_name_en,
            m.movie_name_vn,
            m.director,
            m.actor,
            m.content,
            m.duration,
            m.production_company,
            m.rating_code,
            m.from_date,
            m.to_date,
            m.large_image_url,
            m.small_image_url,
            m.trailer_url,
            mar.rating_name,
            mar.description AS rating_description,
            s2.schedule_time,
            r.room_name
        FROM public.showtime s
        JOIN public.schedule s2 ON s.schedule_id = s2.schedule_id
        LEFT JOIN public.version v ON s.version_id = v.version_id
        LEFT JOIN public.show_dates sd ON s.show_date_id = sd.show_date_id
        LEFT JOIN public.movie m ON s.movie_id = m.movie_id
        LEFT JOIN public.movie_age_rating mar ON m.rating_code = mar.rating_code
        LEFT JOIN public.cinema_room r ON s.room_id = r.room_id
        WHERE s.movie_id = :movie_id AND s.showtime_id = :showtime_id
        """, nativeQuery = true)
    List<Object[]> getInfoShowtimeByMovieIdAndShowtimeId(Long movie_id, Long showtime_id);

    @Query(value = """
             SELECT\s
                s.seat_id,
                s.seat_row,
                s.seat_column,
                s.is_active,
                st.seat_type_id,
                st.type_name,
                cr.room_id,
                cr.room_name,
                cr.seat_quantity,
                cr.type,
                cr.status,
                ss.schedule_seat_id,
                s.seat_price,
                ss.status,
                sh.showtime_id,
                 CASE WHEN ss.status = 0 THEN  false ELSE true END AS is_booked
            FROM public.showtime sh
            JOIN public.cinema_room cr ON sh.room_id = cr.room_id
            JOIN public.seat s ON s.room_id = cr.room_id
            LEFT JOIN public.schedule_seat ss\s
                   ON ss.showtime_id = sh.showtime_id AND ss.seat_id = s.seat_id
            JOIN public.seat_type st ON s.seat_type_id = st.seat_type_id
            JOIN public.movie m ON m.movie_id = sh.movie_id
            WHERE sh.showtime_id = :showtime_id
              AND sh.movie_id = :movieId
            
            """, nativeQuery = true)
    List<Object[]> findSeatSelectionInfo(@Param("showtime_id") Long showtime_id,
                                         @Param("movieId") Long movieId);

    @Query(value = """
        SELECT 
            i.movie_name AS movieName,
            v.version_name AS versionName,
            s2.schedule_time AS scheduleTime,
            cr.room_name AS roomName,
            sd.show_date AS showDate
        FROM public.invoice i
            JOIN public.ticket t ON t.invoice_id = i.invoice_id
            JOIN public.schedule_seat ss ON t.schedule_seat_id = ss.schedule_seat_id
            JOIN public.showtime s ON ss.showtime_id = s.showtime_id
            JOIN public."version" v ON s.version_id = v.version_id
            JOIN public.schedule s2 ON s.schedule_id = s2.schedule_id
            JOIN public.cinema_room cr ON s.room_id = cr.room_id
            JOIN public.show_dates sd ON s.show_date_id = sd.show_date_id
        WHERE i.invoice_id = :invoiceId
            limit 1
            
    """, nativeQuery = true)
    List<BookingInfoDTO> findBookingInfoByInvoiceId(@Param("invoiceId") Long invoiceId);

}
