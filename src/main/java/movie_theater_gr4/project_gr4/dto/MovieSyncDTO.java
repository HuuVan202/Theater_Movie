 package movie_theater_gr4.project_gr4.dto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MovieSyncDTO {
    private Long movieId;
    private String movieName;
    private String movieNameVn;
    private String movieNameEn;
    private String content;
    private String director;
    private String actor;
    private String productionCompany;
    private String largeImageUrl;
    private String smallImageUrl;
    private String trailerUrl;
    private Integer duration;
    private Integer featured;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> genreIds;

    // Constructor for JPQL
    public MovieSyncDTO(Long movieId, String movieName, String movieNameVn, String movieNameEn,
                        String content, String director, String actor, String productionCompany,
                        String largeImageUrl, String smallImageUrl, String trailerUrl,
                        Integer duration, Integer featured, LocalDate fromDate, LocalDate toDate,
                        String genreIds) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.movieNameVn = movieNameVn;
        this.movieNameEn = movieNameEn;
        this.content = content;
        this.director = director;
        this.actor = actor;
        this.productionCompany = productionCompany;
        this.largeImageUrl = largeImageUrl;
        this.smallImageUrl = smallImageUrl;
        this.trailerUrl = trailerUrl;
        this.duration = duration;
        this.featured = featured;
        this.fromDate = fromDate;
        this.toDate = toDate;
        if (genreIds != null && !genreIds.isEmpty()) {
            this.genreIds = Arrays.stream(genreIds.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            this.genreIds = List.of();
        }
    }

    // Getters and setters
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public String getMovieNameVn() { return movieNameVn; }
    public void setMovieNameVn(String movieNameVn) { this.movieNameVn = movieNameVn; }
    public String getMovieNameEn() { return movieNameEn; }
    public void setMovieNameEn(String movieNameEn) { this.movieNameEn = movieNameEn; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getProductionCompany() { return productionCompany; }
    public void setProductionCompany(String productionCompany) { this.productionCompany = productionCompany; }
    public String getLargeImageUrl() { return largeImageUrl; }
    public void setLargeImageUrl(String largeImageUrl) { this.largeImageUrl = largeImageUrl; }
    public String getSmallImageUrl() { return smallImageUrl; }
    public void setSmallImageUrl(String smallImageUrl) { this.smallImageUrl = smallImageUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getFeatured() { return featured; }
    public void setFeatured(Integer featured) { this.featured = featured; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public List<String> getGenreIds() { return genreIds; }
    public void setGenreIds(String genreIds) {
        if (genreIds != null && !genreIds.isEmpty()) {
            this.genreIds = Arrays.stream(genreIds.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            this.genreIds = List.of();
        }
    }
    public void setGenreIds(List<String> genreIds) { this.genreIds = genreIds; }
}