package ui;

import controller.Controller;

import static ui.ConsoleColors.*;

import java.io.PrintStream;
import java.util.Scanner;

public class GameCli {
    private static final String COLUMN_DELIMITER = " | ", BOARD_MIDDLE_FRAME = "-";
    public static final String COLUMN_IS_FULL_MSG = "COLUMN IS FULL", WRONG_COLUMN_MSG = "WRONG COLUMN";
    private static final String FIRST_PLAYER_COLOR = BLACK_BOLD + BLUE_BACKGROUND;
    private static final String SECOND_PLAYER_COLOR = BLACK_BOLD + YELLOW_BACKGROUND;
    private static final String BOARD_COLOR = BLACK_BOLD + GREEN_BACKGROUND;
    private static final String INPUT_COLOR = PURPLE_BRIGHT;

    private PrintStream consoleOut = System.out;
    private Scanner consoleIn = new Scanner(System.in);

    //----------------------------------------------------------------------------------------------------------------//
    private static String getBoardFooter(int columnsQty) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < boardLength(columnsQty); i++)
            builder.append(BOARD_MIDDLE_FRAME);
        return builder.toString();
    }

    private static int boardLength(int columnsQty) {
        return ((COLUMN_DELIMITER + BOARD_MIDDLE_FRAME).length() * 2 + COLUMN_DELIMITER.length() * (columnsQty + 1) + columnsQty);
    }

    public void printFullColumnError(int columnsQty) {
        printlnCentered(COLUMN_IS_FULL_MSG, RED_BOLD, columnsQty);
    }

    public void printWrongColumnError(int columnsQty) {
        printlnCentered(WRONG_COLUMN_MSG, RED_BOLD, columnsQty);
    }


    public void printBoard(char[][] board) {
        for (int row = 0; row < board.length; row++) {
            consoleOut.print(BOARD_COLOR + COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + RESET);
            for (int col = 0; col < board[row].length; col++) {
                consoleOut.print(COLUMN_DELIMITER);
                printBoardSign(board[row][col]);
            }
            consoleOut.println(COLUMN_DELIMITER + BOARD_COLOR + BOARD_MIDDLE_FRAME + COLUMN_DELIMITER + RESET);
        }
        printBoardFooter(board[0].length);
    }


    private void printBoardSign(char sign) {
        if (sign == Controller.FIRST_PLAYER_SIGN)
            consoleOut.print(FIRST_PLAYER_COLOR + sign + RESET);
        else if (sign == Controller.SECOND_PLAYER_SIGN)
            consoleOut.print(SECOND_PLAYER_COLOR + sign + RESET);
        else consoleOut.print(" ");
    }

    private void printBoardFooter(int columnsQty) {
        String boardFooter = getBoardFooter(columnsQty);
        consoleOut.println(BOARD_COLOR + boardFooter + RESET);
        printColumnIndexes(columnsQty);
        consoleOut.println(BOARD_COLOR + boardFooter + RESET);
    }

    private void printColumnIndexes(int columnsQty) {
        consoleOut.print(BOARD_COLOR + COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + RESET);
        for (int col = 0; col < columnsQty; col++)
            consoleOut.print(COLUMN_DELIMITER + INPUT_COLOR + col + RESET);
        consoleOut.println(COLUMN_DELIMITER + BOARD_COLOR + BOARD_MIDDLE_FRAME + COLUMN_DELIMITER + RESET);
    }

    public int readColumn() {
        consoleOut.println();
        consoleOut.print(INPUT_COLOR + "Choose column: " + RESET);
        String input = consoleIn.nextLine();
        consoleOut.println();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean readRestartGame(int columnsQty) {
        while (true) {
            printCentered("Do you want to play once again? (y/n): ", INPUT_COLOR, columnsQty);
            String input = consoleIn.nextLine();
            consoleOut.println();
            if (input.toLowerCase().equals("y"))
                return true;
            if (input.toLowerCase().equals("n"))
                return false;
            printlnCentered("Incorrect decision! Give 'y' or 'n'", RED_BOLD, columnsQty);
        }
    }

    public void printActualTurn(char actualPlayerSign, int columnQty) {
        consoleOut.println(BLUE + getBoardFooter(columnQty) + RESET);
        printCentered("Player turn: ", WHITE, columnQty);
        printBoardSign(actualPlayerSign);
        consoleOut.println();
        consoleOut.println(BLUE + getBoardFooter(columnQty) + RESET);
        consoleOut.println();
    }

    public void printDrawMsg(int columnsQty) {
        printlnCentered("Unlucky. It's a draw", YELLOW_BOLD, columnsQty);
    }

    public void printWinnerMsg(char winner, int columnsQty) {
        consoleOut.println();
        printCentered("Congratulations. Winner is player: ", GREEN_BOLD, columnsQty);
        printBoardSign(winner);
        consoleOut.println("\n" + getBoardFooter(columnsQty));
    }

    public void printStartedMsg(int columnQty) {
        consoleOut.println(GREEN_BOLD + getBoardFooter(columnQty) + RESET);
        printlnCentered("Connect4 Game", WHITE_BOLD, columnQty);
        consoleOut.println(GREEN_BOLD + getBoardFooter(columnQty) + RESET);
        printlnCentered("Board is reset and ready for game!", WHITE, columnQty);
        consoleOut.println(GREEN_BOLD + getBoardFooter(columnQty) + RESET);
        consoleOut.println();
    }

    private void printCentered(String text, String textColor, int columnsQty) {
        printCentered(text, textColor, "", columnsQty);
    }

    private void printCentered(String text, String textColor, String backgroundColor, int columnsQty) {
        setCaretToCenter(text.length(), columnsQty);
        consoleOut.print(textColor + backgroundColor + text + RESET);
    }

    private void printlnCentered(String text, String textColor, int columnsQty) {
        printlnCentered(text, textColor, "", columnsQty);
    }

    private void printlnCentered(String text, String textColor, String backgroundColor, int columnsQty) {
        setCaretToCenter(text.length(), columnsQty);
        consoleOut.println(textColor + backgroundColor + text + RESET);
    }

    private void setCaretToCenter(int textLength, int columnsQty) {
        int boardLength = boardLength(columnsQty);
        int emptySpaces = 0;
        if (textLength < boardLength)
            emptySpaces = (boardLength - textLength) / 2;
        for (int i = 0; i < emptySpaces; i++)
            consoleOut.print(" ");
    }

    public void serverError(String errMessage) {
        consoleOut.print(RED_BOLD + "Server internal error: " + errMessage + RESET);
    }

    public void waitForAnyInput() {
        consoleOut.println("Press any button to continue ...");
        consoleIn.nextLine();
    }
}
