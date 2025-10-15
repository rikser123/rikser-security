package rikser123.security.utils;

import lombok.experimental.UtilityClass;
import rikser123.security.dto.response.RikserResponseItem;

import java.util.List;
import java.util.Map;

@UtilityClass
public class RikserResponseUtils {
    public static <T> RikserResponseItem<T> createResponse(
            boolean result,
            T data,
            Map<String, List<String>> errors,
            Map<String, List<String>> warnings
    ) {
        var response = new RikserResponseItem<T>();
        response.setData(data);
        response.setResult(result);
        response.setErrors(errors);
        response.setWarnings(warnings);
        return response;
    }

    public static <T> RikserResponseItem<T> createResponse(T data) {
        return createResponse(true, data, null, null);
    }

    public static RikserResponseItem createResponse(
            Map<String, List<String>> errors,
            Map<String, List<String>> warnings
    ) {
        return createResponse(false, null, errors, warnings);
    }
}
