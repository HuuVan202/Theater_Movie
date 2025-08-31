package movie_theater_gr4.project_gr4.repository;
//
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.model.MovieDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieElasticSearchRepository extends ElasticsearchRepository<MovieDocument,Long> {
    Page<MovieDocument> findByMovieNameContains(@Param("keyword") String keyword, PageRequest pageRequest);
    @Query("""
        {
            "bool": {
                "must": [
                    { "multi_match": { "query": "?0", "fields": ["movieName", "movieNameVn", "movieNameEn", "content", "genres"], "fuzziness": "AUTO" } }
                ],
                "filter": [
                    { "terms": { "genres.keyword": "?1" } },
                    { "range": { "fromDate": { "lte": "?2", "gt": "?3", "format": "yyyy-MM-dd" } } }
                ]
            }
        }
    """)
    Page<MovieDocument> searchWithDateCondition(String keyword, List<String> genres, String lteDate, String gtDate, Pageable pageable);

}
