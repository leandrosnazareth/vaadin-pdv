package com.leandrosnazareth.security.controlcenter;

import com.leandrosnazareth.security.AppUserInfo;
import com.leandrosnazareth.security.AppUserPrincipal;
import com.leandrosnazareth.security.domain.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class OidcUserAdapter implements OidcUser, AppUserPrincipal {

    private final OidcUser delegate;
    private final AppUserInfo appUserInfo;

    public OidcUserAdapter(OidcUser oidcUser) {
        this.delegate = requireNonNull(oidcUser);
        this.appUserInfo = createAppUserInfo(oidcUser);
    }

    private static AppUserInfo createAppUserInfo(OidcUser oidcUser) {
        return new AppUserInfo() {
            private final UserId userId = UserId.of(oidcUser.getSubject());
            private final String preferredUsername = requireNonNull(oidcUser.getPreferredUsername());
            private final String fullName = requireNonNull(oidcUser.getFullName());
            private final ZoneId zoneId = parseZoneInfo(oidcUser.getZoneInfo());
            private final Locale locale = parseLocale(oidcUser.getLocale());

            @Override
            public UserId getUserId() {
                return userId;
            }

            @Override
            public String getPreferredUsername() {
                return preferredUsername;
            }

            @Override
            public String getFullName() {
                return fullName;
            }

            @Override
            public @Nullable String getProfileUrl() {
                return oidcUser.getProfile();
            }

            @Override
            public @Nullable String getPictureUrl() {
                return oidcUser.getPicture();
            }

            @Override
            public @Nullable String getEmail() {
                return oidcUser.getEmail();
            }

            @Override
            public ZoneId getZoneId() {
                return zoneId;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }
        };
    }

    static ZoneId parseZoneInfo(@Nullable String zoneInfo) {
        if (zoneInfo == null) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(zoneInfo);
        } catch (DateTimeException e) {
            return ZoneId.systemDefault();
        }
    }

    static Locale parseLocale(@Nullable String locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(locale);
    }

    @Override
    public AppUserInfo getAppUser() {
        return appUserInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
