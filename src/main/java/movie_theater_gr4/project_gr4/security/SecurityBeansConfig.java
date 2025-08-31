package movie_theater_gr4.project_gr4.security;

import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AccountService accountService) {
        return username -> {
            System.out.println("Authenticating user: " + username);
            Account account = accountService.findAccountByUsername(username);
            if (account == null) {
                System.out.println("User not found: " + username);
                throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
            }
            System.out.println("User found: " + account.getUsername() + ", Role: " +
                    (account.getRole() != null ? account.getRole().name() : "USER"));
            return new CustomUserDetails(account);
        };
    }
}
