package movie_theater_gr4.project_gr4.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.model.Type;
import movie_theater_gr4.project_gr4.repository.TypeRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;

    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
    public List<String> getAllTypesByMovieId(Long movieId) {
        return typeRepository.findGenresByMovieId(movieId);
    }
    public boolean isValidTypeId(Long typeId) {
        return typeRepository.findById(typeId).isPresent();
    }


}