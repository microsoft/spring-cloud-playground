package com.microsoft.azure.springcloudplayground.security;

import com.microsoft.azure.springcloudplayground.util.TelemetryProxy;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class GithubLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String TELEMETRY_EVENT_GITHUB_LOGIN = "playground-github-login";

    private final TelemetryProxy telemetryProxy = new TelemetryProxy();

    private void triggerGithubLoginSuccessEvent(@NonNull String username) {
        Map<String, String> properties = new HashMap<>();

        properties.put("username", username);

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_GITHUB_LOGIN, properties);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
        String username = oauthToken.getPrincipal().getAttributes().get("login").toString();

        this.triggerGithubLoginSuccessEvent(username);
    }
}
