package daniel.portfolio.icecream.service;

public final class OrderReference {

    private OrderReference() {
    }

    // The stored reference is bare (B8B20784) so it is safe in a URL path --
    // a leading '#' would be read as a fragment and never reach the server.
    // The hash is added only where the value is shown to a person.
    public static String display(String reference) {
        return "#" + reference;
    }
}
