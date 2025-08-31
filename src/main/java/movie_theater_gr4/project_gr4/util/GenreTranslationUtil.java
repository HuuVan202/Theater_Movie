package movie_theater_gr4.project_gr4.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Giúp chuyển tên thể loại tiếng Việt sang key tiếng Anh chuẩn
 * (để dùng trong messages.properties hoặc lưu DB)
 */
@Component
public class GenreTranslationUtil {

    /**
     * Bảng ánh xạ bất biến (thread-safe)
     */
    private static final Map<String, String> VI_TO_EN_MAP;
    private final MessageSource messageSource;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("Hành Động", "Action");
        m.put("Phiêu Lưu", "Adventure");
        m.put("Hoạt Hình", "Animation");
        m.put("Hài", "Comedy");
        m.put("Tội Phạm", "Crime");
        m.put("Tài Liệu", "Documentary");
        m.put("Chính Kịch", "Drama");
        m.put("Gia đình", "Family");
        m.put("Giả Tưởng", "Fantasy");
        m.put("Lịch Sử", "History");
        m.put("Kinh Dị", "Horror");
        m.put("Âm Nhạc", "Music");
        m.put("Bí ẩn", "Mystery");
        m.put("Tình cảm", "Romance");
        m.put("Khoa Học Viễn Tưởng", "Science Fiction");
        m.put("Phim Truyền Hình", "TV Movie");
        m.put("Giật Gân", "Thriller");
        m.put("Chiến Tranh", "War");
        m.put("Cao Bồi", "Western");
        m.put("Tiểu Sử", "Biography");
        m.put("Nhạc Kịch", "Musical");
        m.put("Thể Thao", "Sport");
        m.put("Siêu Anh Hùng", "Superhero");
        m.put("Tâm Lý", "Psychological");
        m.put("Hồi hộp", "Suspense");
        m.put("Thần thoại", "Mythology");
        VI_TO_EN_MAP = Collections.unmodifiableMap(m);
    }

    public GenreTranslationUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Trả về tên tiếng Anh; nếu không có thì trả nguyên input
     */
    public String toEnglishKey(String genreName) {
        if (genreName == null) return null;
        return VI_TO_EN_MAP.getOrDefault(genreName.trim(), genreName.trim());
    }

    /**
     * Nếu muốn có dạng key chuẩn cho messages.properties (genre.science_fiction)
     */
    public String toMessageKey(String genreName) {
        String en = toEnglishKey(genreName);
        return "genre." + en.toLowerCase().replace(" ", "_");
    }

    /**
     * Lấy bản dịch của thể loại theo ngôn ngữ hiện tại
     *
     * @param genreName Tên thể loại (có thể là tiếng Việt)
     * @param locale    Locale hiện tại
     * @return Bản dịch của thể loại
     */
    public String getTranslation(String genreName, Locale locale) {
        String englishKey = toEnglishKey(genreName);
        String messageKey = "genre." + englishKey;
        return messageSource.getMessage(messageKey, null, englishKey, locale);
    }
}
