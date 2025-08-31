package movie_theater_gr4.project_gr4;

import movie_theater_gr4.project_gr4.dto.MovieDTOCRUD;
import movie_theater_gr4.project_gr4.model.Movie;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import movie_theater_gr4.project_gr4.model.Type;
import movie_theater_gr4.project_gr4.model.Version;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProjectGr4ApplicationTests {

	@Test
	void contextLoads() {
	}
//	@Test
//	public void testCreateMovie() {
//		MovieDTOCRUD movieDTO = MovieDTOCRUD.builder()
//				.movieName("Test Movie")
//				.duration(120)
//				.ageRating(1L)
//				.genres(Arrays.asList(1L, 2L))
//				.versions(Arrays.asList(1L, 2L))
//				.showDates(Arrays.asList(LocalDate.now()))
//				.build();
//
//		List<Version> allVersions = Arrays.asList(new Version(1L, "2D"), new Version(2L, "3D"));
//		List<Type> allTypes = Arrays.asList(new Type(1L, "Hành động"), new Type(2L, "Hài"));
//		List<MovieAgeRatingService> allRatings = Arrays.asList(new MovieAgeRatingService("P", "Phổ thông"));
//
//		when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//		Movie movie = movieService.createMovie(movieDTO, allVersions, allTypes, allRatings);
//
//		assertEquals("Test Movie", movie.getMovieName());
//		assertEquals("P", movie.getAgeRating().getRatingCode());
//		assertEquals(2, movie.getVersions().size());
//		assertEquals(2, movie.getMovieTypes().size());
//	}

}
