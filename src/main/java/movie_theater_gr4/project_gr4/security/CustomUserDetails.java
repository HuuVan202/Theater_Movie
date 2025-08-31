package movie_theater_gr4.project_gr4.security;


import lombok.Getter;
import movie_theater_gr4.project_gr4.model.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final int accountId;
    private final String username;
    private final String password;
    @Getter
    private final String fullName;
    @Getter
    private final String avatarUrl;
    @Getter
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountLocked;

    public CustomUserDetails(Account account) {
        this.accountId = account.getAccountId();
        this.username = account.getUsername();
        this.password = account.getPassword();
        this.fullName = account.getFullName();
        this.avatarUrl = account.getAvatarUrl();
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (account.getRole() != null ? account.getRole().name().toUpperCase() : "MEMBER"))
        );
        this.accountLocked = account.getStatus() != 1;
        this.email = account.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
