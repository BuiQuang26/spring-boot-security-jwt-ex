package com.example.demo.Sercurity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().equals("/api/login")){
            filterChain.doFilter(request, response);
        }else {
            if(request.getHeader(AUTHORIZATION) == null || !request.getHeader(AUTHORIZATION).startsWith("Bearer ")){
                filterChain.doFilter(request, response);
            }else{
                try {
                    String token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
                    //verifier
                    Algorithm algorithm = Algorithm.HMAC256("Quang");
                    JWTVerifier jwtVerifier =  JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = jwtVerifier.verify(token);
                    String username = decodedJWT.getSubject();
                    System.out.println("request : " + username);
                    String[] listRole = decodedJWT.getClaim("roles").asArray(String.class);

                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(listRole).forEach(role->{
                        authorities.add(new SimpleGrantedAuthority(role));
                    });

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterChain.doFilter(request,response);
                }catch (Exception e){
                    System.out.println("authorization : " + e.getMessage());
                    //responsive
                    response.setContentType(APPLICATION_JSON_VALUE);
                    response.setStatus(403);
                    new ObjectMapper().writeValue(response.getOutputStream(), new AuthenResponsive("False", "token invalid"));
                }
            }
        }
    }
}
