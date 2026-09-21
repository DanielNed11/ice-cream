package daniel.portfolio.icecream.exception;

public class MailSendFailedException extends RuntimeException {

    public MailSendFailedException(Throwable cause) {
        super(cause);
    }
}
