package controller;

import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import ui.GameCli;

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
        gameCli.printStartedMsg();
        while (true) {
            gameCli.printActualTurn(gameLogic.getCurrentPlayer());
            gameCli.printBoard(gameLogic.getBoard());
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
        checkGameStatus();
        gameLogic.changePlayer();
    }

    private void checkGameStatus() {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                gameCli.printDrawMsg();
            else {
                gameCli.printWinnerMsg(result);
                gameCli.printBoard(gameLogic.getBoard());
            }
            finishOrRestartGame(gameCli.readRestartGame());
        }
    }

    private void finishOrRestartGame(boolean restart) {
        if (restart) {
            gameLogic.restartGame();
            gameCli.printStartedMsg();
        } else System.exit(0);

    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }


}
