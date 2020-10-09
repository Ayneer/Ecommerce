package com.ias.ecommerce.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtUtil {

    private static final String CLAIM_NAME = "authorities";
    private static final Integer TOKEN_HOURS_EXPIRE = 12;
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";
    private static final String SECRET_KEY = "IAS_ECOMMERCE";
    private static final String TOKEN_ID = "softenJWT";

    public Claims validateToken(HttpServletRequest httpServletRequest) {
        String jwtToken = httpServletRequest.getHeader(HEADER).replace(PREFIX, "");
        return Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(jwtToken).getBody();
    }

    public boolean hasToken(HttpServletRequest httpServletRequest){
        String authenticationHeader = httpServletRequest.getHeader(HEADER);
        return authenticationHeader != null && authenticationHeader.startsWith(PREFIX);
    }

    public String getJwtToken(String username, String roleName){
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roleName);

        String token = Jwts
                .builder()
                .setId(TOKEN_ID)
                .setSubject(username)
                .claim(CLAIM_NAME, grantedAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
                .setIssuedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(LocalDateTime.now().plusHours(TOKEN_HOURS_EXPIRE).toInstant(ZoneOffset.UTC)))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes()).compact();

        return PREFIX + token;
    }

    public String getClaimName() {
        return CLAIM_NAME;
    }

    public String objectToJson(Object object) throws JsonProcessingException {
        if(object != null){
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        }
        return null;
    }
}
