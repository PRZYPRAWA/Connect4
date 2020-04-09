package ui;

import applicationLogic.Board;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class GameCli {
    private static final String COLUMN_DELIMITER = " | ", BOARD_MIDDLE_FRAME = "-";
    private static final String BOARD_FOOTER = getBoardFooter();
    public static final String COLUMN_IS_FULL_MSG = "COLUMN IS FULL", WRONG_COLUMN_MSG = "WRONG COLUMN";


    private PrintStream consoleOut = System.out;
    private Scanner consoleIn = new Scanner(System.in);

    private char lastPressedSign = '\0';

    //----------------------------------------------------------------------------------------------------------------//
    private static String getBoardFooter() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < supportLength(); i++)
            builder.append(BOARD_MIDDLE_FRAME);
        return builder.toString();
    }

    private static int supportLength() {
        return ((COLUMN_DELIMITER + BOARD_MIDDLE_FRAME).length() * 2 + COLUMN_DELIMITER.length() * (Board.COLUMNS + 1) + Board.COLUMNS);
    }

    public void printFullColumnError() {
        //todo:
    }

    public void printWrongColumnError() {
        consoleOut.println(WRONG_COLUMN_MSG);
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
        for (int i = 0; i < (COLUMN_DELIMITER + BOARD_MIDDLE_FRAME).length(); i++)
            consoleOut.print(" ");
        for (int col = 0; col < Board.COLUMNS; col++) {
            consoleOut.print(COLUMN_DELIMITER);
            consoleOut.print(col);
        }
    }

    public int readColumn() {
        while (true) {
            consoleOut.println("Choose column: ");
            String input = consoleIn.nextLine();
            consoleOut.println();
            try {
                int column = Integer.parseInt(input);
                return column;
            } catch (NumberFormatException e) {
                printWrongColumnError();
            }
        }
    }
}
