//package movie_theater_gr4.project_gr4.mapper;
//
//import movie_theater_gr4.project_gr4.model.Movie;
//import movie_theater_gr4.project_gr4.model.MovieDocument;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface MovieMapper {
//    @Mapping(source = "description", target = "content")
//    @Mapping(source = "types", target = "genres", expression = "java(movie.getTypes() != null ? movie.getTypes().stream().map(t -> t.getTypeName()).collect(java.util.stream.Collectors.toList()) : null)")
//    @Mapping(source = "versions", target = "versions", expression = "java(movie.getVersions() != null ? movie.getVersions().stream().map(v -> v.getVersionName()).collect(java.util.stream.Collectors.toList()) : null)")
//    @Mapping(source = "ageRating.ratingCode", target = "ratingCode")
//    MovieDocument toMovieDocument(Movie movie);
//}