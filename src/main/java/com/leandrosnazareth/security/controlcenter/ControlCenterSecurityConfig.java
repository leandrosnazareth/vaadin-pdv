package com.leandrosnazareth.security.controlcenter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;

import com.vaadin.controlcenter.starter.idm.IdentityManagementConfiguration;

@EnableWebSecurity
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
public class ControlCenterSecurityConfig extends IdentityManagementConfiguration {

    @Bean
    OidcUserService oidcUserService() {
        var userService = new OidcUserService();
        userService.setOidcUserMapper(ControlCenterSecurityConfig::mapOidcUser);
        return userService;
    }

    private static OidcUser mapOidcUser(OidcUserRequest userRequest, OidcUserInfo userInfo) {
        var authorities = mapAuthorities(userRequest, userInfo);
        var providerDetails = userRequest.getClientRegistration().getProviderDetails();
        var userNameAttributeName = providerDetails.getUserInfoEndpoint().getUserNameAttributeName();
        var oidcUser = StringUtils.hasText(userNameAttributeName)
                ? new DefaultOidcUser(authorities, userRequest.getIdToken(), userInfo, userNameAttributeName)
                : new DefaultOidcUser(authorities, userRequest.getIdToken(), userInfo);
        return new OidcUserAdapter(oidcUser);
    }
}
