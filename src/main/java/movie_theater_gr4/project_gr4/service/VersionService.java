package movie_theater_gr4.project_gr4.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.model.Version;
import movie_theater_gr4.project_gr4.repository.VersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VersionService {
    private final VersionRepository versionRepository;

    public List<Version> getAllTypes() {
        return versionRepository.findAll();
    }
    public List<Version> getVerSionByMovieId(long id){
        return versionRepository.findByMovieVersions_Movie_MovieId(id);
    }
    public boolean isValidVersionId(Long versionId) {
        return versionRepository.findById(versionId).isPresent();
    }
}
