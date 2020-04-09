package controller;

import applicationLogic.Board;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import ui.GameCli;

import java.util.Scanner;

public class Controller {
    private ConnectFour gameLogic;
    private GameCli gameCli;

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic, GameCli gameCli) {
        this.gameLogic = gameLogic;
        this.gameCli = gameCli;
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void startGame() {
        gameLogic.startGame();

        while (true) { //todo: tmp
            printGameStatus();
            nextTurn(readColumnInput());
        }
    }

    private int readColumnInput() {
        while (true) {
            int column = gameCli.readColumn();
            if (column >= 0 && column < Board.COLUMNS)
                return column;
            else gameCli.printWrongColumnError();
        }
    }

    public void nextTurn(int col) {
        //todo: dac unhandled
        try {
            gameLogic.dropDisc(col, gameLogic.getCurrentPlayer());
        } catch (FullColumnException e) {
            gameCli.printFullColumnError();
        } catch (WrongColumnOrRowException e) {
            gameCli.printWrongColumnError();
        }
        printGameStatus();
        gameLogic.changePlayer();
    }

    private void printGameStatus() {
        gameCli.printBoard(gameLogic.getBoard());
    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }


}
