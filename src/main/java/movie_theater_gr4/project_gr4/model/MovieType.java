package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "movie_type")
@IdClass(MovieTypeId.class)
public class MovieType {
    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Id
    @Column(name = "type_id")
    private Long typeId;

    @ManyToOne
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private Type type;
}
