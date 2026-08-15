package com.sporekart.modules.security.infrastructure.jwt;

import com.sporekart.modules.security.domain.UserAccount;
import com.sporekart.modules.security.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String sessionId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;
    private final boolean locked;

    public UserPrincipal(String id, String email, String passwordHash, UserRole role, String sessionId, Collection<? extends GrantedAuthority> authorities, boolean active, boolean locked) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.sessionId = sessionId;
        this.authorities = authorities;
        this.active = active;
        this.locked = locked;
    }

    public static UserPrincipal fromUserAccount(UserAccount userAccount, String sessionId) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(userAccount.getRole().name())
        );

        return new UserPrincipal(
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                userAccount.getRole(),
                sessionId,
                authorities,
                userAccount.getStatus() == com.sporekart.modules.security.domain.UserStatus.ACTIVE,
                userAccount.isAccountLocked()
        );
    }

    public String getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return id; // Return user ID as principal username for controller compatibility
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
