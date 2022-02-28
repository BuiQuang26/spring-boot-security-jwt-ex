package com.example.demo.Repository;

import com.example.demo.Models.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class dataBase {

    private PasswordEncoder passwordEncoder;

    public dataBase(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner intData(UserRepository userRepository){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                User A = new User( "Quang", passwordEncoder.encode("123"), "USER");
                User B = new User( "Ngan", passwordEncoder.encode("123"), "ADMIN");

                System.out.println("insert data : " + userRepository.save(A));
                System.out.println("insert data : " + userRepository.save(B));
            }
        };
    }
}
