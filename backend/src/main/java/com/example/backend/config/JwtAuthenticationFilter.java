package com.example.backend.config;

import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取token
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            logger.debug("Request URL: {}", request.getRequestURL());
            logger.debug("Authorization header: {}", authHeader);

            // 检查token是否存在且格式正确
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                logger.debug("Extracted token: {}", token);
                try {
                    username = jwtUtil.extractUsername(token);
                    logger.debug("Extracted username: {}", username);
                } catch (ExpiredJwtException e) {
                    logger.error("JWT token expired: {}", e.getMessage());
                } catch (Exception e) {
                    logger.error("Failed to extract username from token: {}", e.getMessage());
                }
            } else {
                logger.debug("Authorization header is missing or invalid");
            }

            // 如果token有效且用户未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Username is not null and user is not authenticated");
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("Loaded user details: {}", userDetails.getUsername());

                    // 验证token
                    try {
                        if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                            logger.debug("Token is valid for user: {}", username);
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            logger.debug("Authentication successful for user: {}", username);
                        } else {
                            logger.error("Token validation failed for user: {}", username);
                        }
                    } catch (SignatureException e) {
                        logger.error("Invalid JWT signature for user: {}", username);
                    } catch (ExpiredJwtException e) {
                        logger.error("JWT token expired for user: {}", username);
                    } catch (MalformedJwtException e) {
                        logger.error("Invalid JWT token format for user: {}", username);
                    } catch (UnsupportedJwtException e) {
                        logger.error("Unsupported JWT token for user: {}", username);
                    } catch (Exception e) {
                        logger.error("Token validation failed for user: {} - {}", username, e.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("Failed to load user details for username: {} - {}", username, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}