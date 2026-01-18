package rikser123.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import rikser123.bundle.utils.RikserResponseUtils;

import java.io.IOException;

/**
 * Класс для для преобразования стандарного ответа от SpringSecurity в необходимый формат
 *
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Qualifier("customObjectMapper")
    private static ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var responseBody = RikserResponseUtils.createResponse("Доступ к запрашиваемому ресурсу запрещен", HttpStatus.FORBIDDEN);
        var jsonText = jsonMapper.writer().writeValueAsString(responseBody);

        response.getWriter().write(jsonText);
    }
}
