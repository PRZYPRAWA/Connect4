package ui;

import applicationLogic.Board;
import applicationLogic.ConnectFour;

import static ui.ConsoleColors.*;

import java.io.PrintStream;
import java.util.Scanner;

public class GameCli {
    private static final String COLUMN_DELIMITER = " | ", BOARD_MIDDLE_FRAME = "-";
    private static final String BOARD_FOOTER = getBoardFooter();
    public static final String COLUMN_IS_FULL_MSG = "COLUMN IS FULL", WRONG_COLUMN_MSG = "WRONG COLUMN";
    private static final String FIRST_PLAYER_COLOR = BLACK_BOLD + BLUE_BACKGROUND;
    private static final String SECOND_PLAYER_COLOR = BLACK_BOLD + YELLOW_BACKGROUND;
    private static final String BOARD_COLOR = BLACK_BOLD + GREEN_BACKGROUND;
    private static final String INPUT_COLOR = PURPLE_BRIGHT;

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
        printlnCentered(COLUMN_IS_FULL_MSG, RED_BOLD);
    }

    public void printWrongColumnError() {
        printlnCentered(WRONG_COLUMN_MSG, RED_BOLD);
    }

    //todo: zmienic print boarda na parametry z tablicy char
    public void printBoard(Board board) {
        for (int row = 0; row < Board.ROWS; row++) {
            consoleOut.print(BOARD_COLOR + COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + RESET);
            for (int col = 0; col < Board.COLUMNS; col++) {
                consoleOut.print(COLUMN_DELIMITER);
                printBoardSign(board.getSign(row, col));
            }
            consoleOut.println(COLUMN_DELIMITER + BOARD_COLOR + BOARD_MIDDLE_FRAME + COLUMN_DELIMITER + RESET);
        }
        printBoardFooter();
    }

    private void printBoardSign(char sign) {
        if (sign == ConnectFour.FIRST_PLAYER)
            consoleOut.print(FIRST_PLAYER_COLOR + sign + RESET);
        else if (sign == ConnectFour.SECOND_PLAYER)
            consoleOut.print(SECOND_PLAYER_COLOR + sign + RESET);
        else consoleOut.print(" ");
    }

    private void printBoardFooter() {
        consoleOut.println(BOARD_COLOR + BOARD_FOOTER + RESET);
        printColumnIndexes();
        consoleOut.println(BOARD_COLOR + BOARD_FOOTER + RESET);
    }

    private void printColumnIndexes() {
        consoleOut.print(BOARD_COLOR + COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + RESET);
        for (int col = 0; col < Board.COLUMNS; col++)
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

    public boolean readRestartGame() {
        while (true) {
            printCentered("Do you want to play once again? (y/n): ", INPUT_COLOR);
            String input = consoleIn.nextLine();
            consoleOut.println();
            if (input.toLowerCase().equals("y"))
                return true;
            if (input.toLowerCase().equals("n"))
                return false;
            printlnCentered("Incorrect decision! Give 'y' or 'n'", RED_BOLD);
        }
    }

    public void printActualTurn(char actualPlayerSign) {
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        printCentered("Player turn: ", WHITE);
        printBoardSign(actualPlayerSign);
        consoleOut.println();
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        consoleOut.println();
    }

    public void printDrawMsg() {
        printlnCentered("Unlucky. It's a draw", YELLOW_BOLD);
    }

    public void printWinnerMsg(char winner) {
        consoleOut.println();
        printCentered("Congratulations. Winner is player: ", GREEN_BOLD);
        printBoardSign(winner);
        consoleOut.println("\n" + BOARD_FOOTER);
    }

    public void printStartedMsg() {
        consoleOut.println(GREEN_BOLD + getBoardFooter() + RESET);
        printlnCentered("Connect4 Game", WHITE_BOLD);
        consoleOut.println(GREEN_BOLD + getBoardFooter() + RESET);
        printlnCentered("Board is reset and ready for game!", WHITE);
        consoleOut.println(GREEN_BOLD + getBoardFooter() + RESET);
        consoleOut.println();
    }

    private void printCentered(String text, String textColor) {
        printCentered(text, textColor, "");
    }

    private void printCentered(String text, String textColor, String backgroundColor) {
        setCaretToCenter(text.length());
        consoleOut.print(textColor + backgroundColor + text + RESET);
    }

    private void printlnCentered(String text, String textColor) {
        printlnCentered(text, textColor, "");
    }

    private void printlnCentered(String text, String textColor, String backgroundColor) {
        setCaretToCenter(text.length());
        consoleOut.println(textColor + backgroundColor + text + RESET);
    }

    private void setCaretToCenter(int textLength) {
        int boardLength = boardLength();
        int emptySpaces = 0;
        if (textLength < boardLength)
            emptySpaces = (boardLength - textLength) / 2;
        for (int i = 0; i < emptySpaces; i++)
            consoleOut.print(" ");
    }

    public void serverError(String errMessage) {
        printCentered("Server internal error: " + errMessage, RED_BOLD);
    }

    public void waitForAnyInput() {
        consoleOut.println("Press any button to continue ...");
        consoleIn.nextLine();
    }
}
