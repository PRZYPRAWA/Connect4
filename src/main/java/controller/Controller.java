package controller;

import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import ui.GameCli;

public class Controller {
    private ConnectFour gameLogic;
    private GameCli gameCli;

    private boolean gameIsFinished = false;

    //----------------------------------------------------------------------------------------------------------------//
    public Controller(ConnectFour gameLogic, GameCli gameCli) {
        this.gameLogic = gameLogic;
        this.gameCli = gameCli;
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void startGame() {
        gameCli.printStartedMsg();
        while (!gameIsFinished) {
            printBoard();
            nextTurn(gameCli.readColumn());
        }
    }

    public void nextTurn(int col) {
        try {
            gameLogic.dropDisc(col, gameLogic.getCurrentPlayer());
        } catch (FullColumnException e) {
            gameCli.printFullColumnError();
            return;
        } catch (WrongColumnOrRowException e) {
            gameCli.printWrongColumnError();
            return;
        }
        printBoard();
        checkGameStatus();
        gameLogic.changePlayer();
    }

    private void printBoard() {
        gameCli.printActualTurn(gameLogic.getCurrentPlayer());
        gameCli.printBoard(gameLogic.getBoard());
    }

    private void checkGameStatus() {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                gameCli.printDrawMsg();
            else gameCli.printWinnerMsg(result);
            restartGame(gameCli.readRestartGame());
        }
    }

    private void restartGame(boolean value) {
        if (value) {
            gameLogic.restartGame();
            gameCli.printStartedMsg();
        }

    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }


}
