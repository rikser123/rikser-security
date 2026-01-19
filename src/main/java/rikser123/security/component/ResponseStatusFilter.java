package rikser123.security.component;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.response.RikserResponseItem;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // После Security, но до других фильтров
public class ResponseStatusFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Перехватываем response перед записью
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    Object responseBody = exchange.getAttribute("responseBody");
                    if (responseBody instanceof RikserResponseItem) {
                        RikserResponseItem<?> response = (RikserResponseItem<?>) responseBody;
                        HttpStatus status = response.getHttpStatus();
                        if (status != null) {
                            exchange.getResponse().setStatusCode(status);
                            response.setHttpStatus(null);
                        }
                    }
                });
    }
}