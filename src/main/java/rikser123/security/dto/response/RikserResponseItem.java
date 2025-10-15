package rikser123.security.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RikserResponseItem<T> {
    private boolean result;
    private T data;
    private Map<String, List<String>> errors;
    private Map<String, List<String>> warnings;
    private String message;
}
