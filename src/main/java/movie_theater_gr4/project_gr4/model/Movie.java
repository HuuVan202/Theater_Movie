package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Data
@Entity
@Table(name = "movie")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @Column(name = "movie_name_vn")
    private String movieNameVn;

    @Column(name = "movie_name_en")
    private String movieNameEn;

    @Column(name = "director")
    private String director;

    @Column(name = "actor")
    private String actor;

    @Column(name = "content")
    private String content;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "production_company")
    private String productionCompany;


    @Column(name = "from_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @Column(name = "to_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @Column(name = "large_image_url")
    private String largeImageUrl;

    @Column(name = "small_image_url")
    private String smallImageUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @ManyToOne
    @JoinColumn(name = "rating_code", referencedColumnName = "rating_code")
    private MovieAgeRating ageRating;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<MovieType> movieTypes;

    @ManyToMany
    @JoinTable(
            name = "movie_type",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<Type> types;

    @ManyToMany
    @JoinTable(
            name = "movie_version",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "version_id")
    )

    private List<Version> versions;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Showtime> showtimes;

    @Column(name = "featured")
    private Integer featured;

    @Override
    public String toString() {
        return "Movie{movieId=" + movieId + ", movieName='" + movieName + '\'' + '}';
    }

    public List<Type> getTypes() {
        if (movieTypes == null) return List.of();
        return movieTypes.stream()
                .map(MovieType::getType)
                .toList();
    }


    public List<ShowDate> getShowDates() {
        if (showtimes == null) return List.of();
        return showtimes.stream()
                .map(Showtime::getShowDate)
                .distinct()
                .sorted(Comparator.comparing(ShowDate::getShowDate))
                .toList();
    }



}
