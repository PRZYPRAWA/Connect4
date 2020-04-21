package ui;

import logic.Broker;

import static ui.ConsoleColors.*;

import java.io.PrintStream;
import java.util.Scanner;

public class GameCli {
    private static final String COLUMN_DELIMITER = " | ", BOARD_MIDDLE_FRAME = "-";
    public static final String COLUMN_IS_FULL_MSG = "COLUMN IS FULL", WRONG_COLUMN_MSG = "WRONG COLUMN";
    private static final String FIRST_PLAYER_COLOR = BLACK_BOLD + BLUE_BACKGROUND;
    private static final String SECOND_PLAYER_COLOR = BLACK_BOLD + YELLOW_BACKGROUND;
    private static final String SPECIAL_FIELD_COLOR = WHITE_BOLD + RED_BACKGROUND;
    private static final String NO_COLOR = " ";
    private static final String BOARD_COLOR = BLACK_BOLD + GREEN_BACKGROUND;
    private static final String INPUT_COLOR = PURPLE_BRIGHT;
    private static final String CREDITS_COLOR = BLACK_BOLD + CYAN_BACKGROUND;
    private static final String AUTHORS_COLOR = GREEN_BOLD;
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

    public void printBoard(String[][] boardSigns, String[][] boardColorsMsg) {
        for (int row = 0; row < boardSigns.length; row++) {
            consoleOut.print(BOARD_COLOR + COLUMN_DELIMITER + BOARD_MIDDLE_FRAME + RESET);
            for (int col = 0; col < boardSigns[row].length; col++) {
                consoleOut.print(COLUMN_DELIMITER);
                printBoardSign(boardSigns[row][col].charAt(0), boardColorsMsg[row][col]);
            }
            consoleOut.println(COLUMN_DELIMITER + BOARD_COLOR + BOARD_MIDDLE_FRAME + COLUMN_DELIMITER + RESET);
        }
        printBoardFooter();
    }

    private void printBoardSign(char sign, String colorMsg) {
        String signColor = getSignColor(colorMsg);
        if (signColor.equals(NO_COLOR))
            consoleOut.print(NO_COLOR);
        else consoleOut.print(signColor + sign + RESET);
    }

    private String getSignColor(String colorMsg) {
        switch (colorMsg) {
            case Broker.EMPTY_COLOR:
                return NO_COLOR;
            case Broker.FIRST_COLOR:
                return FIRST_PLAYER_COLOR;
            case Broker.SECOND_COLOR:
                return SECOND_PLAYER_COLOR;
            default:
                return SPECIAL_FIELD_COLOR;
        }
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

    public void printActualTurn(String actualPlayerSign, String colorMsg) {
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        printCentered("Player turn: ", WHITE);
        if (actualPlayerSign.length() > 1) {
            consoleOut.println();
            printCentered(actualPlayerSign, CYAN_BOLD);
        } else
            printBoardSign(actualPlayerSign.charAt(0), colorMsg);
        consoleOut.println();
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        consoleOut.println();
    }

    public void printDrawMsg() {
        printlnCentered("Unlucky. It's a draw", YELLOW_BOLD);
    }

    public void printWinnerMsg(char winner, String colorMsg) {
        consoleOut.println();
        printCentered("Congratulations. Winner is player: ", GREEN_BOLD);
        printBoardSign(winner, colorMsg);
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

    public void printWaitingForPlayers(boolean withLobbyText) {
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        if (withLobbyText)
            printlnCentered("LOBBY", WHITE_BOLD);
        printlnCentered("Waiting for another player...", FIRST_PLAYER_COLOR);
        consoleOut.println(BLUE + getBoardFooter() + RESET);
    }

    public void printOpponentTurn() {
        printlnCentered("Opponent move...", FIRST_PLAYER_COLOR);
        consoleOut.println();
    }

    public void printEndGame() {
        consoleOut.println();
        printlnCentered("Game is finished", CREDITS_COLOR);
        printlnCentered("At least one player refused to play again", WHITE);
        consoleOut.println();
        printlnCentered("Thank you for game!", BOARD_COLOR);
        consoleOut.println();
        printlnCentered("Authors:", CREDITS_COLOR);
        consoleOut.println();
        printlnCentered("Lukasz Gajerski (www.github.com/Ukasz09)", AUTHORS_COLOR);
        printlnCentered("Pawel Piotrowski (www.github.com/PRZYPRAWA)", AUTHORS_COLOR);
        consoleOut.println();
        printlnCentered("Press any button to exit...", WHITE);
        consoleIn.nextLine();
    }

    public void printOtherPlayersInGame() {
        consoleOut.println(BLUE + getBoardFooter() + RESET);
        printlnCentered("LOBBY", WHITE_BOLD);
        printlnCentered("Can't join", WHITE_BOLD, RED_BACKGROUND);
        printlnCentered("Other players already in game", WHITE_BOLD);
        consoleOut.println(BLUE + getBoardFooter() + RESET);
    }

    public String readServerID() {
        printCentered("Server ID: ", AUTHORS_COLOR);
        String serverID = consoleIn.nextLine();
        consoleOut.println();
        return serverID;
    }
}
