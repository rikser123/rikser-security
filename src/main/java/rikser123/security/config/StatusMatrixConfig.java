package rikser123.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;
import rikser123.security.repository.entity.UserStatus;

import java.util.EnumSet;

/**
 * Конфигурация доступных состояний
 *
 */
@Configuration
public class StatusMatrixConfig {

    @Bean
    StatusMatrix<UserStatus> userStatusMatrix() {
        var statusMatrix = new StatusMatrixImpl<UserStatus>();

        statusMatrix.addTransition(UserStatus.REGISTERED, EnumSet.of(UserStatus.DEACTIVATED, UserStatus.EMAIL_ACTIVATED));
        statusMatrix.addTransition(UserStatus.EMAIL_ACTIVATED, EnumSet.of(UserStatus.DEACTIVATED));

        return statusMatrix;
    }

}
