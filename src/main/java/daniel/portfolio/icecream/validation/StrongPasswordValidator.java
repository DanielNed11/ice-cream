package daniel.portfolio.icecream.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        if (value.length() < 8) {
            return false;
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean special = false;
        boolean digits = false;

        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) {
                uppercase = true;
            } else if (Character.isLowerCase(c)) {
                lowercase = true;
            } else if (Character.isDigit(c)) {
                digits = true;
            } else if (!Character.isWhitespace(c)) {
                special = true;
            }
        }

        return uppercase && lowercase && special && digits;
    }
}
