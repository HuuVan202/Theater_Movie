package movie_theater_gr4.project_gr4.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        httpSecurity
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/auth", "/authAdmin", "/home",
                                "/forgot", "/verifyOTP", "/register", "/register-google", "/changePassword",
                                "/faq", "/terms", "/support", "/refund-policy", "/privacy", "/news", "/promotion/**",
                                "/static/**", "/css/**", "/js/**", "/img/**", "/ws/**",
                                "/nowShowing", "/comingSoon", "/detail/{id}", "/oauth2/**", "/showtime/**").permitAll()
                        .requestMatchers("/admin/**", "/admin/stats/all", "/admin/logout").hasRole("ADMIN")
                        .requestMatchers("/admin/logout").hasRole("ADMIN_SUPPORT")
                        .requestMatchers("/admin/**", "/stats/all").hasRole("ADMIN")
                        .requestMatchers("/admin/logout").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/notifications/**").authenticated()
                        .requestMatchers("/selectSeats/**").authenticated() // Keep this to allow any authenticated user
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth")
                        .loginProcessingUrl("/auth")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth")
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey")
                        .tokenValiditySeconds(86400 * 7)
                        .rememberMeParameter("remember-me")
                        .useSecureCookie(true)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new OrRequestMatcher(
                                new AntPathRequestMatcher("/logout", "POST"),
                                new AntPathRequestMatcher("/admin/logout", "POST")
                        ))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String redirectUrl = "/auth?logout=true";
                            if (authentication != null && authentication.getAuthorities().stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                                            || auth.getAuthority().equals("ROLE_EMPLOYEE")
                            || auth.getAuthority().equals("ROLE_ADMIN_SUPPORT"))) {
                                redirectUrl = "/authAdmin?logout=true";
                            }
                            request.getSession().invalidate();
                            response.setHeader("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0; HttpOnly");
                            if (!response.isCommitted()) {
                                response.sendRedirect(redirectUrl);
                            } else {
                                System.err.println("Cannot redirect to " + redirectUrl + ": Response already committed");
                                response.setContentType("text/html");
                                response.getWriter().write("<html><head><meta http-equiv='refresh' content='0;url=" + redirectUrl + "'></head><body></body></html>");
                            }
                        })
                        .addLogoutHandler((request, response, authentication) -> {
                            String requestUri = request.getRequestURI();
                            if ("/admin/logout".equals(requestUri)) {
                                if (authentication != null && authentication.getAuthorities().stream()
                                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                                                || auth.getAuthority().equals("ROLE_EMPLOYEE")
                                        || auth.getAuthority().equals("ROLE_ADMIN_SUPPORT"))) {
                                    throw new AccessDeniedException("Only ADMIN or EMPLOYEE users can access /admin/logout");
                                }
                            }
                        })
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth", "/authAdmin", "/login", "/logout", "/admin/logout", "/ws/**", "/notifications/send", "/oauth2/**", "/selectSeats/**") // Add /selectSeats/** to disable CSRF
                )
                .addFilterBefore(new RedirectAuthenticatedUserFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new SaveRequestFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(adminAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .securityContext(context -> context.requireExplicitSave(false));

        return httpSecurity.build();
    }

    // Bộ lọc mới để chuyển hướng người dùng đã xác thực
    public static class RedirectAuthenticatedUserFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String requestURI = request.getRequestURI();
            // Chỉ áp dụng cho /auth và /authAdmin (bao gồm cả query parameters)
            if (requestURI.startsWith("/auth") || requestURI.startsWith("/authAdmin")) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                    boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    boolean isEmployee = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));

                    String redirectUrl = "/home"; // Default for regular users
                    if (isAdmin) {
                        redirectUrl = "/admin/stats/all";
                        System.out.println("Redirecting authenticated admin to /admin/stats/all");
                    } else if (isEmployee) {
                        redirectUrl = "/employee/showtime";
                        System.out.println("Redirecting authenticated employee to /employee/showtime");
                    }

                    if (!response.isCommitted()) {
                        response.sendRedirect(redirectUrl);
                        return;
                    } else {
                        System.err.println("Cannot redirect to " + redirectUrl + ": Response already committed");
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    // SaveRequestFilter modified to avoid interfering with /selectSeats
    public static class SaveRequestFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String requestURI = request.getRequestURI();
            if (!requestURI.startsWith("/auth") &&
                    !requestURI.startsWith("/authAdmin") &&
                    !requestURI.startsWith("/login") &&
                    !requestURI.startsWith("/logout") &&
                    !requestURI.startsWith("/oauth2") &&
                    !requestURI.startsWith("/register") &&
                    !requestURI.startsWith("/selectSeats") && // Exclude /selectSeats
                    !requestURI.equals("/favicon.ico") &&
                    !requestURI.startsWith("/css/") &&
                    !requestURI.startsWith("/js/") &&
                    !requestURI.startsWith("/img/") &&
                    !requestURI.startsWith("/showtime/") &&
                    !requestURI.startsWith("/favicon/")) {
                request.getSession().setAttribute("PRE_LOGIN_URL", requestURI);
                System.out.println("Saved PRE_LOGIN_URL: " + requestURI);
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String servletPath = request.getServletPath();
            String requestUri = request.getRequestURI();
            String sessionId = request.getSession().getId();
            System.out.println("Form login success for user: " + authentication.getName() +
                    ", Servlet Path: " + servletPath + ", Request URI: " + requestUri +
                    ", Session ID: " + sessionId + ", Response committed: " + response.isCommitted());
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isEmployee = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));
            boolean isAdminSupport = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN_SUPPORT"));

            request.getSession().setAttribute("AUTHENTICATION_PATH", servletPath);

            String redirectUrl = (String) request.getSession().getAttribute("PRE_LOGIN_URL");
            if (redirectUrl == null || redirectUrl.isEmpty() || redirectUrl.equals("/favicon.ico") ||
                    redirectUrl.startsWith("/css/") || redirectUrl.startsWith("/js/") || redirectUrl.startsWith("/img/")) {
                redirectUrl = "/home";
            }

            if (isAdminSupport) {
                redirectUrl = "/chat";
            } else if (isAdmin && ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri))) {
                System.out.println("Redirecting admin to /admin/stats/all");
                redirectUrl = "/admin/stats/all";
            } else if (isEmployee && ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri))) {
                System.out.println("Redirecting employee to /employee/showtime");
                redirectUrl = "/employee/showtime";
            } else if (!redirectUrl.startsWith("/auth") && !redirectUrl.startsWith("/authAdmin") &&
                    !redirectUrl.startsWith("/login") && !redirectUrl.startsWith("/logout")) {
                System.out.println("Redirecting user to previous URL: " + redirectUrl);
            } else {
                System.out.println("Redirecting user to /home as fallback");
                redirectUrl = "/home";
            }

            if (!response.isCommitted()) {
                response.sendRedirect(redirectUrl);
            } else {
                System.err.println("Cannot redirect to " + redirectUrl + ": Response already committed");
            }
            request.getSession().removeAttribute("PRE_LOGIN_URL");
        };
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            String servletPath = request.getServletPath();
            String requestUri = request.getRequestURI();
            String sessionId = request.getSession().getId();
            System.out.println("OAuth2 login success for user: " + authentication.getName() +
                    ", Servlet Path: " + servletPath + ", Request URI: " + requestUri +
                    ", Session ID: " + sessionId + ", Response committed: " + response.isCommitted());
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isEmployee = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));
            boolean isAdminSupport = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN_SUPPORT"));

            request.getSession().setAttribute("AUTHENTICATION_PATH", servletPath);

            String redirectUrl = (String) request.getSession().getAttribute("PRE_LOGIN_URL");
            if (redirectUrl == null || redirectUrl.isEmpty() || redirectUrl.equals("/favicon.ico") ||
                    redirectUrl.startsWith("/css/") || redirectUrl.startsWith("/js/") || redirectUrl.startsWith("/img/")) {
                redirectUrl = "/home";
            }

            if (isAdminSupport) {
                redirectUrl = "/chat";
            } else if (isAdmin && ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri))) {
                System.out.println("Redirecting OAuth2 admin to /admin/stats/all");
                redirectUrl = "/admin/stats/all";
            } else if (isEmployee && ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri))) {
                System.out.println("Redirecting OAuth2 employee to /employee/showtime");
                redirectUrl = "/employee/showtime";
            } else if (!isAdmin && !isEmployee && ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri))) {
                System.err.println("Non-admin/employee user attempted to access /authAdmin via OAuth2: " + authentication.getName());
                redirectUrl = "/auth?error=true";
            } else if (!redirectUrl.startsWith("/auth") && !redirectUrl.startsWith("/authAdmin") &&
                    !redirectUrl.startsWith("/login") && !redirectUrl.startsWith("/logout")) {
                System.out.println("Redirecting OAuth2 user to previous URL: " + redirectUrl);
                redirectUrl = "/register-google";
            }

            if (!response.isCommitted()) {
                response.sendRedirect(redirectUrl);
            } else {
                System.err.println("Cannot redirect to " + redirectUrl + ": Response already committed");
            }
            request.getSession().removeAttribute("PRE_LOGIN_URL");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String servletPath = request.getServletPath();
            String requestUri = request.getRequestURI();
            String sessionId = request.getSession().getId();
            System.err.println("Login failed for path: " + servletPath +
                    ", Request URI: " + requestUri +
                    ", Session ID: " + sessionId +
                    ", Error: " + exception.getMessage());
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
            String redirectUrl = "/auth?error=true";
            if ("/authAdmin".equals(servletPath) || "/authAdmin".equals(requestUri)) {
                redirectUrl = "/authAdmin?error=true";
            }
            if (!response.isCommitted()) {
                response.sendRedirect(redirectUrl);
            } else {
                System.err.println("Cannot redirect to " + redirectUrl + ": Response already committed");
            }
        };
    }

    @Bean
    public AdminAuthenticationFilter adminAuthenticationFilter(AuthenticationManager authenticationManager) {
        AdminAuthenticationFilter filter = new AdminAuthenticationFilter(authenticationManager);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/authAdmin", "POST"));
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }
}