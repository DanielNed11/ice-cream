package daniel.portfolio.icecream.exception;

public class ProductSlugAlreadyExistsException extends RuntimeException {

    public ProductSlugAlreadyExistsException(String message) {
        super(message);
    }
}
