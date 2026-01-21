package com.keldorn.phenylalaninecalculatorapi.domain.entity;

import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User implements UserDetails {

    private static final Set<String> AVAILABLE_ZONE_IDS = ZoneId.getAvailableZoneIds();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "timezone", nullable = false)
    private String timezone = "UTC";

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "daily_limit")
    private BigDecimal dailyLimit;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Food> foods;

    public void setTimezone(String timezone) {
        if (isNotValidTimezone(timezone)) {
            this.timezone = "UTC";
        } else {
            this.timezone = timezone;
        }
    }

    public ZoneId resolveZoneId() {
        if (isNotValidTimezone(this.timezone)) {
            return ZoneId.of("UTC");
        }
        return ZoneId.of(this.timezone);
    }

    private boolean isNotValidTimezone(String timezoneId) {
        return timezoneId == null || timezoneId.isEmpty() || !AVAILABLE_ZONE_IDS.contains(timezoneId);
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
