package io.viren.msscssm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"io.viren.msscssm.repository"})
public class MsscSsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsscSsmApplication.class, args);
    }

}
