public class ConnectFour {
    private Board board;
    private int droppedDiscs = 0;

    public final static char FIRST_PLAYER = 'r', SECOND_PLAYER = 'g', EMPTY = 'o';

    //----------------------------------------------------------------------------------------------------------------//
    public ConnectFour() {
        this.board = new Board();
    }

    //----------------------------------------------------------------------------------------------------------------//
    public int getDroppedDiscsQty() {
        return droppedDiscs;
    }

    public void dropDisc(int col, char player) throws WrongColumnOrRowException {
        if (col < 0 || col >= Board.COLUMNS)
            throw new WrongColumnOrRowException();
        board.dropDisc(col, player);
        droppedDiscs++;
    }

    public int getDiscsInColumnQty(int col) {
        return board.getDiscsInColumnQty(col);
    }
}
