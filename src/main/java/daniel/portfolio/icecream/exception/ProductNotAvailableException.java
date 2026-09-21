package daniel.portfolio.icecream.exception;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(String message) {
        super(message);
    }
}
