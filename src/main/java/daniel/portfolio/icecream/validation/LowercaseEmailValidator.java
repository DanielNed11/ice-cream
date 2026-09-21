package daniel.portfolio.icecream.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ObjectUtils;

public class LowercaseEmailValidator implements ConstraintValidator<LowercaseEmail, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (ObjectUtils.isEmpty(value)) {
            return true;
        }

        return value.equals(value.toLowerCase());
    }
}
