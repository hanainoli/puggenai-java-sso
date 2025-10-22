// ============================================
// FIXED: OAuth2AuthenticationSuccessHandler.java
// Location: auth-service/src/main/java/com/pubsystem/auth/security/OAuth2AuthenticationSuccessHandler.java
// ============================================
package com.pubsystem.auth.security;

import com.pubsystem.auth.model.Role;
import com.pubsystem.auth.model.User;
import com.pubsystem.auth.repository.RoleRepository;
import com.pubsystem.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            System.out.println("=== OAuth2 Success Handler START ===");

            // Get OAuth2User
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Get user info from OAuth2
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");
            String sub = oAuth2User.getAttribute("sub");

            System.out.println("OAuth2 User Info:");
            System.out.println("  Email: " + email);
            System.out.println("  Name: " + name);
            System.out.println("  Picture: " + picture);
            System.out.println("  Sub: " + sub);

            // Find or create user
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User not found, creating new user...");
                user = createNewOAuthUser(email, name, picture, sub);
                System.out.println("New user created with ID: " + user.getId());
            } else {
                System.out.println("Existing user found with ID: " + user.getId());
                // Update user info
                user.setName(name);
                user.setProfilePicture(picture);
                user = userRepository.saveAndFlush(user);
            }

            // ✅ FIXED: Get actual user roles from database (not OAuth scopes)
            String roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.joining(","));

            if (roles.isEmpty()) {
                roles = "ROLE_USER";
            }

            System.out.println("User roles: " + roles);

            // Generate JWT token
            String token = tokenProvider.generateTokenFromEmail(
                    user.getEmail(),
                    user.getId(),
                    user.getName(),
                    roles,
                    user.getAuthProvider().name()
            );

            System.out.println("JWT Token generated successfully");
            System.out.println("Token: " + token.substring(0, Math.min(50, token.length())) + "...");

            // ✅ FIXED: Redirect to frontend with token AND user data
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback")
                    .queryParam("token", token)
                    .queryParam("userId", user.getId())
                    .queryParam("email", user.getEmail())
                    .queryParam("name", user.getName())
                    .queryParam("roles", roles)
                    .build().toUriString();

            System.out.println("Redirecting to: " + targetUrl);
            System.out.println("=== OAuth2 Success Handler END ===");

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            System.err.println("=== OAuth2 Success Handler ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "oauth_failed")
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    @Transactional
    private User createNewOAuthUser(String email, String name, String picture, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name != null ? name : email.split("@")[0]);
        user.setProfilePicture(picture);
        user.setAuthProvider(User.AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setIsActive(true);
        user.setIsVerified(true);
        user.setPassword(null);

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Save and flush immediately
        return userRepository.saveAndFlush(user);
    }
}