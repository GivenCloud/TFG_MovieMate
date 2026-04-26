package com.moviemate;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class MoviemateBackendApplicationMainTest {

    @Test
    void main_shouldDelegateToSpringApplicationRun() {
        String[] args = new String[]{"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            MoviemateBackendApplication.main(args);

            springApp.verify(() -> SpringApplication.run(MoviemateBackendApplication.class, args));
        }
    }
}
