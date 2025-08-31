package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "type")
public class Type {
    @Id
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "type_name")
    private String typeName;

    @OneToMany(mappedBy = "type")
    private List<MovieType> movieTypes;
}