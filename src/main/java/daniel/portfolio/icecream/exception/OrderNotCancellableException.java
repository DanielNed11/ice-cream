package daniel.portfolio.icecream.exception;

public class OrderNotCancellableException extends RuntimeException {

    public OrderNotCancellableException(String message) {
        super(message);
    }
}
