package daniel.portfolio.icecream.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(String message) {
        super(message);
    }
}
