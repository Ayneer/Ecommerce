package com.ias.ecommerce.security;

import com.ias.ecommerce.exception.ApiResponse;
import io.jsonwebtoken.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try{
            if(jwtUtil.hasToken(httpServletRequest)){
                Claims claims = jwtUtil.validateToken(httpServletRequest);
                if(claims.get(jwtUtil.getClaimName()) != null){
                    setUpSpringAuthentication(claims);
                }else{
                    SecurityContextHolder.clearContext();
                }
            }else{
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IOException exception){
            sendJwtAuthError(exception.getMessage(), httpServletResponse);
        }
    }

    private void setUpSpringAuthentication(Claims claims){
        @SuppressWarnings("unchecked")
        List<String> authorities = (List) claims.get(jwtUtil.getClaimName());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void sendJwtAuthError(String message, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        ApiResponse apiResponse = new ApiResponse(HttpStatus.FORBIDDEN, message, HttpStatus.FORBIDDEN.value(), true);
        httpServletResponse.getWriter().write(jwtUtil.objectToJson(apiResponse));
    }

}
