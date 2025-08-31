package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TypeRepository extends JpaRepository<Type, Long> {

    @Query("SELECT t.typeName FROM Movie m JOIN m.movieTypes mt JOIN mt.type t WHERE m.movieId = :movieId")
    List<String> findGenresByMovieId(@Param("movieId") Long movieId);
}