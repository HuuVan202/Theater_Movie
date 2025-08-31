package movie_theater_gr4.project_gr4.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.model.MovieType;
import movie_theater_gr4.project_gr4.model.Version;
import movie_theater_gr4.project_gr4.repository.MovieTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class MovieTypeService {
    private final MovieTypeRepository movieTypeRepository;

    public List<MovieType> getAllTypes() {
        return movieTypeRepository.findAll();
    }
}
