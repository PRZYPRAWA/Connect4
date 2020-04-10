package ui;

import applicationLogic.Board;

import java.io.PrintStream;
import java.util.Scanner;

public class GameCli {
    private static final String COLUMN_DELIMITER = " | ", BOARD_MIDDLE_FRAME = "-";
    private static final String BOARD_FOOTER = getBoardFooter();
    public static final String COLUMN_IS_FULL_MSG = "COLUMN IS FULL", WRONG_COLUMN_MSG = "WRONG COLUMN";

    private PrintStream consoleOut = System.out;
    private Scanner consoleIn = new Scanner(System.in);

    //----------------------------------------------------------------------------------------------------------------//
    private static String getBoardFooter() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < boardLength(); i++)
            builder.append(BOARD_MIDDLE_FRAME);
        return builder.toString();
    }

    private static int boardLength() {
        return ((COLUMN_DELIMITER + BOARD_MIDDLE_FRAME).length() * 2 + COLUMN_DELIMITER.length() * (Board.COLUMNS + 1) + Board.COLUMNS);
    }

    public void printFullColumnError() {
        printlnCentered(COLUMN_IS_FULL_MSG);
    }

    public void printWrongColumnError() {
        printlnCentered(WRONG_COLUMN_MSG);
    }

    public void printBoard(Board board) {
        for (int row = 0; row < Board.ROWS; row++) {
            consoleOut.print(COLUMN_DELIMITER + BOARD_MIDDLE_FRAME);
            for (int col = 0; col < Board.COLUMNS; col++)
                consoleOut.print(COLUMN_DELIMITER + board.getSign(row, col));
            consoleOut.println(COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + COLUMN_DELIMITER);
        }
        printBoardFooter();
    }

    private void printBoardFooter() {
        consoleOut.println(BOARD_FOOTER);
        printColumnIndexes();
        consoleOut.println(COLUMN_DELIMITER);
        consoleOut.println(BOARD_FOOTER);
    }

    private void printColumnIndexes() {
        consoleOut.print(COLUMN_DELIMITER + BOARD_MIDDLE_FRAME);
        for (int col = 0; col < Board.COLUMNS; col++)
            consoleOut.print(COLUMN_DELIMITER + col);
        consoleOut.print(COLUMN_DELIMITER + BOARD_MIDDLE_FRAME);
    }

    public int readColumn() {
        while (true) {
            consoleOut.println("Choose column: ");
            String input = consoleIn.nextLine();
            consoleOut.println();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printWrongColumnError();
            }
        }
    }

    public boolean readRestartGame() {
        while (true) {
            printlnCentered("Do you want to play once again? (y/n): ");
            String input = consoleIn.nextLine();
            consoleOut.println();
            if (input.toLowerCase().strip().equals("y"))
                return true;
            if (input.toLowerCase().strip().equals("n"))
                return false;
            printlnCentered("Incorrect decision! Give 'y' or 'n'");
        }
    }

    public void printActualTurn(char actualPlayerSign) {
        consoleOut.println(getBoardFooter());
        printlnCentered("Player turn: " + actualPlayerSign);
        consoleOut.println(getBoardFooter());
        consoleOut.println();
    }

    public void printDrawMsg() {
        printlnCentered("Unlucky. It's a draw");
    }

    public void printWinnerMsg(char winner) {
        printlnCentered("Congratulations. Winner is player: " + winner);
    }

    public void printStartedMsg() {
        consoleOut.println(getBoardFooter());
        printlnCentered("Connect4 Game");
        consoleOut.println(getBoardFooter());
        printlnCentered("Board is reset and ready for game!");
        consoleOut.println(getBoardFooter());
        consoleOut.println();
    }

    private void printlnCentered(String text) {
        setCaretToCenter(text.length());
        consoleOut.println(text);
    }

    private void setCaretToCenter(int textLength) {
        int boardLength = boardLength();
        int emptySpaces = 0;
        if (textLength < boardLength)
            emptySpaces = (boardLength - textLength) / 2;
        for (int i = 0; i < emptySpaces; i++)
            consoleOut.print(" ");
    }
}
