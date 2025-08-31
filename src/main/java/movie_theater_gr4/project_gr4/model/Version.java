package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "version")
public class Version {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "version_name")
    private String versionName;

    private String description;

    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY)
    private List<Showtime> showtimes;

    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY)
    private List<MovieVersion> movieVersions;


}
