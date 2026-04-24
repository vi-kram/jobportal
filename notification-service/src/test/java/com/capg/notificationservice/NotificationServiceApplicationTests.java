package com.capg.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceApplicationTests {

    @Test
    void main_doesNotThrow() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                  .thenReturn(mock(ConfigurableApplicationContext.class));
            NotificationServiceApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(any(Class.class), any(String[].class)));
        }
    }

    @Test
    void constructor_canBeInstantiated() {
        assertNotNull(new NotificationServiceApplication());
    }
}
