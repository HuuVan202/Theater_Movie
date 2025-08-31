package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "movie_version")
@IdClass(MovieVersionId.class)
public class MovieVersion {
    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Id
    @Column(name = "version_id")
    private Long versionId;

    @ManyToOne
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private Version version;
}
