package movie_theater_gr4.project_gr4.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;

public class AdminAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public AdminAuthenticationFilter(AuthenticationManager authenticationManager) {
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String sessionId = request.getSession().getId();
        System.out.println("Attempting admin authentication for: " + request.getServletPath() +
                ", Request URI: " + request.getRequestURI() +
                ", Session ID: " + sessionId);
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isEmployee = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));
        boolean isAdminSupport = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN_SUPPORT"));
        String sessionId = request.getSession().getId();

        System.out.println("Admin authentication result: isAdmin=" + isAdmin +
                ", User: " + authResult.getName() +
                ", Servlet Path: " + request.getServletPath() +
                ", Request URI: " + request.getRequestURI() +
                ", Session ID: " + sessionId);

        if (!isAdmin && !isEmployee && !isAdminSupport) {
            System.err.println("Non-admin user attempted to login via /authAdmin: " + authResult.getName());
            getFailureHandler().onAuthenticationFailure(request, response,
                    new BadCredentialsException("Only ADMIN users can access this login"));
        } else {
            if (!response.isCommitted()) {
                super.successfulAuthentication(request, response, chain, authResult);
            } else {
                System.err.println("Cannot proceed with admin authentication: Response already committed");
            }
        }
    }
}