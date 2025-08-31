package movie_theater_gr4.project_gr4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProjectGr4Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGr4Application.class, args);
	}

}
