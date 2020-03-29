public class FullColumnException extends Exception {
    private static final String EXCEPTION_MSG = "Column is full. Cannot add new disc";

    public FullColumnException() {
        super(EXCEPTION_MSG);
    }
}
