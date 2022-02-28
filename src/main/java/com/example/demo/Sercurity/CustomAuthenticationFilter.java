package com.example.demo.Sercurity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.Models.User;
import com.example.demo.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRepository userRepo;
    private final AuthenticationManager authenticationManager;
    private Map<String, String> userMap = null;

    public CustomAuthenticationFilter(UserRepository userRepo, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            InputStream inputStream = request.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            userMap = mapper.readValue(inputStream, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(userMap == null || userMap.get("username") == null || userMap.get("password") == null){
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(403);
            try {
                new ObjectMapper().writeValue(response.getOutputStream(),
                            new AuthenResponsive("False", "username or password incorrect"));
            } catch (IOException e) {
                System.out.println("authentication err : " + e.getMessage());
            }
            return null;
        }

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userMap.get("username"),userMap.get("password")));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user = userRepo.findByUsername(userMap.get("username"));
        List<String> listRole = new ArrayList<>();
        listRole.add(user.getRole());
        //create jwt
        Algorithm algorithm = Algorithm.HMAC256("Quang");
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", listRole)
                .sign(algorithm);
        String refresh_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60*60*1000))
                .sign(algorithm);

        //responsive
        Map<String, String> tokens = new HashMap<>();
        tokens.put("username", user.getUsername());
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), new AuthenResponsive("OK", "login successfully", tokens));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(403);
        new ObjectMapper().writeValue(response.getOutputStream(), new AuthenResponsive("False", "username or password incorrect"));

    }
}
