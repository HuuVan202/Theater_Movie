-- Xóa các bảng nếu tồn tại
DROP TABLE IF EXISTS public.message CASCADE;
DROP TABLE IF EXISTS public.dialogue CASCADE;
DROP TABLE IF EXISTS public.invoice_promotion CASCADE;
DROP TABLE IF EXISTS public.ticket CASCADE;
DROP TABLE IF EXISTS public.schedule_seat CASCADE;
DROP TABLE IF EXISTS public.showtime CASCADE;
DROP TABLE IF EXISTS public.movie_promotion CASCADE;
DROP TABLE IF EXISTS public.movie_version CASCADE;
DROP TABLE IF EXISTS public.movie_type CASCADE;
DROP TABLE IF EXISTS public.movie_review CASCADE;
DROP TABLE IF EXISTS public.invoice CASCADE;
DROP TABLE IF EXISTS public.promotion CASCADE;
DROP TABLE IF EXISTS public.version CASCADE;
DROP TABLE IF EXISTS public.type CASCADE;
DROP TABLE IF EXISTS public.seat CASCADE;
DROP TABLE IF EXISTS public.ticket_type CASCADE;
DROP TABLE IF EXISTS public.cinema_room CASCADE;
DROP TABLE IF EXISTS public.schedule CASCADE;
DROP TABLE IF EXISTS public.show_dates CASCADE;
DROP TABLE IF EXISTS public.movie CASCADE;
DROP TABLE IF EXISTS public.member CASCADE;
DROP TABLE IF EXISTS public.employee CASCADE;
DROP TABLE IF EXISTS public.account CASCADE;
DROP TABLE IF EXISTS public.seat_type CASCADE;
DROP TABLE IF EXISTS public.roles CASCADE;
DROP TABLE IF EXISTS public.movie_age_rating CASCADE;
DROP TABLE IF EXISTS public.notification CASCADE;

BEGIN;

-- Bảng movie_age_rating: Lưu các mã phân loại độ tuổi
CREATE TABLE IF NOT EXISTS public.movie_age_rating (
    rating_code VARCHAR(10) PRIMARY KEY,
    rating_name VARCHAR(50) NOT NULL,
    description TEXT
);

-- Bảng roles: Lưu các vai trò trong hệ thống
CREATE TABLE IF NOT EXISTS public.roles (
    role_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    role_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT roles_pkey PRIMARY KEY (role_id)
);

