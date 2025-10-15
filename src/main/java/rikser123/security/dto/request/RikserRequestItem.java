package rikser123.security.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class RikserRequestItem<T> {
    @Valid private T data;
    private String channel;
}
