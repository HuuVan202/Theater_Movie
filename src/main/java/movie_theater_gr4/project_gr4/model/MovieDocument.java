package movie_theater_gr4.project_gr4.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(indexName = "movie", writeTypeHint = WriteTypeHint.FALSE)
public class MovieDocument {
    @Id
    private Long movieId;

    @Field(type = FieldType.Text)
    private String movieName;

    @Field(type = FieldType.Text)
    private String movieNameVn;

    @Field(type = FieldType.Text)
    private String movieNameEn;

    @Field(type = FieldType.Text)
    private String director;

    @Field(type = FieldType.Text)
    private String actor;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Integer)
    private Integer duration;

    @Field(type = FieldType.Text)
    private String productionCompany;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @Field(type = FieldType.Text)
    private String largeImageUrl;

    @Field(type = FieldType.Text)
    private String smallImageUrl;

    @Field(type = FieldType.Text)
    private String trailerUrl;

    @Field(type = FieldType.Integer)
    private Integer featured;

    @Field(type = FieldType.Text)
    private List<String> genres;

//    public static MovieDocument fromEntity(Movie movie) {
//        MovieDocument doc = new MovieDocument();
//        doc.setMovieId(movie.getMovieId());
//        doc.setMovieName(movie.getMovieName());
//        doc.setMovieNameVn(movie.getMovieNameVn());
//        doc.setMovieNameEn(movie.getMovieNameEn());
//        doc.setContent(movie.getContent());
//        doc.setDuration(movie.getDuration());
//        doc.setDirector(movie.getDirector());
//        doc.setActor(movie.getActor());
//        doc.setProductionCompany(movie.getProductionCompany());
//        doc.setFromDate(movie.getFromDate());
//        doc.setToDate(movie.getToDate());
//        doc.setLargeImageUrl(movie.getLargeImageUrl());
//        doc.setSmallImageUrl(movie.getSmallImageUrl());
//        doc.setTrailerUrl(movie.getTrailerUrl());
//        doc.setFeatured(movie.getFeatured());
//        return doc;
//    }
}