package dev.rudyevhenii.crypto_aggregator.auth.filter;

import dev.rudyevhenii.crypto_aggregator.auth.security.SecurityUserDetails;
import dev.rudyevhenii.crypto_aggregator.auth.service.JwtService;
import dev.rudyevhenii.crypto_aggregator.auth.service.TokenBlacklistServiceImpl;
import dev.rudyevhenii.crypto_aggregator.auth.service.UserService;
import dev.rudyevhenii.crypto_aggregator.core.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklistServiceImpl tokenBlacklistService;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthFilter(
            JwtService jwtService,
            UserService userService,
            TokenBlacklistServiceImpl tokenBlacklistService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwtToken = authHeader.substring(7);

        if (tokenBlacklistService.isBlacklisted(jwtToken)) {
            SecurityContextHolder.clearContext();
            exceptionResolver.resolveException(request, response, null,
                    new UnauthorizedException("Token is blacklisted"));
            return;
        }

        try {
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
        } catch (Exception ex) {
            exceptionResolver.resolveException(request, response, null, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }
}
