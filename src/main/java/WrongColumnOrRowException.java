public class WrongColumnOrRowException extends Exception {
    static String msg = "WRONG COLUMN OR ROW";

    public WrongColumnOrRowException() {
        super(msg);
    }
}