-- Bảng account: Lưu thông tin tài khoản người dùng
CREATE TABLE IF NOT EXISTS public.account (
    account_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    username character varying(100) COLLATE pg_catalog."default",
    password character varying(255) COLLATE pg_catalog."default",
    full_name character varying(150) COLLATE pg_catalog."default",
    email character varying(100) COLLATE pg_catalog."default",
    phone_number character varying(20) COLLATE pg_catalog."default",
    address character varying(255) COLLATE pg_catalog."default",
    date_of_birth date,
    gender character varying(1) COLLATE pg_catalog."default",
    identity_card character varying(50) COLLATE pg_catalog."default",
    register_date date DEFAULT CURRENT_DATE,
    status integer DEFAULT 1 CHECK (status IN (0, 1)),
    role_id integer NOT NULL,
    image character varying(255) COLLATE pg_catalog."default",
    is_google boolean,
    CONSTRAINT account_pkey PRIMARY KEY (account_id),
    CONSTRAINT account_email_key UNIQUE (email),
    CONSTRAINT account_username_key UNIQUE (username),
    CONSTRAINT fk_account_role FOREIGN KEY (role_id)
        REFERENCES public.roles (role_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE notification (
    notification_id SERIAL PRIMARY KEY,
    date TIMESTAMP WITHOUT TIME ZONE,
    message text,
    status BOOLEAN,
    title VARCHAR(255),
    account_id INTEGER,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

-- Bảng dialogue: Lưu thông tin các cuộc hội thoại giữa hai người dùng
CREATE TABLE IF NOT EXISTS public.dialogue (
    dialogue_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    user1_id bigint NOT NULL,
    user2_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    last_message_at timestamp without time zone,
    CONSTRAINT dialogue_pkey PRIMARY KEY (dialogue_id),
    CONSTRAINT uc_dialogue_users UNIQUE (user1_id, user2_id),
    CONSTRAINT fk_dialogue_user1 FOREIGN KEY (user1_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_dialogue_user2 FOREIGN KEY (user2_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng message: Lưu thông tin các tin nhắn trong cuộc hội thoại
CREATE TABLE IF NOT EXISTS public.message (
    message_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    dialogue_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    message_content text COLLATE pg_catalog."default" NOT NULL,
    sent_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_seen boolean DEFAULT false,
    sender_deleted boolean DEFAULT false,
    receiver_deleted boolean DEFAULT false,
    CONSTRAINT message_pkey PRIMARY KEY (message_id),
    CONSTRAINT fk_message_dialogue FOREIGN KEY (dialogue_id)
        REFERENCES public.dialogue (dialogue_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng cinema_room: Lưu thông tin các phòng chiếu
CREATE TABLE IF NOT EXISTS public.cinema_room (
    room_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    room_name character varying(100) COLLATE pg_catalog."default" NOT NULL UNIQUE,
    seat_quantity integer NOT NULL CHECK (seat_quantity >= 0),
    type character varying(50) COLLATE pg_catalog."default" NOT NULL CHECK (type IN ('2D', '3D', 'IMAX', '4DX')),
    status integer DEFAULT 1 CHECK (status IN (0, 1)),
    CONSTRAINT cinema_room_pkey PRIMARY KEY (room_id)
);

-- Bảng seat_type: Lưu các loại ghế (đã xóa room_id để tránh lỗi)
CREATE TABLE IF NOT EXISTS public.seat_type (
    seat_type_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    type_name character varying(50) COLLATE pg_catalog."default" NOT NULL UNIQUE,
    description character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT seat_type_pkey PRIMARY KEY (seat_type_id)
);

-- Bảng seat: Lưu thông tin ghế trong phòng chiếu
CREATE TABLE IF NOT EXISTS public.seat (
    seat_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    room_id bigint NOT NULL,
    seat_row integer NOT NULL CHECK (seat_row > 0),
    seat_column character varying(2) COLLATE pg_catalog."default" NOT NULL,
    seat_type_id integer NOT NULL,
    seat_price numeric(12,2) CHECK (seat_price >= 0),
    is_active boolean DEFAULT true,
    CONSTRAINT seat_pkey PRIMARY KEY (seat_id),
    CONSTRAINT uc_seat_room_row_column UNIQUE (room_id, seat_row, seat_column),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id)
        REFERENCES public.cinema_room (room_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_seat_type FOREIGN KEY (seat_type_id)
        REFERENCES public.seat_type (seat_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng ticket_type: Lưu các loại vé
CREATE TABLE IF NOT EXISTS public.ticket_type (
    ticket_type_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    type_name character varying(50) COLLATE pg_catalog."default" NOT NULL UNIQUE,
    description character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT ticket_type_pkey PRIMARY KEY (ticket_type_id)
);

-- Bảng version: Lưu các phiên bản phim
CREATE TABLE IF NOT EXISTS public.version (
    version_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    version_name character varying(50) COLLATE pg_catalog."default" NOT NULL UNIQUE,
    description text COLLATE pg_catalog."default",
    CONSTRAINT version_pkey PRIMARY KEY (version_id)
);

-- Bảng movie: Lưu thông tin phim, thêm NOT NULL cho to_date
CREATE TABLE IF NOT EXISTS public.movie (
    movie_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    movie_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    movie_name_en character varying(255) COLLATE pg_catalog."default",
    movie_name_vn character varying(255) COLLATE pg_catalog."default",
    director character varying(255) COLLATE pg_catalog."default",
    actor character varying(255) COLLATE pg_catalog."default",
    content text COLLATE pg_catalog."default",
    duration integer,
    production_company character varying(150) COLLATE pg_catalog."default",
    rating_code character varying(10) COLLATE pg_catalog."default",
    from_date date,
    to_date date,
    large_image_url character varying(255) COLLATE pg_catalog."default",
    small_image_url character varying(255) COLLATE pg_catalog."default",
    trailer_url character varying(255) COLLATE pg_catalog."default",
    featured integer UNIQUE,
    CONSTRAINT movie_pkey PRIMARY KEY (movie_id),
    CONSTRAINT chk_date_range CHECK (from_date <= to_date),
    CONSTRAINT fk_movie_rating FOREIGN KEY (rating_code)
        REFERENCES public.movie_age_rating (rating_code) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Bảng movie_version: Liên kết phim và phiên bản
CREATE TABLE IF NOT EXISTS public.movie_version (
    movie_id bigint NOT NULL,
    version_id bigint NOT NULL,
    CONSTRAINT pk_movie_version PRIMARY KEY (movie_id, version_id),
    CONSTRAINT fk_movie_version_movie FOREIGN KEY (movie_id)
        REFERENCES public.movie (movie_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_movie_version_version FOREIGN KEY (version_id)
        REFERENCES public.version (version_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng movie_review: Lưu đánh giá và nhận xét của người dùng
CREATE TABLE IF NOT EXISTS public.movie_review (
    review_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    movie_id bigint NOT NULL,
    account_id bigint NOT NULL,
    rating integer NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment text COLLATE pg_catalog."default",
    review_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT movie_review_pkey PRIMARY KEY (review_id),
    CONSTRAINT fk_review_movie FOREIGN KEY (movie_id)
        REFERENCES public.movie (movie_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_review_account FOREIGN KEY (account_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng type: Lưu thể loại phim
CREATE TABLE IF NOT EXISTS public.type (
    type_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    type_name character varying(255) COLLATE pg_catalog."default" NOT NULL UNIQUE,
    CONSTRAINT type_pkey PRIMARY KEY (type_id)
);

-- Bảng movie_type: Liên kết phim và thể loại
CREATE TABLE IF NOT EXISTS public.movie_type (
    movie_id bigint NOT NULL,
    type_id bigint NOT NULL,
    CONSTRAINT pk_movie_type PRIMARY KEY (movie_id, type_id),
    CONSTRAINT fk_movietype_movie FOREIGN KEY (movie_id)
        REFERENCES public.movie (movie_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_movietype_type FOREIGN KEY (type_id)
        REFERENCES public.type (type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng promotion: Lưu thông tin khuyến mãi
CREATE TABLE IF NOT EXISTS public.promotion (
    promotion_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (
        INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1
    ),
    title character varying(150) NOT NULL,
    detail text,
    discount_level numeric(5,2) CHECK (discount_level >= 0 AND discount_level <= 100),
    discount_amount numeric(12,2) CHECK (discount_amount >= 0),
    min_tickets integer CHECK (min_tickets >= 0),
    max_tickets integer CHECK (max_tickets >= min_tickets),
    day_of_week integer CHECK (day_of_week >= 1 AND day_of_week <= 7),
    ticket_type_id integer,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL,
    image_url character varying(255),
    is_active boolean DEFAULT true,
    max_usage integer CHECK (max_usage >= 0),
    CONSTRAINT promotion_pkey PRIMARY KEY (promotion_id),
    CONSTRAINT chk_promo_date CHECK (start_time < end_time),
    CONSTRAINT fk_promotion_ticket_type FOREIGN KEY (ticket_type_id)
        REFERENCES public.ticket_type (ticket_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

-- Bảng movie_promotion: Liên kết phim và khuyến mãi
CREATE TABLE IF NOT EXISTS public.movie_promotion (
    movie_id bigint NOT NULL,
    promotion_id bigint NOT NULL,
    CONSTRAINT pk_movie_promotion PRIMARY KEY (movie_id, promotion_id),
    CONSTRAINT fk_mp_movie FOREIGN KEY (movie_id)
        REFERENCES public.movie (movie_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_mp_promotion FOREIGN KEY (promotion_id)
        REFERENCES public.promotion (promotion_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng show_dates: Lưu các ngày chiếu
CREATE TABLE IF NOT EXISTS public.show_dates (
    show_date_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    show_date date NOT NULL,
    CONSTRAINT show_dates_pkey PRIMARY KEY (show_date_id),
    CONSTRAINT unique_show_date UNIQUE (show_date)
);

-- Bảng schedule: Lưu các khung giờ chiếu
CREATE TABLE IF NOT EXISTS public.schedule (
    schedule_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    schedule_time time without time zone NOT NULL,
    CONSTRAINT schedule_pkey PRIMARY KEY (schedule_id)
);

-- Bảng showtime: Lưu suất chiếu
CREATE TABLE IF NOT EXISTS public.showtime (
    showtime_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    movie_id bigint NOT NULL,
    show_date_id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    room_id bigint NOT NULL,
    version_id bigint NOT NULL,
    available_seats integer CHECK (available_seats >= 0),
    CONSTRAINT showtime_pkey PRIMARY KEY (showtime_id),
    CONSTRAINT uc_showtime_unique UNIQUE (show_date_id, schedule_id, room_id),
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movie_id)
        REFERENCES public.movie (movie_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_showtime_date FOREIGN KEY (show_date_id)
        REFERENCES public.show_dates (show_date_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_showtime_schedule FOREIGN KEY (schedule_id)
        REFERENCES public.schedule (schedule_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_showtime_room FOREIGN KEY (room_id)
        REFERENCES public.cinema_room (room_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_showtime_version FOREIGN KEY (version_id)
        REFERENCES public.version (version_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng schedule_seat: Lưu thông tin ghế cho từng suất chiếu
CREATE TABLE IF NOT EXISTS public.schedule_seat (
    schedule_seat_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    showtime_id bigint NOT NULL,
    seat_id bigint NOT NULL,
    seat_price numeric(12,2) NOT NULL CHECK (seat_price >= 0),
    status integer DEFAULT 0 CHECK (status IN (0, 1, 2)),
    CONSTRAINT schedule_seat_pkey PRIMARY KEY (schedule_seat_id),
    CONSTRAINT uc_schedule_seat UNIQUE (showtime_id, seat_id),
    CONSTRAINT fk_ss_showtime FOREIGN KEY (showtime_id)
        REFERENCES public.showtime (showtime_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_ss_seat FOREIGN KEY (seat_id)
        REFERENCES public.seat (seat_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng employee: Lưu thông tin nhân viên
CREATE TABLE IF NOT EXISTS public.employee (
    employee_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    account_id bigint NOT NULL,
    hire_date date,
    position character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT employee_pkey PRIMARY KEY (employee_id),
    CONSTRAINT employee_account_id_key UNIQUE (account_id),
    CONSTRAINT fk_employee_account FOREIGN KEY (account_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng invoice: Lưu thông tin hóa đơn
CREATE TABLE IF NOT EXISTS public.invoice (
    invoice_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    account_id bigint,
    employee_id bigint,
    booking_date timestamp,
    total_amount numeric(12,2) NOT NULL CHECK (total_amount >= 0),
    payment_method character varying(50) COLLATE pg_catalog."default" CHECK (payment_method IN ('credit_card', 'online')),
    use_score integer DEFAULT 0 CHECK (use_score >= 0),
    add_score integer DEFAULT 0 CHECK (add_score >= 0),
    status integer DEFAULT 1 CHECK (status IN (-1, 0, 1)),
    movie_name character varying(255) COLLATE pg_catalog."default",
    seat_number character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT invoice_pkey PRIMARY KEY (invoice_id),
    CONSTRAINT fk_invoice_account FOREIGN KEY (account_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_invoice_employee FOREIGN KEY (employee_id)
        REFERENCES public.employee (employee_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

-- Bảng invoice_promotion: Liên kết hóa đơn và khuyến mãi
CREATE TABLE IF NOT EXISTS public.invoice_promotion (
    invoice_id bigint NOT NULL,
    promotion_id bigint NOT NULL,
    CONSTRAINT pk_invoice_promotion PRIMARY KEY (invoice_id, promotion_id),
    CONSTRAINT fk_ip_invoice FOREIGN KEY (invoice_id)
        REFERENCES public.invoice (invoice_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_ip_promotion FOREIGN KEY (promotion_id)
        REFERENCES public.promotion (promotion_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng ticket: Lưu thông tin vé
CREATE TABLE IF NOT EXISTS public.ticket (
    ticket_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    invoice_id bigint NOT NULL,
    schedule_seat_id bigint NOT NULL,
    ticket_type_id integer NOT NULL,
    price numeric(12,2) NOT NULL CHECK (price >= 0),
    CONSTRAINT ticket_pkey PRIMARY KEY (ticket_id),
    CONSTRAINT uc_ticket_unique_ss UNIQUE (schedule_seat_id),
    CONSTRAINT fk_ticket_invoice FOREIGN KEY (invoice_id)
        REFERENCES public.invoice (invoice_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket_ss FOREIGN KEY (schedule_seat_id)
        REFERENCES public.schedule_seat (schedule_seat_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket_type FOREIGN KEY (ticket_type_id)
        REFERENCES public.ticket_type (ticket_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Bảng member: Lưu thông tin thành viên
CREATE TABLE IF NOT EXISTS public.member (
    member_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    account_id bigint NOT NULL,
    score integer DEFAULT 0 CHECK (score >= 0),
    tier character varying(50) COLLATE pg_catalog."default" DEFAULT 'Bạc' CHECK (tier IN ('Bạc', 'Vàng', 'Kim cương')),
    CONSTRAINT member_pkey PRIMARY KEY (member_id),
    CONSTRAINT member_account_id_key UNIQUE (account_id),
    CONSTRAINT fk_member_account FOREIGN KEY (account_id)
        REFERENCES public.account (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Trigger để tự động cập nhật tier trong bảng member dựa trên score
CREATE OR REPLACE FUNCTION update_member_tier()
RETURNS TRIGGER AS $$
BEGIN
    NEW.tier = CASE
        WHEN NEW.score >= 5000 THEN 'Kim cương'
        WHEN NEW.score >= 1000 THEN 'Vàng'
        ELSE 'Bạc'
    END;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_member_tier
BEFORE INSERT OR UPDATE OF score
ON public.member
FOR EACH ROW
EXECUTE FUNCTION update_member_tier();

-- Trigger để tự động cập nhật available_seats trong bảng showtime
CREATE OR REPLACE FUNCTION update_available_seats()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.showtime
    SET available_seats = (
        SELECT COUNT(*)
        FROM public.schedule_seat
        WHERE showtime_id = NEW.showtime_id
        AND status = 0
    )
    WHERE showtime_id = NEW.showtime_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_available_seats
AFTER INSERT OR UPDATE OF status
ON public.schedule_seat
FOR EACH ROW
EXECUTE FUNCTION update_available_seats();

-- Trigger để kiểm tra khoảng cách 20-30 phút giữa các suất chiếu trong cùng một phòng
CREATE OR REPLACE FUNCTION check_showtime_gap()
RETURNS TRIGGER AS $$
DECLARE
    movie_duration integer;
    showtime_end timestamp;
    new_start_time timestamp;
    min_rest interval := interval '19 minutes';
    max_rest interval := interval '31 minutes';
    prev_showtime record;
    next_showtime record;
BEGIN
    SELECT duration INTO movie_duration
    FROM public.movie
    WHERE movie_id = NEW.movie_id;

    IF movie_duration IS NULL THEN
        RAISE EXCEPTION 'Phim với movie_id % không tồn tại', NEW.movie_id;
    END IF;

    SELECT (sd.show_date + s.schedule_time) INTO new_start_time
    FROM public.show_dates sd
    JOIN public.schedule s ON s.schedule_id = NEW.schedule_id
    WHERE sd.show_date_id = NEW.show_date_id;

    IF new_start_time IS NULL THEN
        RAISE EXCEPTION 'Không thể tính new_start_time: show_date_id % hoặc schedule_id % không hợp lệ', 
            NEW.show_date_id, NEW.schedule_id;
    END IF;

    showtime_end := new_start_time + (movie_duration || ' minutes')::interval;
    SELECT sd.show_date, s.schedule_time, m.duration, st.showtime_id
    INTO prev_showtime
    FROM public.showtime st
    JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id
    JOIN public.schedule s ON st.schedule_id = s.schedule_id
    JOIN public.movie m ON st.movie_id = m.movie_id
    WHERE st.room_id = NEW.room_id
    AND st.show_date_id = NEW.show_date_id
    AND (sd.show_date + s.schedule_time) < new_start_time
    ORDER BY (sd.show_date + s.schedule_time) DESC
    LIMIT 1;

    SELECT sd.show_date, s.schedule_time, m.duration, st.showtime_id
    INTO next_showtime
    FROM public.showtime st
    JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id
    JOIN public.schedule s ON st.schedule_id = s.schedule_id
    JOIN public.movie m ON st.movie_id = m.movie_id
    WHERE st.room_id = NEW.room_id
    AND st.show_date_id = NEW.show_date_id
    AND (sd.show_date + s.schedule_time) > new_start_time
    ORDER BY (sd.show_date + s.schedule_time) ASC
    LIMIT 1;

    IF prev_showtime IS NOT NULL THEN
        DECLARE
            prev_end timestamp;
            rest_interval interval;
        BEGIN
            prev_end := (prev_showtime.show_date + prev_showtime.schedule_time) + 
                        (prev_showtime.duration || ' minutes')::interval;
            rest_interval := new_start_time - prev_end;
           
            IF rest_interval < min_rest OR rest_interval > max_rest THEN
                RAISE EXCEPTION 'Khoảng nghỉ giữa suất chiếu mới và suất trước (% phút) không nằm trong khoảng 20-30 phút', 
                    EXTRACT(EPOCH FROM rest_interval) / 60;
            END IF;
            IF prev_end > new_start_time THEN
                RAISE EXCEPTION 'Suất chiếu mới xung đột với suất chiếu trước (showtime_id=%) trong phòng %', 
                    prev_showtime.showtime_id, NEW.room_id;
            END IF;
        END;
    END IF;

    IF next_showtime IS NOT NULL THEN
        DECLARE
            next_start timestamp;
            rest_interval interval;
        BEGIN
            next_start := next_showtime.show_date + next_showtime.schedule_time;
            rest_interval := next_start - showtime_end;
          
            IF rest_interval < min_rest OR rest_interval > max_rest THEN
                RAISE EXCEPTION 'Khoảng nghỉ giữa suất chiếu mới và suất sau (% phút) không nằm trong khoảng 20-30 phút', 
                    EXTRACT(EPOCH FROM rest_interval) / 60;
            END IF;
            IF showtime_end > next_start THEN
                RAISE EXCEPTION 'Suất chiếu mới xung đột với suất chiếu sau (showtime_id=%) trong phòng %', 
                    next_showtime.showtime_id, NEW.room_id;
            END IF;
        END;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_showtime_gap_trigger
BEFORE INSERT OR UPDATE ON public.showtime
FOR EACH ROW EXECUTE FUNCTION check_showtime_gap();

-- Hàm đề xuất giờ chiếu tiếp theo
CREATE OR REPLACE FUNCTION suggest_next_showtime(p_room_id bigint, p_show_date_id bigint, p_movie_id bigint)
RETURNS time AS $$
DECLARE
    v_movie_duration integer;
    v_last_end_time timestamp;
    v_suggested_time time;
    v_show_date date;
BEGIN
    SELECT duration INTO v_movie_duration
    FROM public.movie
    WHERE movie_id = p_movie_id;

    IF v_movie_duration IS NULL THEN
        RAISE EXCEPTION 'Phim với movie_id % không tồn tại', p_movie_id;
    END IF;

    SELECT show_date INTO v_show_date
    FROM public.show_dates
    WHERE show_date_id = p_show_date_id;

    IF v_show_date IS NULL THEN
        RAISE EXCEPTION 'Ngày chiếu với show_date_id % không tồn tại', p_show_date_id;
    END IF;

    SELECT MAX((sd.show_date + s.schedule_time) + (m.duration || ' minutes')::interval)
    INTO v_last_end_time
    FROM public.showtime st
    JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id
    JOIN public.schedule s ON st.schedule_id = s.schedule_id
    JOIN public.movie m ON st.movie_id = m.movie_id
    WHERE st.room_id = p_room_id
    AND st.show_date_id = p_show_date_id;

    IF v_last_end_time IS NULL THEN
        v_suggested_time := '08:00'::time;
    ELSE
        v_suggested_time := (v_last_end_time + interval '20 minutes')::time;
        v_suggested_time := date_trunc('minute', v_suggested_time) + 
                           (CASE WHEN EXTRACT(MINUTE FROM v_suggested_time)::integer % 5 != 0 
                                 THEN interval '5 minutes' - 
                                      (EXTRACT(MINUTE FROM v_suggested_time)::integer % 5 || ' minutes')::interval 
                                 ELSE interval '0 minutes' 
                            END);
    END IF;

    IF v_suggested_time > '23:00'::time THEN
        RAISE EXCEPTION 'Không thể đề xuất giờ chiếu vì vượt quá 23:00';
    END IF;

    RETURN v_suggested_time;
END;
$$ LANGUAGE plpgsql;

-- Trigger để kiểm tra version_id trong showtime
CREATE OR REPLACE FUNCTION check_showtime_version()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.movie_version mv
        WHERE mv.movie_id = NEW.movie_id
        AND mv.version_id = NEW.version_id
    ) THEN
        RAISE EXCEPTION 'Version % không hợp lệ cho phim %', NEW.version_id, NEW.movie_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_showtime_version
BEFORE INSERT OR UPDATE
ON public.showtime
FOR EACH ROW
EXECUTE FUNCTION check_showtime_version();

-- Hàm vô hiệu hóa khuyến mãi hết hạn
CREATE OR REPLACE FUNCTION deactivate_expired_promotions()
RETURNS void AS $$
BEGIN
    UPDATE public.promotion
    SET is_active = false
    WHERE end_time < NOW() AND is_active = true;
END;
$$ LANGUAGE plpgsql;

-- Trigger để kiểm tra ngày hết hạn khi chèn hoặc cập nhật promotion
CREATE OR REPLACE FUNCTION check_expiration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_time < NOW() THEN
        NEW.is_active := false;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_expiration_insert
BEFORE INSERT ON public.promotion
FOR EACH ROW EXECUTE FUNCTION check_expiration();

CREATE TRIGGER trigger_check_expiration_update
BEFORE UPDATE ON public.promotion
FOR EACH ROW EXECUTE FUNCTION check_expiration();

-- Trigger để cập nhật last_message_at trong dialogue
CREATE OR REPLACE FUNCTION update_dialogue_last_message()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.dialogue
    SET last_message_at = NEW.sent_at
    WHERE dialogue_id = NEW.dialogue_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_dialogue_last_message
AFTER INSERT ON public.message
FOR EACH ROW
EXECUTE FUNCTION update_dialogue_last_message();

CREATE OR REPLACE FUNCTION insert_schedule_seats(
    p_showtime_id bigint,
    p_room_id bigint,
    p_seat_price numeric
)
RETURNS void AS $$
BEGIN
    PERFORM -- Thay SELECT bằng PERFORM để không trả về result set
        p_showtime_id,
        s.seat_id,
        p_seat_price,
        0
    FROM public.seat s
    WHERE s.room_id = p_room_id
    AND s.is_active = true;
    
    INSERT INTO public.schedule_seat (showtime_id, seat_id, seat_price, status)
    SELECT 
        p_showtime_id,
        s.seat_id,
        p_seat_price,
        0
    FROM public.seat s
    WHERE s.room_id = p_room_id
    AND s.is_active = true;
END;
$$ LANGUAGE plpgsql;

-- Hàm kiểm tra trước khi xóa suất chiếu
CREATE OR REPLACE FUNCTION restrict_showtime_deletion()
RETURNS TRIGGER AS $$
DECLARE
    v_show_date date;
    v_current_date date := CURRENT_DATE;
    v_days_until_show integer;
    v_booked_tickets integer;
BEGIN
    SELECT show_date INTO v_show_date
    FROM public.show_dates
    WHERE show_date_id = OLD.show_date_id;

    IF v_show_date IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy ngày chiếu với show_date_id %', OLD.show_date_id;
    END IF;

    v_days_until_show := v_show_date - v_current_date;

    IF v_days_until_show < 3 THEN
        RAISE EXCEPTION 'Không thể xóa suất chiếu % vì chỉ còn % ngày đến ngày chiếu (yêu cầu ít nhất 3 ngày).', 
            OLD.showtime_id, v_days_until_show;
    END IF;

    SELECT COUNT(*) INTO v_booked_tickets
    FROM public.schedule_seat ss
    JOIN public.ticket t ON ss.schedule_seat_id = t.schedule_seat_id
    WHERE ss.showtime_id = OLD.showtime_id;

    IF v_booked_tickets > 0 THEN
        RAISE EXCEPTION 'Không thể xóa suất chiếu % vì đã có % vé được đặt.', 
            OLD.showtime_id, v_booked_tickets;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_restrict_showtime_deletion
BEFORE DELETE ON public.showtime
FOR EACH ROW
EXECUTE FUNCTION restrict_showtime_deletion();

-- Chỉ mục để tối ưu hiệu suất
CREATE INDEX IF NOT EXISTS idx_employee_account_id ON public.employee(account_id);
CREATE INDEX IF NOT EXISTS idx_member_account_id ON public.member(account_id);
CREATE INDEX IF NOT EXISTS idx_ticket_invoice ON public.ticket(invoice_id);
CREATE INDEX IF NOT EXISTS idx_ticket_schedule_seat ON public.ticket(schedule_seat_id);
CREATE INDEX IF NOT EXISTS idx_movie_review_movie ON public.movie_review(movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_review_account ON public.movie_review(account_id);
CREATE INDEX IF NOT EXISTS idx_movie_version_movie_id ON public.movie_version(movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_version_version_id ON public.movie_version(version_id);
CREATE INDEX IF NOT EXISTS idx_showtime_version_id ON public.showtime(version_id);
CREATE INDEX IF NOT EXISTS idx_movie_rating_code ON public.movie(rating_code);
CREATE INDEX IF NOT EXISTS idx_message_dialogue_id ON public.message(dialogue_id);
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON public.message(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_sent_at ON public.message(sent_at);
CREATE INDEX IF NOT EXISTS idx_cinema_room_status ON public.cinema_room(status);
CREATE INDEX IF NOT EXISTS idx_showtime_movie_id ON public.showtime(movie_id);
CREATE INDEX IF NOT EXISTS idx_promotion_ticket_type_id ON public.promotion(ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_showtime_show_date_id ON public.showtime(show_date_id);
CREATE INDEX IF NOT EXISTS idx_showtime_room_id ON public.showtime(room_id);
CREATE INDEX IF NOT EXISTS idx_invoice_account_id ON public.invoice(account_id);

COMMIT;

-- Insert dữ liệu mẫu

-- 1. movie_age_rating
INSERT INTO public.movie_age_rating (rating_code, rating_name, description) VALUES
('P', 'Mọi lứa tuổi', 'Phim phù hợp với mọi lứa tuổi'),
('K', 'Phù hợp cho trẻ em', 'Phim phù hợp cho trẻ em và gia đình, không chứa nội dung nhạy cảm'),
('T13', '13+', 'Phim dành cho người từ 13 tuổi trở lên'),
('T16', '16+', 'Phim dành cho người từ 16 tuổi trở lên'),
('T18', '18+', 'Phim dành cho người từ 18 tuổi trở lên');

-- 2. roles
INSERT INTO public.roles (role_name) VALUES
('ADMIN'),
('EMPLOYEE'),
('MEMBER');


-- 8. cinema_room
INSERT INTO public.cinema_room (room_name, seat_quantity, type, status) VALUES
('Room 1', 100, '2D', 1),
('Room 2', 80, '3D', 1),
('Room 3', 120, 'IMAX', 1),
('Room 4', 60, '4DX', 1);

-- 9. seat_type
INSERT INTO public.seat_type (type_name, description) VALUES 
('Normal', 'Standard seat with basic comfort'), 
('VIP', 'Premium seat with extra legroom'), 
('Couple', 'Double seat for couples'), 
('Empty', 'Empty');

-- 11. ticket_type
INSERT INTO public.ticket_type (type_name, description) VALUES
    ('Thường', 'Vé dành cho khách hàng thông thường'),
('Sinh viên & U22', 'Vé dành cho sinh viên, kiểm tra thẻ tại quầy');

-- 12. version
INSERT INTO public.version (version_name, description) VALUES
('Phụ đề Tiếng Việt', 'Phim có phụ đề tiếng Việt'),
('Lồng tiếng', 'Phim được lồng tiếng Việt'),
('Phụ đề Tiếng Hàn', 'Phim có phụ đề tiếng Hàn'),
('Phụ đề Tiếng Anh', 'Phim có phụ đề tiếng Anh'),
('3D', 'Phim định dạng 3D');

-- 13. movie
INSERT INTO public.movie (movie_name, movie_name_vn, movie_name_en, director, actor, content, duration, production_company, rating_code, from_date, to_date, large_image_url, small_image_url, trailer_url, featured)
VALUES
-- 1. PHIM ĐIỆN ẢNH THÁM TỬ LỪNG DANH CONAN: DƯ ẢNH CỦA ĐỘC NHÃN
('PHIM ĐIỆN ẢNH THÁM TỬ LỪNG DANH CONAN: DƯ ẢNH CỦA ĐỘC NHÃN','PHIM ĐIỆN ẢNH THÁM TỬ LỪNG DANH CONAN: DƯ ẢNH CỦA ĐỘC NHÃN','DETECTIVE CONAN: THE SHADOW OF THE ONE-EYE','Katsuya Shigehara','Minami Takayama, Wakana Yamazaki, Rikiya Koyama, Megumi Hayashibara','Trên những ngọn núi tuyết của Nagano, một vụ án bí ẩn đã đưa Conan và các thám tử quay trở lại quá khứ...',110,NULL,'K','2025-07-25',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/c/o/conan-2025-poster.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/c/o/conan-2025-poster.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11835_301_100002.mp4',NULL),
-- 2. BỘ TỨ SIÊU ĐẲNG: BƯỚC ĐI ĐẦU TIÊN
('BỘ TỨ SIÊU ĐẲNG: BƯỚC ĐI ĐẦU TIÊN','BỘ TỨ SIÊU ĐẲNG: BƯỚC ĐI ĐẦU TIÊN','THE FANTASTIC FOUR: FIRST STEPS','Matt Shakman','Pedro Pascal, Vanessa Kirby, Joseph Quinn, Ebon Moss-Bachrach, Ralph Ineson, Julia Garner, Paul Walter Hauser, John Malkovich, Natasha Lyonne, Sarah Niles','Sau một chuyến bay thám hiểm vũ trụ, bốn phi hành gia bất ngờ sở hữu năng lực siêu nhiên...',115,NULL,'T13','2025-07-25',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/c/g/cgv_350x495.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/c/g/cgv_350x495.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11804_301_100001.mp4',NULL),
-- 3. SUPERMAN
('SUPERMAN','SUPERMAN','SUPERMAN','James Gunn','David Corenswet, Rachel Brosnahan, Nicholas Hoult','Mùa hè tới đây, Warner Bros. Pictures sẽ mang “Superman” - phim điện ảnh đầu tiên của DC Studios...',130,NULL,'T13','2025-07-11',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_teaser_poster_superman_1_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_teaser_poster_superman_1_.jpg','https://www.youtube.com/embed/4I2qw8HctQ8?rel=0&showinfo=0',NULL),
-- 4. PHIM XÌ TRUM
('PHIM XÌ TRUM','PHIM XÌ TRUM','SMURFS MOVIE','Chris Miller','Rihanna, James Corden, Nick Offerman, Natasha Lyonne, Amy Sedaris, Nick Kroll, Daniel Levy, Octavia Spencer','Câu chuyện trở lại với ngôi làng Xì Trum, nơi mà mỗi ngày đều là lễ hội...',92,NULL,'P','2025-07-18',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/7/0/700x1000-smurfs.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/7/0/700x1000-smurfs.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202502/11704_301_100001.mp4',NULL),
-- 5. THẾ GIỚI KHỦNG LONG: TÁI SINH
('THẾ GIỚI KHỦNG LONG: TÁI SINH','THẾ GIỚI KHỦNG LONG: TÁI SINH','JURASSIC WORLD: REBIRTH','Gareth Edwards','Scarlett Johansson, Mahershala Ali, Jonathan Bailey','Thế Giới Khủng Long: Tái Sinh lấy bối cảnh 5 năm sau phần phim Thế Giới Khủng Long: Lãnh Địa...',134,NULL,'T13','2025-07-04',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/j/w/jw4_sjquetzart_470x700.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/j/w/jw4_sjquetzart_470x700.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202502/11703_301_100001.mp4',NULL),
-- 6. THÁM TỬ TƯ: PHÍA SAU VẾT MÁU
('THÁM TỬ TƯ: PHÍA SAU VẾT MÁU','THÁM TỬ TƯ: PHÍA SAU VẾT MÁU','BEHIND THE SHADOW','Lý Tử Tuấn, Chu Vấn Như','Cổ Thiên Lạc, Trương Thiệu Huy, Châu Tú Na, Huỳnh Hạo Nhiên','5 vụ án mạng rúng động, một "trò chơi" sinh tử đầy uẩn khúc...',103,NULL,'T18','2025-07-25',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/1/0/1080wx650h-bts.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/4/7/470wx700h-bts.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11848_301_100001.mp4',NULL),
-- 7. QUỶ ĂN HỒN
('QUỶ ĂN HỒN','QUỶ ĂN HỒN',NULL,'Chad Archibald','Ashley Greene, Shawn Ashmore, Ellie O''Brien','Sau khi tìm về ký ức tàn khốc và chứng kiến hàng loạt sự kiện tang thương...',102,NULL,'T16','2025-07-25',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/3/5/350x495qah.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/3/5/350x495qah.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11838_301_100001.mp4',NULL),
-- 8. TIẾNG ỒN QUỶ DỊ
('TIẾNG ỒN QUỶ DỊ','TIẾNG ỒN QUỶ DỊ','NOISE','Kim Soo-Jin','Lee Sun-bin, Han Soo-a, Kim Min-Seok','Sau khi dọn vào căn hộ mới, hai chị em Joo-Young và Joo-Hee liên tục bị quấy nhiễu...',94,NULL,'T18','2025-07-18',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/1/0/1080wx608h.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/1/0/1080wx608h.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11851_301_100001.mp4',NULL),
-- 9. MỘT NỬA HOÀN HẢO
('MỘT NỬA HOÀN HẢO','MỘT NỬA HOÀN HẢO','MATERIALISTS','Celine Song','Dakota Johnson, Chris Evans, Pedro Pascal','Lucy, một cô gái xinh đẹp làm công việc mai mối ở New York...',117,NULL,'T16','2025-07-04',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/3/5/350x495-materialists.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/3/5/350x495-materialists.jpg','https://www.youtube.com/embed/C_Y2nNESqSo?rel=0&showinfo=0',NULL),
-- 10. ĐÀN CÁ GỖ
('ĐÀN CÁ GỖ','ĐÀN CÁ GỖ',NULL,'Nguyễn Phạm Thành Đạt','Nguyễn Hùng, Minh Hà, Lãnh Thanh','Phim ngắn “Đàn Cá Gỗ” - cùng giải thưởng Phim ngắn xuất sắc nhất tại Cánh Diều Vàng 2024...',29,NULL,'T13','2025-07-15',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thumbnail_trailer.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thumbnail_trailer.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11824_301_100001.mp4',NULL),
-- 11. ĐIỀU ƯỚC CUỐI CÙNG
('ĐIỀU ƯỚC CUỐI CÙNG','ĐIỀU ƯỚC CUỐI CÙNG',NULL,'Đoàn Sĩ Nguyên','Avin Lu, Lý Hạo Mạnh Quỳnh, Hoàng Hà, Tiến Luật, Đinh Y Nhung, Quốc Cường, Kiều Anh, Katleen Phan Võ, Hoàng Minh Triết','Biết mình không còn sống được bao lâu vì căn bệnh ALS, Hoàng tâm sự với hai người bạn thân...',114,NULL,'T16','2025-07-04',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/_/i/_i_u_c_cu_i_c_ng_-_teaser_poster_-kh_i_chi_u_04072025_1_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/_/i/_i_u_c_cu_i_c_ng_-_teaser_poster_-kh_i_chi_u_04072025_1_.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202505/11799_301_100001.mp4',NULL),
-- 12. MÙA HÈ KINH HÃI
('MÙA HÈ KINH HÃI','MÙA HÈ KINH HÃI',NULL,'Jennifer Kaytin Robinson','Madelyn Cline, Chase Sui Wonders, Jonah Hauer-King, Tyriq Withers, Sarah Pidgeon, Billy Campbell, Gabbriette Bechtel','Khi năm người bạn vô tình gây ra một vụ tai nạn xe hơi chết người...',110,NULL,'T18','2025-07-18',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-ikwydls.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-ikwydls.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11852_301_100002.mp4',NULL),
-- 13. CON NÍT QUỶ
('CON NÍT QUỶ','CON NÍT QUỶ',NULL,'Sidharta Tata',NULL,'20 năm sau thảm kịch Jatijajar, nỗi kinh hoàng mang tên Ummu Sibyan một lần nữa trỗi dậy...',105,NULL,'T16','2025-07-18',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/_/4._pr_-_teaser_poster_-_waktu_2.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/_/4._pr_-_teaser_poster_-_waktu_2.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11847_301_100001.mp4',NULL),
-- 14. CHUYỆN MA NGHĨA ĐỊA
('CHUYỆN MA NGHĨA ĐỊA','CHUYỆN MA NGHĨA ĐỊA',NULL,'Adirek Phothong, Songsak Mongkolthong, Suttawat Settakorn, Phontharis Chotkijsadarsopon','Ruethaiphat Phatthananapaphangkorn, Rebecca Patricia Armstrong, Poompat Iam-samang, Daycha Konalo, Ninnara Delamarche, Latthgarmon Pinrojkeerathi, Phanchanokchon Phansang, Chananticha Chaipa','Bốn câu chuyện nghĩa địa mang theo bốn cơn ác mộng kinh hoàng...',116,NULL,'T18','2025-07-18',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-cmnd.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-cmnd.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11846_301_100001.mp4',NULL),
-- 15. WOLFOO & CUỘC ĐUA TAM GIỚI
('WOLFOO & CUỘC ĐUA TAM GIỚI','WOLFOO & CUỘC ĐUA TAM GIỚI',NULL,'Thơ Phan',NULL,'Wolfoo - chú sói tỷ view của Youtube, IP hoạt hình Việt nổi tiếng...',100,NULL,'P','2025-07-11',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/6/4/640_x_396-wolfoo.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/6/4/640_x_396-wolfoo.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11843_301_100001.mp4',NULL),
-- 16. MA XƯỞNG MÍA
('MA XƯỞNG MÍA','MA XƯỞNG MÍA',NULL,'Awi Suryadi','Arbani Yasiz, Ersya Aurelia, Erika Carlina','Để trang trải nợ nần, Endah cùng nhóm bạn thân phải đến làm việc thời vụ...',121,NULL,'T18','2025-07-11',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thumb-main-16-9.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thumb-main-16-9.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11840_301_100001.mp4',NULL),
-- 17. GA TỬ THẦN
('GA TỬ THẦN','GA TỬ THẦN','GHOST TRAIN','Se-woong Tak','Joo Hyun-young, Jeon Bae-soo, Choi Bo-min','Bước vào thế giới của Ghost Train, khán giả theo chân Da Gyeong...',93,NULL,'T16','2025-07-11',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h_2.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h_2.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11844_301_100002.mp4',NULL),
-- 18. MARACUDA: NHÓC QUẬY RỪNG XANH
('MARACUDA: NHÓC QUẬY RỪNG XANH','MARACUDA: NHÓC QUẬY RỪNG XANH','MYTH OF MARACUDA','Viktor Glukhushin',NULL,'Maracuda: Nhóc Quậy Rừng Xanh là hành trình quậy tưng bừng của cậu bé Maracuda...',89,NULL,'P','2025-07-11',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-maracuda.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-maracuda.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11839_301_100002.mp4',NULL),
-- 19. F1®
('F1®','F1®','F1','Joseph Kosinski','Brad Pitt, Simone Ashley, Javier Bardem','Sonny Hayes được mệnh danh là "Huyền thoại chưa từng được gọi tên"...',156,NULL,'T16','2025-06-27',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_f1_insta_vert_main_1638x2048_intl.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_f1_insta_vert_main_1638x2048_intl.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11792_301_100002.mp4',NULL),
-- 20. ÚT LAN: OÁN LINH GIỮ CỦA
('ÚT LAN: OÁN LINH GIỮ CỦA','ÚT LAN: OÁN LINH GIỮ CỦA',NULL,'Trần Trọng Dần','Quốc Trường, Mạc Văn Khoa','Sau sự ra đi của cha, Lan về một vùng quê và ở đợ cho nhà ông Danh...',111,NULL,'T18','2025-06-20',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/u/t/utlan_firtlook_simple_layers_cmyk_1_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/u/t/utlan_firtlook_simple_layers_cmyk_1_.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202505/11765_301_100001.mp4',NULL),
-- 21. BÍ KÍP LUYỆN RỒNG
('BÍ KÍP LUYỆN RỒNG','BÍ KÍP LUYỆN RỒNG','HOW TO TRAIN YOUR DRAGON','Dean DeBlois','Mason Thames, Nico Parker, Gerard Butler','Câu chuyện về một chàng trai trẻ với ước mơ trở thành thợ săn rồng...',126,NULL,'K','2025-06-13',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/h/d/hdg_payoff_470x700.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/h/d/hdg_payoff_470x700.jpg','https://www.youtube.com/embed/rvOaNwwDVZk?rel=0&showinfo=0',NULL),
-- 22. ELIO - CẬU BÉ ĐẾN TỪ TRÁI ĐẤT
('ELIO - CẬU BÉ ĐẾN TỪ TRÁI ĐẤT','ELIO - CẬU BÉ ĐẾN TỪ TRÁI ĐẤT','ELIO','Adrian Molina, Madeline Sharafian, Domee Shi','Yonas Kibreab, Zoe Saldaña, Brad Garrett','Điều gì sẽ xảy ra nếu chính thứ bạn đang tìm kiếm lại tìm đến bạn trước?...',97,NULL,'P','2025-06-27',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/e/l/elio_vn.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/e/l/elio_vn.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11793_301_100002.mp4',NULL),
-- 23. QUAN TÀI VỢ QUỶ
('QUAN TÀI VỢ QUỶ','QUAN TÀI VỢ QUỶ','TOMB WATCHER','Vathanyu Ingkawiwat','Woranuch BhiromBhakdi, Arachaporn Pokinpakorn, Thanavate Siriwattanagul','Sau khi Lunthom chết, người chồng và cô tình nhân những tưởng sẽ được hưởng khối gia sản...',91,NULL,'T18','2025-07-04',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/6/4/640x396-tomb.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/6/4/640x396-tomb.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11826_301_100001.mp4',NULL),
-- 24. 28 NĂM SAU: HẬU TẬN THẾ
('28 NĂM SAU: HẬU TẬN THẾ','28 NĂM SAU: HẬU TẬN THẾ','28 YEARS LATER','Danny Boyle','Aaron Taylor-Johnson, Ralph Fiennes, Jodie Comer, Cillian Murphy','Cơn ác mộng chưa kết thúc. Virus trở lại, kéo theo bóng tối bao trùm nước Anh...',114,NULL,'T18','2025-06-20',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/2/8/28yrs_16x9_thumbs_official_trailer_v2_1205_08_1_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/2/8/28yrs_16x9_thumbs_official_trailer_v2_1205_08_1_.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202506/11766_301_100001.mp4',NULL),
-- 25. NHIỆM VỤ: BẤT KHẢ THI - NGHIỆP BÁO CUỐI CÙNG
('NHIỆM VỤ: BẤT KHẢ THI - NGHIỆP BÁO CUỐI CÙNG','NHIỆM VỤ: BẤT KHẢ THI - NGHIỆP BÁO CUỐI CÙNG','MISSION: IMPOSSIBLE - FINAL RECKONING','Christopher McQuarrie','Tom Cruise','Cuộc đời là tất thảy những lựa chọn. Tom Cruise thủ vai Ethan Hunt trở lại...',169,NULL,'T16','2025-05-30',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/m/i/mi8_poster_470x700_1.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/m/i/mi8_poster_470x700_1.jpg','https://www.youtube.com/embed/no2HdwAX8jI?rel=0&showinfo=0',NULL),
-- 26. MANG MẸ ĐI BỎ
('MANG MẸ ĐI BỎ','MANG MẸ ĐI BỎ',NULL,'Mo Hong-jin','Hồng Đào, Tuấn Trần, Jung Il-woo, Juliet Bảo Ngọc, Quốc Khánh, Hải Triều, Lâm Vỹ Dạ, Vinh Râu','Mang Mẹ Đi Bỏ kể về số phận của Hoan - một chàng trai trẻ ngày ngày hóa thân thành “thằng hề đường phố”...',113,NULL,'K','2025-08-01',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/m/m/mmdb_1stlook_fa_lores.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/m/m/mmdb_1stlook_fa_lores.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11803_301_100001.mp4',NULL),
-- 27. TOÀN TRÍ ĐỘC GIẢ
('TOÀN TRÍ ĐỘC GIẢ','TOÀN TRÍ ĐỘC GIẢ',NULL,'KIM Byung-woo','LEE Min-ho, AHN Hyo-seop, KIM Jisoo, CHAE Soo-bin, Nana, SHIN Seung-ho','Khi thế giới diệt vong trong cuốn tiểu thuyết yêu thích bỗng biến thành hiện thực...',116,NULL,'T16','2025-08-01',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-omni.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-omni.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11845_301_100001.mp4',NULL),
-- 28. HỌNG SÚNG VÔ HÌNH
('HỌNG SÚNG VÔ HÌNH','HỌNG SÚNG VÔ HÌNH','NAKED GUN','Akiva Schaffer','Liam Neeson, Pamela Anderson, Paul Walter Hauser, CCH Pounder, Kevin Durand, Cody Rhodes, Liza Koshy, Eddie Yu, Danny Huston','Chỉ có một người đàn ông sở hữu bộ kỹ năng đặc biệt...',85,NULL,'T16','2025-08-01',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470x700-naked-gun.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470x700-naked-gun.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11857_301_100001.mp4',NULL),
-- 29. MA LÒNG THÒNG
('MA LÒNG THÒNG','MA LÒNG THÒNG',NULL,'Chiska Doppert','Andrew Barret, Bulan Sofya, Michael Russel, Annisa Aurelia, Adelia','Sau khi nhận tin cha qua đời tại làng Kidul, Ryan quyết trở về chịu tang...',NULL,NULL,NULL,'2025-08-01',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/1/0/1080wx608h-longthong.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/1/0/1080wx608h-longthong.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11859_301_100001.mp4',NULL),
-- 30. ZOMBIE CƯNG CỦA BA
('ZOMBIE CƯNG CỦA BA','ZOMBIE CƯNG CỦA BA',NULL,'Pil Gam Sung','Cho Yeo Jeong, Jo Jung Suk, Lee Yung Eun, Yoon Kyung Ho, Choi Yoo Ri','Zombie Cưng Của Ba đánh dấu sự trở lại của nam diễn viên Cho Jung Seok...',NULL,NULL,NULL,'2025-08-08',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-zombie.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-zombie.jpg','https://media.lottecinemavn.com/Media/MovieFile/MovieMedia/202507/11859_301_100001.mp4',NULL),
-- 31. THANH GƯƠM DIỆT QUỶ: VÔ HẠN THÀNH
('THANH GƯƠM DIỆT QUỶ: VÔ HẠN THÀNH','THANH GƯƠM DIỆT QUỶ: VÔ HẠN THÀNH','DEMON SLAYER: INFINITY CASTLE','Haruo Sotozaki','Natsuki Hanae, Saori Hayami, Yoshitsugu Matsuoka','Xin được phép chào sân với trận đấu đầu tiên đến từ Akaza và trận chiến tại Lâu đài Vô cực...',NULL,NULL,NULL,'2025-08-15',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thanh-guom-diet-quy_up.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thanh-guom-diet-quy_up.jpg',NULL,NULL),
-- 32. NGÀY THỨ SÁU SIÊU KỲ QUÁI
('NGÀY THỨ SÁU SIÊU KỲ QUÁI','NGÀY THỨ SÁU SIÊU KỲ QUÁI','FREAKIER FRIDAY','Nisha Ganatra','Lindsay Lohan, Jamie Lee Curtis','Sau 22 năm, phim teen đình đám một thời “Freaky Friday” sẵn sàng trở lại...',NULL,NULL,NULL,'2025-08-15',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/f/r/freakier_friday_instagram_teaser_poster_vietnam_1_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/f/r/freakier_friday_instagram_teaser_poster_vietnam_1_.jpg','https://www.youtube.com/embed/x1DfNFw2aEM?rel=0&showinfo=0',NULL),
-- 33. KẺ VÔ DANH 2
('KẺ VÔ DANH 2','KẺ VÔ DANH 2','NOBODY 2','Timo Tjahjanto','Bob Odenkirk, Connie Nielsen, John Ortiz, RZA, Colin Hanks, Christopher Lloyd, Sharon Stone','Bob Odenkirk trở lại với vai Hutch Mansell - người chồng, người cha sống ở vùng ngoại ô...',NULL,NULL,NULL,'2025-08-15',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/n/b/nb2_poolposter_470x700.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/n/b/nb2_poolposter_470x700.jpg','https://www.youtube.com/embed/rTpeRfGM23M?rel=0&showinfo=0',NULL),
-- 34. CÔ DÂU MA
('CÔ DÂU MA','CÔ DÂU MA',NULL,'Lee Thongkham','JJ Krissanapoom, Rima Thanh Vy, Công Dương, Jun Vũ','Đám cưới Việt – Thái hóa nghi thức rợn người và ám ảnh...',NULL,NULL,NULL,'2025-08-29',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/c/_/c_d_u_ma_-_teaser_poster_-_kh_i_chi_u_15082025_.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/c/_/c_d_u_ma_-_teaser_poster_-_kh_i_chi_u_15082025_.jpg','https://www.youtube.com/embed/G_Qc1ydCVNQ?rel=0&showinfo=0',NULL),
-- 35. MƯA ĐỎ
('MƯA ĐỎ','MƯA ĐỎ',NULL,'NSƯT Đặng Thái Huyền','Đỗ Nhật Hoàng, Phương Nam, Lâm Thanh Nhã, Đình Khang, Hoàng Long, Nguyễn Hùng, Trần Gia Huy, Steven Nguyễn, Hạ Anh','“Mưa Đỏ” - Phim truyện điện ảnh về đề tài chiến tranh cách mạng...',NULL,NULL,NULL,'2025-09-02',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-muado.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-muado.jpg','https://www.youtube.com/embed/-Ir9rPvqwuw?rel=0&showinfo=0',NULL),
-- 36. BĂNG ĐẢNG QUÁI KIỆT 2
('BĂNG ĐẢNG QUÁI KIỆT 2','BĂNG ĐẢNG QUÁI KIỆT 2','BAD GUYS 2','Pierre Perifel, JP Sans',NULL,'Biệt đội Bad Guys đang cố gắng lấy lại sự tin tưởng của mọi người sau khi hoàn lương...',NULL,NULL,NULL,'2025-08-29',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/b/g/bg2_montage_470x700.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/b/g/bg2_montage_470x700.jpg','https://www.youtube.com/embed/ql1mh0P6shE?rel=0&showinfo=0',NULL),
-- 37. THE CONJURING: NGHI LỄ CUỐI CÙNG
('THE CONJURING: NGHI LỄ CUỐI CÙNG','THE CONJURING: NGHI LỄ CUỐI CÙNG','THE CONJURING: LAST RITES','Michael Chaves','Vera Farmiga, Patrick Wilson, Mia Tomlinson, Ben Hardy, Tony Spera, Steve Coulter','“The Conjuring: Nghi Lễ Cuối Cùng” mang đến một chương kinh dị mới đầy kịch tính...',NULL,NULL,NULL,'2025-09-05',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-conjuring.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/4/7/470wx700h-conjuring.jpg','https://www.youtube.com/embed/aFLk3ocVtfk?rel=0&showinfo=0',NULL),
-- 38. LÀM GIÀU VỚI MA 2: CUỘC CHIẾN HỘT XOÀN
('LÀM GIÀU VỚI MA 2: CUỘC CHIẾN HỘT XOÀN','LÀM GIÀU VỚI MA 2: CUỘC CHIẾN HỘT XOÀN',NULL,'Trung Lùn','NSƯT Hoài Linh, Tuấn Trần, Diệp Bảo Ngọc, Võ Tấn Phát, Ngọc Xuân','Hành trình đầy bi hài của 5 con người với những toan tính khác nhau...',NULL,NULL,NULL,'2025-09-02',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-lgvm2.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/3/5/350x495-lgvm2.jpg','https://www.youtube.com/embed/qq4l_rficuw?rel=0&showinfo=0',NULL),
-- 39. TRON: ARES
('TRON: ARES','TRON: ARES','TRON: ARES','Joachim Rønning','Gillian Anderson, Jeff Bridges, Jared Leto','Không còn đường lùi. Tron: Ares dự kiến khởi chiếu 10.10.25.',NULL,NULL,NULL,'2025-10-10',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/r/tron_ares_-_cgv.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/r/tron_ares_-_cgv.jpg','https://www.youtube.com/embed/X8IgFEBnJQE?rel=0&showinfo=0',NULL),
-- 40. ĐIỆN THOẠI ĐEN 2
('ĐIỆN THOẠI ĐEN 2','ĐIỆN THOẠI ĐEN 2','BLACK PHONE 2','Scott Derrickson','Ethan Hawke, Mason Thames, Madeleine McGraw, Demián Bichir, Miguel Mora, Jeremy Davies, Arianna Rivas','Bốn năm trước, Finn khi mới 13 tuổi đã giết chết kẻ bắt cóc mình...',NULL,NULL,NULL,'2025-10-17',NULL,'http://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/b/p/bp2_teaser_470x700.jpg','http://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/b/p/bp2_teaser_470x700.jpg','https://www.youtube.com/embed/oSitimAlEF0?rel=0&showinfo=0',NULL),
-- 41. MORTAL KOMBAT: CUỘC CHIẾN SINH TỬ II
('MORTAL KOMBAT: CUỘC CHIẾN SINH TỬ II','MORTAL KOMBAT: CUỘC CHIẾN SINH TỬ II','MORTAL KOMBAT II','Simon McQuoid','Karl Urban, Adeline Rudolph, Jessica McNamee, Josh Lawson, Ludi Lin, Mehcad Brooks, Tati Gabrielle, Lewis Tan','Hãng phim New Line Cinema, phần tiếp theo đầy kịch tính trong loạt phim bom tấn...',NULL,NULL,NULL,'2025-10-24',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_kombat2_vert_tsr_2764x4096_intl.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/v/n/vn_kombat2_vert_tsr_2764x4096_intl.jpg','https://www.youtube.com/embed/_UYoxtVTKEI?rel=0&showinfo=0',NULL),
-- 42. PHI VỤ ĐỘNG TRỜI 2
('PHI VỤ ĐỘNG TRỜI 2','PHI VỤ ĐỘNG TRỜI 2','ZOOTOPIA 2','Jared Bush, Byron Howard','Jason Bateman, Quinta Brunson, Fortune Feimster','ZOOTOPIA 2 trở lại sau 9 năm Đuối Nick & Judy chuẩn bị 28.11.2025...',NULL,NULL,NULL,'2025-11-28',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/z/o/zootopia_2_-_teaser_poster_up.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/z/o/zootopia_2_-_teaser_poster_up.jpg','https://www.youtube.com/embed/A3r9Zptjlyc?rel=0&showinfo=0',NULL),
-- 43. WICKED: PHẦN 2
('WICKED: PHẦN 2','WICKED: PHẦN 2','WICKED: FOR GOOD','Jon M. Chu','Cynthia Erivo, Ariana Grande, Jonathan Bailey, Ethan Slater, Bowen Yang, Marissa Bode, Michelle Yeoh, Jeff Goldblum','Bộ phim chuyển thể từ sân khấu Broadway thành công nhất mọi thời đại...',NULL,NULL,NULL,'2025-11-21',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/w/k/wk2_cliffposter_700x1000.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/w/k/wk2_cliffposter_700x1000.jpg','https://www.youtube.com/embed/XVJiQLNXRRAt?rel=0&showinfo=0',NULL),
-- 44. CHÀNG MÈO MANG MŨ
('CHÀNG MÈO MANG MŨ','CHÀNG MÈO MANG MŨ','THE CAT IN THE HAT','Alessandro Carloni, Erica Rivinoja','Bill Hader, Xochitl Gomez, Matt Berry, Quinta Brunson, Paula Pell','Trong phim, Chàng Mèo Mang Mũ của chúng ta nhận nhiệm vụ khó khăn nhất từ trước đến nay...',NULL,NULL,NULL,'2026-02-27',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thecatinthehat_poster.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/h/thecatinthehat_poster.jpg','https://www.youtube.com/watch?v=SBkTtFz7v8E',NULL),
-- 45. THOÁT KHỎI TẬN THẾ
('THOÁT KHỎI TẬN THẾ','THOÁT KHỎI TẬN THẾ','PROJECT HAIL MARY','Phil Lord, Christopher Miller','Ryan Gosling, Liz Kingsman, Milana Vayntrub, Sandra Hüller','Ryland Grace tỉnh dậy trong một con tàu vũ trụ mà không hề có bất kỳ ký ức gì...',NULL,NULL,NULL,'2026-03-20',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/k/tktt_thumb_fb.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/k/tktt_thumb_fb.jpg','https://www.youtube.com/watch?v=LQ9KHDpA9vI',NULL),
-- 46. CÚ NHẢY KỲ DIỆU
('CÚ NHẢY KỲ DIỆU','CÚ NHẢY KỲ DIỆU','HOPPERS','Daniel Chong','Jon Hamm, Bobby Moynihan, Piper Curda','Nó dùng tửng mà nó dễ thương thiệt sự luôn. Ai từng mê nét hài của ba anh gấu trong...',NULL,NULL,NULL,'2026-05-22',NULL,'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/h/o/hoppers_-_poster.jpg','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/h/o/hoppers_-_poster.jpg','https://www.youtube.com/watch?v=mRSpJwMp6LM',NULL);

-- 14. movie_version
INSERT INTO public.movie_version (movie_id, version_id) VALUES
(1, 1), -- PHIM THÁM TỬ CONAN - Tiếng Nhật - Phụ đề Tiếng Việt
(1, 2), -- Lồng tiếng Việt
(2, 1), -- BỘ TỨ SIÊU ĐẲNG - Tiếng Anh - Phụ đề Tiếng Việt
(2, 2), -- Lồng tiếng Việt
(3, 1), -- SUPERMAN - Tiếng Anh - Phụ đề Tiếng Việt
(3, 3), -- Phụ đề Tiếng Hàn
(4, 1), -- PHIM XÌ TRUM - Tiếng Anh - Phụ đề
(4, 2), -- Lồng tiếng Việt
(5, 1), -- THẾ GIỚI KHỦNG LONG - Tiếng Anh - Phụ đề Tiếng Việt
(6, 2), -- THÁM TỬ TƯ - Quảng Đông - Lồng tiếng Việt
(6, 1), -- Phụ đề Việt
(6, 4), -- Phụ đề Tiếng Anh
(7, 1), -- QUỶ ĂN HỒN - Tiếng Anh - Phụ đề Tiếng Việt
(8, 1), -- TIẾNG ỒN QUỶ DỊ - Tiếng Hàn - Phụ đề Tiếng Việt
(8, 4), -- Phụ đề Tiếng Anh
(9, 1), -- MỘT NỬA HOÀN HẢO - Tiếng Anh - Phụ đề Tiếng Việt
(10, 4), -- ĐÀN CÁ GỖ - Tiếng Việt - Phụ đề Tiếng Anh
(11, 4), -- ĐIỀU ƯỚC CUỐI CÙNG - Tiếng Việt (Không cần phiên bản)
(12, 1), -- MÙA HÈ KINH HÃI - Tiếng Anh - Phụ đề Tiếng Việt
(13, 1), -- CON NÍT QUỶ - Tiếng Indonesia - Phụ đề Tiếng Việt
(14, 1), -- CHUYỆN MA NGHĨA ĐỊA - Tiếng Thái - Phụ đề Tiếng Việt
(15, 2), -- WOLFOO & CUỘC ĐUA TAM GIỚI - Lồng tiếng Việt
(16, 1), -- MA XƯỞNG MÍA - Tiếng Indonesia - Phụ đề Tiếng Việt
(16, 4), -- Phụ đề Tiếng Anh
(17, 1), -- GA TỬ THẦN - Tiếng Hàn - Phụ đề Tiếng Việt
(17, 4), -- Phụ đề Tiếng Anh
(18, 2), -- MARACUDA - Tiếng Anh - Lồng tiếng Việt
(19, 1), -- F1® - Tiếng Anh - Phụ đề Tiếng Việt
(19, 3), -- Phụ đề Tiếng Hàn
(20, 4), -- ÚT LAN - Tiếng Việt - Phụ đề Tiếng Anh
(21, 1), -- BÍ KÍP LUYỆN RỒNG - Tiếng Anh - Phụ đề
(21, 2), -- Lồng tiếng Việt
(22, 1), -- ELIO - Tiếng Anh - Phụ đề Tiếng Việt
(22, 2), -- Lồng tiếng Việt
(23, 1), -- QUAN TÀI VỢ QUỶ - Tiếng Thái - Phụ đề Tiếng Việt
(24, 1), -- 28 NĂM SAU - Tiếng Anh - Phụ đề Tiếng Việt
(25, 1), -- NHIỆM VỤ: BẤT KHẢ THI - Tiếng Anh - Phụ đề Tiếng Việt
(25, 3), -- Phụ đề Tiếng Hàn
(26, 4), -- MANG MẺ ĐI BỎ - Tiếng Việt & Tiếng Hàn - Phụ đề Tiếng Anh
(27, 1), -- TOÀN TRÍ ĐỘC GIẢ - Tiếng Hàn - Phụ đề Tiếng Việt
(28, 1), -- HỌNG SÚNG VÔ HÌNH - Tiếng Anh - Phụ đề Tiếng Việt
(29, 1), -- MA LÒNG THÒNG - Tiếng Indonesia - Phụ đề Tiếng Việt
(29, 4), -- Phụ đề Tiếng Anh
(30, 1), -- ZOMBIE CƯNG CỦA BA - Tiếng Hàn - Phụ đề Tiếng Việt
(30, 2), -- Lồng tiếng
(31, 1), -- THANH GƯƠM DIỆT QUỶ - Tiếng Nhật - Phụ đề Tiếng Việt
(31, 4), -- Phụ đề Tiếng Anh
(32, 1), -- NGÀY THỨ SÁU - Tiếng Anh - Phụ đề Tiếng Việt
(33, 1), -- KẺ VÔ DANH 2 - Tiếng Anh - Phụ đề Tiếng Việt
(34, 1), -- CÔ DÂU MA - NULL (Giả định Phụ đề Tiếng Việt)
(35, 4), -- MƯA ĐỎ - Tiếng Việt (Không cần phiên bản)
(36, 1), -- BĂNG ĐẢNG QUÁI KIỆT 2 - Tiếng Anh - Phụ đề Tiếng Việt
(36, 2), -- Lồng tiếng Việt
(37, 1), -- THE CONJURING - Tiếng Anh - Phụ đề Tiếng Việt
(38, 4), -- LÀM GIÀU VỚI MA 2 - Tiếng Việt - Phụ đề Tiếng Anh
(39, 1), -- TRON: ARES - Tiếng Anh - Phụ đề Tiếng Việt
(40, 1), -- ĐIỆN THOẠI ĐEN 2 - Tiếng Anh - Phụ đề Tiếng Việt
(41, 1), -- MORTAL KOMBAT II - Tiếng Anh - Phụ đề Tiếng Việt
(42, 1), -- PHI VỤ ĐỘNG TRỜI 2 - NULL (Giả định Phụ đề Tiếng Việt)
(43, 1), -- WICKED: PHẦN 2 - Tiếng Anh - Phụ đề Tiếng Việt
(44, 1), -- CHÀNG MÈO MANG MŨ - Tiếng Anh - Phụ đề Tiếng Việt
(45, 1), -- THOÁT KHỎI TẬN THẾ - Tiếng Anh - Phụ đề Tiếng Việt
(46, 1); -- CÚ NHẢY KỲ DIỆU - NULL (Giả định Phụ đề Tiếng Việt)

-- 15. type
INSERT INTO public.type (type_name) VALUES
('Hoạt Hình'),
('Phiêu Lưu'),
('Hành Động'),
('Hồi hộp'),
('Gia đình'),
('Hài'),
('Kinh Dị'),
('Tâm Lý'),
('Thần thoại'),
('Lịch Sử'),
('Khoa Học Viễn Tưởng'),
('Tình cảm'),
('Bí ẩn'),
('Nhạc Kịch');

--16. movie_type
INSERT INTO public.movie_type (movie_id, type_id) VALUES
-- 1. PHIM THÁM TỬ CONAN
(1, 13), -- Bí ẩn
(1, 3),  -- Hành Động
(1, 1),  -- Hoạt Hình
-- 2. BỘ TỨ SIÊU ĐẲNG
(2, 3),  -- Hành Động
(2, 11), -- Khoa Học Viễn Tưởng
(2, 2),  -- Phiêu Lưu
-- 3. SUPERMAN
(3, 3),  -- Hành Động
(3, 2),  -- Phiêu Lưu
-- 4. PHIM XÌ TRUM
(4, 5),  -- Gia đình
(4, 6),  -- Hài
(4, 1),  -- Hoạt Hình
(4, 2),  -- Phiêu Lưu
-- 5. THẾ GIỚI KHỦNG LONG
(5, 3),  -- Hành Động
(5, 2),  -- Phiêu Lưu
(5, 9),  -- Thần thoại
-- 6. THÁM TỬ TƯ
(6, 7),  -- Kinh Dị
-- 7. QUỶ ĂN HỒN
(7, 7),  -- Kinh Dị
-- 8. TIẾNG ỒN QUỶ DỊ
(8, 4),  -- Hồi hộp
(8, 7),  -- Kinh Dị
-- 9. MỘT NỬA HOÀN HẢO
(9, 12), -- Tình cảm
-- 10. ĐÀN CÁ GỖ
(10, 8), -- Tâm Lý
(10, 12),-- Tình cảm
-- 11. ĐIỀU ƯỚC CUỐI CÙNG
(11, 5), -- Gia đình
(11, 6), -- Hài
-- 12. MÙA HÈ KINH HÃI
(12, 4), -- Hồi hộp
(12, 7), -- Kinh Dị
-- 13. CON NÍT QUỶ
(13, 7), -- Kinh Dị
-- 14. CHUYỆN MA NGHĨA ĐỊA
(14, 7), -- Kinh Dị
-- 15. WOLFOO & CUỘC ĐUA TAM GIỚI
(15, 1), -- Hoạt Hình
-- 16. MA XƯỞNG MÍA
(16, 7), -- Kinh Dị
-- 17. GA TỬ THẦN
(17, 13), -- Bí ẩn
(17, 7),  -- Kinh Dị
-- 18. MARACUDA
(18, 6), -- Hài
(18, 1), -- Hoạt Hình
(18, 2), -- Phiêu Lưu
-- 19. F1
(19, 3), -- Hành Động
(19, 8), -- Tâm Lý
-- 20. ÚT LAN
(20, 7), -- Kinh Dị
-- 21. BÍ KÍP LUYỆN RỒNG
(21, 6), -- Hài
(21, 3), -- Hành Động
(21, 2), -- Phiêu Lưu
(21, 9), -- Thần thoại
-- 22. ELIO
(22, 1), -- Hoạt Hình
(22, 2), -- Phiêu Lưu
-- 23. QUAN TÀI VỢ QUỶ
(23, 7), -- Kinh Dị
-- 24. 28 NĂM SAU
(24, 4), -- Hồi hộp
(24, 7), -- Kinh Dị
-- 25. NHIỆM VỤ: BẤT KHẢ THI
(25, 3), -- Hành Động
(25, 4), -- Hồi hộp
(25, 2), -- Phiêu Lưu
-- 26. MANG MẺ ĐI BỎ
(26, 5), -- Gia đình
(26, 8), -- Tâm Lý
-- 27. TOÀN TRÍ ĐỘC GIẢ
(27, 3), -- Hành Động
(27, 9), -- Thần thoại
-- 28. HỌNG SÚNG VÔ HÌNH
(28, 6), -- Hài
(28, 3), -- Hành Động
-- 29. MA LÒNG THÒNG
(29, 7), -- Kinh Dị
-- 30. ZOMBIE CƯNG CỦA BA
(30, 6), -- Hài
(30, 7), -- Kinh Dị
-- 31. THANH GƯƠM DIỆT QUỶ
(31, 3), -- Hành Động
(31, 1), -- Hoạt Hình
(31, 2), -- Phiêu Lưu
-- 32. NGÀY THỨ SÁU SIÊU KỲ QUÁI
(32, 5), -- Gia đình
(32, 6), -- Hài
(32, 9), -- Thần thoại
-- 33. KẺ VÔ DANH 2
(33, 3), -- Hành Động
(33, 4), -- Hồi hộp
-- 34. CÔ DÂU MA
(34, 7), -- Kinh Dị
-- 35. MƯA ĐỎ
(35, 3), -- Hành Động
(35, 10),-- Lịch Sử
-- 36. BĂNG ĐẢNG QUÁI KIỆT 2
(36, 6), -- Hài
(36, 1), -- Hoạt Hình
-- 37. THE CONJURING
(37, 7), -- Kinh Dị
-- 38. LÀM GIÀU VỚI MA 2
(38, 6), -- Hài
(38, 3), -- Hành Động
-- 39. TRON: ARES
(39, 3), -- Hành Động
(39, 11),-- Khoa Học Viễn Tưởng
(39, 2), -- Phiêu Lưu
-- 40. ĐIỆN THOẠI ĐEN 2
(40, 4), -- Hồi hộp
(40, 7), -- Kinh Dị
-- 41. MORTAL KOMBAT II
(41, 3), -- Hành Động
(41, 2), -- Phiêu Lưu
(41, 9), -- Thần thoại
-- 42. PHI VỤ ĐỘNG TRỜI 2
(42, 5), -- Gia đình
(42, 3), -- Hành Động
(42, 2), -- Phiêu Lưu
(42, 9), -- Thần thoại
-- 43. WICKED: PHẦN 2
(43, 9), -- Thần thoại
(43, 14),-- Nhạc Kịch
-- 44. CHÀNG MÈO MANG MŨ
(44, 5), -- Gia đình
(44, 6), -- Hài
(44, 1), -- Hoạt Hình
(44, 2), -- Phiêu Lưu
-- 45. THOÁT KHỎI TẬN THẾ
(45, 11),-- Khoa Học Viễn Tưởng
(45, 2), -- Phiêu Lưu
-- 46. CÚ NHẢY KỲ DIỆU
(46, 5), -- Gia đình
(46, 6), -- Hài
(46, 1), -- Hoạt Hình
(46, 2); -- Phiêu Lưu

-- 18. promotion
INSERT INTO public.promotion (
    title, 
    detail, 
    discount_level, 
    discount_amount, 
    min_tickets, 
    max_tickets, 
    day_of_week, 
    ticket_type_id, 
    start_time, 
    end_time, 
    image_url, 
    is_active, 
    max_usage
) VALUES
    -- 1. Giảm giá theo nhóm (Cinestar: Mua 4 vé giảm 10%, mua 8 vé giảm 20%)
    (
        'Mua 4 vé giảm 10%',
        'Giảm 10% khi mua từ 4 vé trở lên trong một giao dịch. Áp dụng cho tất cả các suất chiếu.',
        10.00, -- 10% giảm giá
        NULL, -- Không giảm cố định
        4, -- Tối thiểu 4 vé
        7, -- Tối đa 7 vé
        NULL, -- Áp dụng mọi ngày
        1, -- Áp dụng cho vé Thường
        '2025-06-30 00:00:00', -- Bắt đầu
        '2025-12-31 23:59:59', -- Kết thúc
        'https://i.postimg.cc/HLMNb25p/Screenshot-2025-07-26-192944.png',
        true,
        1000 -- Giới hạn 1000 lượt sử dụng
    ),
    (
        'Mua 8 vé giảm 20%',
        'Giảm 20% khi mua từ 8 vé trở lên trong một giao dịch. Áp dụng cho tất cả các suất chiếu.',
        20.00, -- 20% giảm giá
        NULL, -- Không giảm cố định
        8, -- Tối thiểu 8 vé
        NULL, -- Không giới hạn tối đa
        NULL, -- Áp dụng mọi ngày
        1, -- Áp dụng cho vé Thường
        '2025-06-30 00:00:00',
        '2025-12-31 23:59:59',
        'https://i.postimg.cc/pTt6CFYK/Screenshot-2025-07-26-193610.png',
        true,
        5000 -- Giới hạn 500 lượt sử dụng
    ),

    -- 2. Ưu đãi ngày đặc biệt (CGV: Super Tuesday, Lotte: Monitor Wednesday, CGV: Black Friday)
    (
        'Thứ 3 Siêu Rẻ',
        'Giảm 20.000 ₫/vé cho tất cả suất chiếu 2D vào thứ 3.',
        NULL, -- Không giảm phần trăm
        20000.00, -- Giả định giá vé gốc ~70.000 ₫, giảm 20.000 ₫
        NULL, -- Không yêu cầu số vé tối thiểu
        NULL, -- Không giới hạn tối đa
        2, -- Áp dụng cho thứ 3
        1, -- Áp dụng cho vé Thường
        '2025-06-30 00:00:00',
        '2025-12-31 23:59:59',
        'https://i.postimg.cc/Nj3JGHnZ/Screenshot-2025-07-26-155832.png',
        true,
        2000 -- Giới hạn 2000 lượt sử dụng
    ),
    (
        'Black Friday 2025',
        'Giảm 25% cho tất cả các suất chiếu trong dịp Black Friday.',
        25.00, -- 25% giảm giá
        NULL, -- Không giảm cố định
        NULL, -- Không yêu cầu số vé tối thiểu
        NULL, -- Không giới hạn tối đa
        NULL, -- Áp dụng mọi ngày trong khoảng thời gian
        1, -- Áp dụng cho vé Thường
        '2025-11-28 00:00:00', -- Giả định Black Friday
        '2025-11-30 23:59:59',
        'https://i.postimg.cc/L8YQMTxz/Screenshot-2025-07-26-161804.png',
        true,
        1000 -- Giới hạn 1000 lượt sử dụng
    ),

    -- 3. Ưu đãi sinh viên (CGV: Giảm 20.000 ₫/vé)
    (
        'Ưu đãi Sinh viên',
        'Giảm 20.000 ₫/vé cho vé sinh viên, áp dụng cho các suất chiếu thường. Kiểm tra thẻ sinh viên tại quầy.',
        NULL, -- Không giảm phần trăm
        20000.00, -- Giảm 20.000 ₫/vé
        NULL, -- Không yêu cầu số vé tối thiểu
        NULL, -- Không giới hạn tối đa
        NULL, -- Áp dụng mọi ngày
        2, -- Áp dụng cho vé Sinh viên
        '2025-06-30 00:00:00',
        '2025-12-31 23:59:59',
        'https://i.postimg.cc/bv16Zvcr/Screenshot-2025-07-26-160503.png',
        true,
        5000 -- Giới hạn 5000 lượt sử dụng
    ),

	(
        'Xem Conan – Giảm ngay 15% tại MOON CINEMA!',
        'Đồng hành cùng những vụ án nghẹt thở và phá án cùng Conan, bạn sẽ nhận ngay ưu đãi 15% cho mọi suất chiếu bộ phim Thám tử lừng danh Conan.',
        15.00, -- giảm 15% phần trăm
        NULL, -- Giảm 20.000 ₫/vé
        NULL, -- Không yêu cầu số vé tối thiểu
        NULL, -- Không giới hạn tối đa
        NULL, -- Áp dụng mọi ngày
        1, -- Áp dụng cho vé thường
        '2025-07-25 00:00:00',
        '2025-09-1 23:59:59',
        'https://i.postimg.cc/g2Q5w5xS/Screenshot-2025-07-26-204159.png',
        true,
        NULL
    );

--19 movie_promotion	
INSERT INTO public.movie_promotion (movie_id, promotion_id)
VALUES (1, 6);
COMMIT;
