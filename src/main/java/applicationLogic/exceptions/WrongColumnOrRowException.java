package applicationLogic.exceptions;

public class WrongColumnOrRowException extends Exception {
    private static final String exceptionMsg = "WRONG COLUMN OR ROW";

    public WrongColumnOrRowException() {
        super(exceptionMsg);
    }
}
