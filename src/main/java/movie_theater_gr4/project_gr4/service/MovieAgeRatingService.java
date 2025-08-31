package movie_theater_gr4.project_gr4.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import movie_theater_gr4.project_gr4.repository.MovieAgeRatingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieAgeRatingService {
    private final MovieAgeRatingRepository movieAgeRatingRepository;

    public List<movie_theater_gr4.project_gr4.model.MovieAgeRating> geMovieAgeRatings() {
        return movieAgeRatingRepository.findAll();
    }
    public MovieAgeRating getMovieAgeRatingByMovieId(long id){
        return movieAgeRatingRepository.findAgeRatingByMovieId(id);
    }

}
