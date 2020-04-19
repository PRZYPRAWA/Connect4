package ui;

import controller.ClientController;

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
    private static final int DEFAULT_COLUMNS_QTY = 6;

    private PrintStream consoleOut = System.out;
    private Scanner consoleIn = new Scanner(System.in);
    private int columnsQty = DEFAULT_COLUMNS_QTY;

    //----------------------------------------------------------------------------------------------------------------//
    public void setBoardColumnsQty(int boardColumnsQty) {
        this.columnsQty = boardColumnsQty;
    }

    private String getBoardFooter() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < boardLength(); i++)
            builder.append(BOARD_MIDDLE_FRAME);
        return builder.toString();
    }

    private int boardLength() {
        return ((COLUMN_DELIMITER + BOARD_MIDDLE_FRAME).length() * 2 + COLUMN_DELIMITER.length() * (columnsQty + 1) + columnsQty);
    }

    public void printFullColumnError() {
        printlnCentered(COLUMN_IS_FULL_MSG, RED_BOLD);
    }

    public void printWrongColumnError() {
        printlnCentered(WRONG_COLUMN_MSG, RED_BOLD);
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
        printBoardFooter();
    }

    private void printBoardSign(char sign) {
        if (sign == ClientController.FIRST_PLAYER_SIGN)
            consoleOut.print(FIRST_PLAYER_COLOR + sign + RESET);
        else if (sign == ClientController.SECOND_PLAYER_SIGN)
            consoleOut.print(SECOND_PLAYER_COLOR + sign + RESET);
        else consoleOut.print(" ");
    }

    private void printBoardFooter() {
        String boardFooter = getBoardFooter();
        consoleOut.println(BOARD_COLOR + boardFooter + RESET);
        printColumnIndexes();
        consoleOut.println(BOARD_COLOR + boardFooter + RESET);
    }

    private void printColumnIndexes() {
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

    public boolean restartGameInput() {
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

    public void printActualTurn(String actualPlayerSign) {
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        printCentered("Player turn: ", WHITE);
        if (actualPlayerSign.length() > 1) {
            consoleOut.println();
            printCentered(actualPlayerSign, CYAN_BOLD);
        } else
            printBoardSign(actualPlayerSign.charAt(0));
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
        consoleOut.println("\n" + getBoardFooter());
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

    public void connectionError(String errMessage) {
        consoleOut.print(RED_BOLD + "Server internal error: " + errMessage + RESET);
    }

    public void waitForAnyInput() {
        consoleOut.println("Press any button to continue ...");
        consoleIn.nextLine();
    }

    public void printWaitingForPlayers() {
        printlnCentered("Lobby: waiting for another player to start a game...", FIRST_PLAYER_COLOR);
    }

    public void printEndGame() {
        printlnCentered("Game is finished", FIRST_PLAYER_COLOR);
        printlnCentered("Authors: Lukasz Gajerski, Pawel Piotrowski", INPUT_COLOR);
    }
}
