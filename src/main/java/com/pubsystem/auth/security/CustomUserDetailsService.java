// ============================================
// OPTIONAL: Simpler CustomUserDetailsService.java (backup - keep the old one)
// Location: auth-service/src/main/java/com/pubsystem/auth/security/CustomUserDetailsService.java
// ============================================
package com.pubsystem.auth.security;

import com.pubsystem.auth.model.User;
import com.pubsystem.auth.repository.RoleRepository;
import com.pubsystem.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService extends DefaultOAuth2UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return CustomUserDetails.create(user);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("=== CustomUserDetailsService.loadUser() ===");
        System.out.println("OAuth2 user loaded from Google");

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        System.out.println("Email: " + email);

        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            System.out.println("User exists in database with ID: " + user.getId());
            // Return CustomUserDetails with the existing user
            return CustomUserDetails.create(user, attributes);
        } else {
            System.out.println("User does NOT exist - will be created in success handler");
            // Return the default OAuth2User - user will be created in success handler
            return oAuth2User;
        }
    }
}