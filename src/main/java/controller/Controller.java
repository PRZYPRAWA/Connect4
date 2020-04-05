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
    public void nextTurn(int col) {
        try {
            gameLogic.dropDisc(col, gameLogic.getCurrentPlayer());
        } catch (FullColumnException e) {
            gameCli.printFullColumnError();
        } catch (WrongColumnOrRowException e) {
            gameCli.printWrongColumnError();
        }
        gameLogic.changePlayer();
        gameCli.printGameStatus();
    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }

    public void startGame() {
        gameLogic.startGame();
    }
}
