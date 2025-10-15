package rikser123.security.validation;


import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rikser123.security.dto.request.CreateUserRequestDto;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class IsEqualConstraintTest {
    private ConstraintValidatorContext context;
    private IsEqual annotation;

    @BeforeEach()
    void init() {
        context = Mockito.mock(ConstraintValidatorContext.class);
        annotation = getIsEqualAnnotation("password", "passwordConfirmation");
    }

    @Test
    void shouldDetectEqual() {
        var constraint = new IsEqualConstraint();
        var userRequestDto = new CreateUserRequestDto();
        userRequestDto.setPassword("a");
        userRequestDto.setPasswordConfirmation("a");

        constraint.initialize(annotation);
        var result = constraint.isValid(userRequestDto, context);
        assertTrue(result);
    }

    @Test
    void shouldDetectNotEqual() {
        var constraint = new IsEqualConstraint();
        var userRequestDto = new CreateUserRequestDto();
        userRequestDto.setPassword("ab");
        userRequestDto.setPasswordConfirmation("a");

        constraint.initialize(annotation);
        var result = constraint.isValid(userRequestDto, context);
        assertFalse(result);
    }

    @Test
    void shouldDetectIfNoFields() {
        var constraint = new IsEqualConstraint();
        var userRequestDto = new CreateUserRequestDto();
        userRequestDto.setPassword("ab");
        userRequestDto.setPasswordConfirmation("a");

        annotation = getIsEqualAnnotation("field1", "field2");

        constraint.initialize(annotation);
        var result = constraint.isValid(userRequestDto, context);
        assertFalse(result);
    }

    private IsEqual getIsEqualAnnotation(String firstFieldValue, String secondFieldValue) {
        var annotation = new ConstraintAnnotationDescriptor.Builder<>(IsEqual.class);
        annotation.setAttribute("firstField", firstFieldValue);
        annotation.setAttribute("secondField", secondFieldValue);

        return annotation.build().getAnnotation();

    }
}
