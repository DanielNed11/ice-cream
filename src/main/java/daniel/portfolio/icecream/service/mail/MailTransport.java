package daniel.portfolio.icecream.service.mail;

public interface MailTransport {

    void send(OutgoingEmail email);
}
