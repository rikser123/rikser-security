package rikser123.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import rikser123.bundle.utils.RikserResponseUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(@Qualifier("customObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
        return Mono.defer(() -> {
            try {
                var responseBody = RikserResponseUtils.createResponse(
                        "Доступ к запрашиваемому ресурсу запрещен",
                        HttpStatus.FORBIDDEN
                );

                String jsonText = objectMapper.writer().writeValueAsString(responseBody);
                byte[] bytes = jsonText.getBytes(StandardCharsets.UTF_8);

                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));

            } catch (Exception e) {
                log.error("Error writing authentication error response", e);

                // Fallback: простой текст в случае ошибки
                String errorMessage = "{\"error\":\"Authentication failed\"}";
                DataBuffer buffer = exchange.getResponse().bufferFactory()
                        .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
        });
    }
}