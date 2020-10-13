package com.ias.ecommerce.security;

import com.ias.ecommerce.exception.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntityPoint implements AuthenticationEntryPoint {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse apiResponse = new ApiResponse(HttpStatus.FORBIDDEN, authException.getMessage(), HttpStatus.FORBIDDEN.value(), true);
        res.getWriter().write(jwtUtil.objectToJson(apiResponse));
    }

}
