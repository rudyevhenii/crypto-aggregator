package dev.rudyevhenii.crypto_aggregator.auth.filter;

import dev.rudyevhenii.crypto_aggregator.auth.security.SecurityUserDetails;
import dev.rudyevhenii.crypto_aggregator.auth.service.JwtService;
import dev.rudyevhenii.crypto_aggregator.auth.service.TokenBlacklistService;
import dev.rudyevhenii.crypto_aggregator.auth.service.TokenType;
import dev.rudyevhenii.crypto_aggregator.auth.service.UserService;
import dev.rudyevhenii.crypto_aggregator.core.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwtToken = authHeader.substring(7);
        validateAccessTokenPassed(jwtToken);

        if (tokenBlacklistService.isBlacklisted(jwtToken)) {
            SecurityContextHolder.clearContext();
            throw new UnauthorizedException("Token is blacklisted");
        }
        String email = jwtService.extractSubject(jwtToken);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityUserDetails userDetails = (SecurityUserDetails) userService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwtToken, userDetails.getUser())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validateAccessTokenPassed(String jwtToken) {
        if (!jwtService.extractTokenType(jwtToken).equals(TokenType.ACCESS_TOKEN)) {
            throw new BadCredentialsException("Only ACCESS tokens are allowed here");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/register") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/refresh-token");
    }
}
