package movie_theater_gr4.project_gr4.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import co.elastic.clients.json.JsonData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import movie_theater_gr4.project_gr4.dto.MovieSearchDTO;
import movie_theater_gr4.project_gr4.model.Movie;
import movie_theater_gr4.project_gr4.model.MovieDocument;
import movie_theater_gr4.project_gr4.repository.MovieElasticSearchRepository;
import movie_theater_gr4.project_gr4.repository.MovieRepository;
import movie_theater_gr4.project_gr4.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;



import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieDocumentService {
    @Autowired
    private MovieElasticSearchRepository movieElasticSearchRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TypeRepository typeRepository;


    public MovieSearchDTO convert(MovieDocument doc, Map<String, List<String>> highlights) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MovieSearchDTO dto = objectMapper.convertValue(doc, MovieSearchDTO.class);
        dto.setHighlights(highlights);
        return dto;
    }

    public MovieDocument convertToMovieDocument(MovieSearchDTO dto) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MovieDocument document = objectMapper.convertValue(dto, MovieDocument.class);
        return document;
    }
    public MovieDocument convertToMovieDocument(Movie movie) {
        MovieDocument document = new MovieDocument();
        List<String> genres = typeRepository.findGenresByMovieId(movie.getMovieId())
                .stream()
                .map(String::trim)
                .map(String::toLowerCase) // Chuẩn hóa thành chữ thường
                .collect(Collectors.toList());
        document.setMovieId(movie.getMovieId());
        document.setMovieName(movie.getMovieName());
        document.setMovieNameVn(movie.getMovieNameVn());
        document.setMovieNameEn(movie.getMovieNameEn());
        document.setContent(movie.getContent());
        document.setDirector(movie.getDirector());
        document.setActor(movie.getActor());
        document.setProductionCompany(movie.getProductionCompany());
        document.setLargeImageUrl(movie.getLargeImageUrl());
        document.setSmallImageUrl(movie.getSmallImageUrl());
        document.setTrailerUrl(movie.getTrailerUrl());
        document.setDuration(movie.getDuration());
        document.setFeatured(movie.getFeatured());
        document.setFromDate(movie.getFromDate());
        document.setToDate(movie.getToDate());
        document.setGenres(genres);
        // Không truy cập movie.getVersions() hoặc version.getShowtimes()
        return document;
    }


    public Page<MovieSearchDTO> searchMovieDocument(String keyword, List<String> genres, PageRequest pageRequest) {
        try {
//            System.out.println("Searching with keyword: " + keyword + ", genres: " + (genres != null ? genres : "none"));
            SearchResponse<MovieDocument> response = elasticsearchClient.search(s -> {
                s.index("movie")
                        .from((int) pageRequest.getOffset())
                        .size(pageRequest.getPageSize());

                s.query(q -> {
                    BoolQuery.Builder boolQuery = new BoolQuery.Builder();

                    if (StringUtils.hasText(keyword)) {
                        boolQuery.must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("movieName", "movieNameVn", "movieNameEn", "genres")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                )
                        );
                    } else {
                        boolQuery.must(m -> m.matchAll(ma -> ma));
                    }

                    if (genres != null && !genres.isEmpty()) {
                        boolQuery.filter(f -> f
                                .terms(t -> t
                                        .field("genres.keyword") // Sử dụng genres.keyword
                                        .terms(tt -> tt.value(genres.stream().map(FieldValue::of).collect(Collectors.toList())))
                                )
                        );
                    }

                    return q.bool(boolQuery.build());
                });

                s.highlight(h -> h
                        .fields("movieName", f -> f)
                        .fields("movieNameVn", f -> f)
                        .fields("movieNameEn", f -> f)
                        .fields("content", f -> f)
                        .fields("genres", f -> f)
                        .preTags("<em>")
                        .postTags("</em>")
                );

                return s;
            }, MovieDocument.class);

            List<MovieSearchDTO> dtoList = response.hits().hits().stream()
                    .map(hit -> {
                        MovieSearchDTO dto = convert(hit.source(), hit.highlight());
                        if (dto.getGenres() == null && hit.source() != null) {
                            dto.setGenres(hit.source().getGenres());
                        }
                        return dto;
                    })
                    .peek(dto -> System.out.println("Document with highlights: " + dto))
                    .toList();

            long totalHits = response.hits().total() != null ? response.hits().total().value() : dtoList.size();
            return new PageImpl<>(dtoList, pageRequest, totalHits);
        } catch (IOException e) {
            System.err.println("Elasticsearch search error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Elasticsearch search error", e);
        }
    }
    public List<String> suggestMovies(String keyword) {
        try {
            System.out.println("Fetching suggestions for keyword: " + keyword);
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                            .index("movie")
                            .suggest(sug -> sug
                                    .suggesters("movie-suggest", sugField -> sugField
                                            .completion(c -> c
                                                    .field("suggest")
                                                    .size(5)
                                                    .skipDuplicates(true)
                                                    .fuzzy(f -> f.fuzziness("AUTO"))
                                            )
                                            .text(keyword)
                                    )
                            ),
                    Void.class
            );

//            System.out.println("Suggestion response: " + response.toString());
            if (response.suggest() != null && response.suggest().get("movie-suggest") != null) {
                List<String> suggestions = response.suggest().get("movie-suggest").stream()
                        .flatMap(s -> s.completion().options().stream()
                                .map(o -> o.text()))
                        .filter(text -> text != null && !text.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
//                System.out.println("Suggestions returned: " + suggestions);
                return suggestions;
            }
//            System.out.println("No suggestions found for keyword: " + keyword);
            return List.of();
        } catch (IOException e) {
            System.err.println("Elasticsearch suggestion error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public void syncMoviesToElasticsearch() {
        System.out.println("Starting sync to Elasticsearch...");
        try {
            try {
                elasticsearchClient.indices().delete(d -> d.index("movie"));
                System.out.println("Deleted index 'movie'");
            } catch (Exception e) {
                System.out.println("Index 'movie' not found or could not be deleted: " + e.getMessage());
            }

            elasticsearchClient.indices().create(c -> c
                    .index("movie")
                    .mappings(m -> m
                            .properties("movieId", p -> p.long_(l -> l))
                            .properties("movieName", p -> p.text(t -> t))
                            .properties("movieNameVn", p -> p.text(t -> t))
                            .properties("movieNameEn", p -> p.text(t -> t))
                            .properties("content", p -> p.text(t -> t))
                            .properties("director", p -> p.text(t -> t))
                            .properties("actor", p -> p.text(t -> t))
                            .properties("productionCompany", p -> p.text(t -> t))
                            .properties("largeImageUrl", p -> p.text(t -> t))
                            .properties("smallImageUrl", p -> p.text(t -> t))
                            .properties("trailerUrl", p -> p.text(t -> t))
                            .properties("duration", p -> p.integer(i -> i))
                            .properties("featured", p -> p.integer(i -> i))
                            .properties("fromDate", p -> p.date(d -> d.format("yyyy-MM-dd")))
                            .properties("toDate", p -> p.date(d -> d.format("yyyy-MM-dd")))
                            .properties("suggest", p -> p.completion(co -> co))
                            .properties("genres", p -> p
                                    .text(t -> t
                                            .fields("keyword", f -> f.keyword(k -> k)) // Thêm sub-field keyword
                                    )
                            )
                    )
            );
            System.out.println("Created index 'movie' with mapping");

            int pageSize = 100;
            int page = 0;
            Page<Movie> moviePage;
            do {
                PageRequest pageRequest = PageRequest.of(page, pageSize);
                moviePage = movieRepository.findAll(pageRequest);
                System.out.println("Fetched " + moviePage.getContent().size() + " movies from database in page " + page);
                List<MovieDocument> movieDocuments = moviePage.getContent().stream()
                        .map(this::convertToMovieDocument)
                        .peek(doc -> System.out.println("Genres for movieId=" + doc.getMovieId() + ": " + doc.getGenres()))
                        .collect(Collectors.toList());

                BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
                for (MovieDocument doc : movieDocuments) {
                    Map<String, Object> docWithSuggest = objectMapper.convertValue(doc, Map.class);
                    List<String> suggestions = Arrays.asList(
                                    doc.getMovieName() != null ? doc.getMovieName() : "",
                                    doc.getMovieNameVn() != null ? doc.getMovieNameVn() : "",
                                    doc.getMovieNameEn() != null ? doc.getMovieNameEn() : "",
                                    "Conan" // Thêm từ khóa ngắn để tìm kiếm dễ dàng hơn
                            ).stream()
                            .filter(s -> s != null && !s.isEmpty())
                            .distinct()
                            .collect(Collectors.toList());
                    if (!suggestions.isEmpty()) {
                        docWithSuggest.put("suggest", suggestions);
                        System.out.println("Adding suggest for movieId=" + doc.getMovieId() + ": " + suggestions);
                    } else {
                        System.out.println("No valid suggestions for movieId=" + doc.getMovieId());
                    }
                    bulkRequest.operations(op -> op
                            .index(idx -> idx
                                    .index("movie")
                                    .id(doc.getMovieId().toString())
                                    .document(docWithSuggest)
                            )
                    );
                }
                elasticsearchClient.bulk(bulkRequest.build());
                System.out.println("Synced " + movieDocuments.size() + " movies in page " + page);
                page++;
            } while (moviePage.hasNext());
            System.out.println("Completed syncing movies to Elasticsearch");
        } catch (Exception e) {
            System.err.println("Error during sync: " + e.getMessage());
            throw new RuntimeException("Sync failed", e);
        }
    }

    public void saveMovieDocument(Movie movie) throws IOException {

        MovieDocument document = convertToMovieDocument(movie);
        IndexRequest<MovieDocument> request = IndexRequest.of(i -> i
                .index("movie")
                .id(movie.getMovieId().toString())
                .document(document));
        elasticsearchClient.index(request);
    }

    public void deleteMovieDocument(Long id) throws IOException {
        DeleteRequest request = DeleteRequest.of(d -> d
                .index("movie")
                .id(id.toString()));
        elasticsearchClient.delete(request);
    }

    public Page<MovieSearchDTO> searchComingSoonMovieDocument(String keyword, List<String> genres, PageRequest pageRequest) {
        try {
            LocalDate today = LocalDate.now();

            SearchResponse<MovieDocument> response = elasticsearchClient.search(s -> {
                s.index("movie")
                        .from((int) pageRequest.getOffset())
                        .size(pageRequest.getPageSize());

                s.query(q -> {
                    BoolQuery.Builder boolQuery = new BoolQuery.Builder();

                    // Điều kiện keyword
                    if (StringUtils.hasText(keyword)) {
                        boolQuery.must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("movieName", "movieNameVn", "movieNameEn", "genres")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                )
                        );
                    } else {
                        boolQuery.must(m -> m.matchAll(ma -> ma));
                    }

                    // Điều kiện genres
                    if (genres != null && !genres.isEmpty()) {
                        boolQuery.filter(f -> f
                                .terms(t -> t
                                        .field("genres.keyword")
                                        .terms(tt -> tt.value(genres.stream()
                                                .map(FieldValue::of)
                                                .collect(Collectors.toList())))
                                )
                        );
                    }

                    // Điều kiện "coming soon" -> fromDate > hôm nay
                    boolQuery.filter(f -> f
                            .range(r -> r
                                    .date(d -> d
                                            .field("fromDate")
                                            .gt(JsonData.of(today.toString()).toString()) // format yyyy-MM-dd
                                    )
                            )
                    );


                    return q.bool(boolQuery.build());
                });

                s.highlight(h -> h
                        .fields("movieName", f -> f)
                        .fields("movieNameVn", f -> f)
                        .fields("movieNameEn", f -> f)
                        .fields("content", f -> f)
                        .fields("genres", f -> f)
                        .preTags("<em>")
                        .postTags("</em>")
                );

                return s;
            }, MovieDocument.class);

            List<MovieSearchDTO> dtoList = response.hits().hits().stream()
                    .map(hit -> {
                        MovieSearchDTO dto = convert(hit.source(), hit.highlight());
                        if (dto.getGenres() == null && hit.source() != null) {
                            dto.setGenres(hit.source().getGenres());
                        }
                        return dto;
                    })
                    .toList();

            long totalHits = response.hits().total() != null ? response.hits().total().value() : dtoList.size();
            return new PageImpl<>(dtoList, pageRequest, totalHits);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search error", e);
        }
    }

    public Page<MovieSearchDTO> searchNowShowingMovieDocument(String keyword, List<String> genres, PageRequest pageRequest) {
        try {
            LocalDate today = LocalDate.now();

            SearchResponse<MovieDocument> response = elasticsearchClient.search(s -> {
                s.index("movie")
                        .from((int) pageRequest.getOffset())
                        .size(pageRequest.getPageSize());

                s.query(q -> {
                    BoolQuery.Builder boolQuery = new BoolQuery.Builder();

                    // Điều kiện keyword
                    if (StringUtils.hasText(keyword)) {
                        boolQuery.must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("movieName", "movieNameVn", "movieNameEn", "genres")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                )
                        );
                    } else {
                        boolQuery.must(m -> m.matchAll(ma -> ma));
                    }

                    // Điều kiện genres
                    if (genres != null && !genres.isEmpty()) {
                        boolQuery.filter(f -> f
                                .terms(t -> t
                                        .field("genres.keyword")
                                        .terms(tt -> tt.value(genres.stream()
                                                .map(FieldValue::of)
                                                .collect(Collectors.toList())))
                                )
                        );
                    }

                    // Điều kiện "now showing" -> fromDate <= hôm nay
                    boolQuery.filter(f -> f
                            .range(r -> r
                                    .date(d -> d
                                            .field("fromDate")
                                            .lte(JsonData.of(today.toString()).toString()) // format yyyy-MM-dd
                                    )
                            )
                    );

                    return q.bool(boolQuery.build());
                });

                s.highlight(h -> h
                        .fields("movieName", f -> f)
                        .fields("movieNameVn", f -> f)
                        .fields("movieNameEn", f -> f)
                        .fields("content", f -> f)
                        .fields("genres", f -> f)
                        .preTags("<em>")
                        .postTags("</em>")
                );

                return s;
            }, MovieDocument.class);

            List<MovieSearchDTO> dtoList = response.hits().hits().stream()
                    .map(hit -> {
                        MovieSearchDTO dto = convert(hit.source(), hit.highlight());
                        if (dto.getGenres() == null && hit.source() != null) {
                            dto.setGenres(hit.source().getGenres());
                        }
                        return dto;
                    })
                    .toList();

            long totalHits = response.hits().total() != null ? response.hits().total().value() : dtoList.size();
            return new PageImpl<>(dtoList, pageRequest, totalHits);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search error", e);
        }
    }



}