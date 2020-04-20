package controller;

import logic.Broker;
import org.eclipse.paho.client.mqttv3.*;
import ui.GameCli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ClientController implements MqttCallback {
    public final static char FIRST_PLAYER_SIGN = 'X', SECOND_PLAYER_SIGN = 'Q';

    private String actualPlayerTurn = "NO-ONE";
    private GameCli gameCli;
    private Broker broker;

    //----------------------------------------------------------------------------------------------------------------//
    public ClientController(GameCli gameCli) {
        this.gameCli = gameCli;
        this.broker = new Broker(gameCli);
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void startGame() {
        broker.connect(this);
        String connectMsg = Broker.CLIENT_CONNECTED_MSG + Broker.DELIMITER + broker.getClientId();
        broker.publish(broker.getPlayerTopic() + Broker.PREPARE_TOP, connectMsg);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String textMessage = message.toString();
        if (topic.contains(Broker.BOARD_TOP)) {
            char[][] board = decodeBoardLookMsg(message.toString());
            gameCli.setBoardColumnsQty(board[0].length);
            gameCli.printActualTurn(actualPlayerTurn);
            gameCli.printBoard(board);
        } else if (topic.contains(Broker.FIELD_TOP))
            fieldTopicMsg(textMessage);
        else if (topic.contains(Broker.RESULTS_TOP))
            resultTopicMsg(textMessage);
        else if (topic.contains(Broker.PREPARE_TOP))
            preparationTopicMsg(textMessage);
        else if (topic.contains(Broker.SERVER_ERROR_TOP))
            broker.criticalErrorAction("Server internal error: " + textMessage);
    }

    private char[][] decodeBoardLookMsg(String msg) {
        String[] decoded = msg.split(Broker.DELIMITER);
        int rows = getNumberFromMsg(decoded[0]);
        int columns = getNumberFromMsg(decoded[1]);
        actualPlayerTurn = decoded[2];
        char[][] board = new char[rows][columns];

        for (int row = 0; row < rows; row++)
            for (int col = 0; col < columns; col++)
                board[row][col] = decoded[columns * row + col + 3].charAt(0);
        return board;
    }

    private int getNumberFromMsg(String number) {
        int result = 0;
        try {
            result = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            broker.criticalErrorAction("Cant read board look from server " + e.getMessage());
        }
        return result;
    }

    private void fieldTopicMsg(String message) {
        switch (message) {
            case Broker.FIELD_REQUEST_MSG: {
                String topic = broker.getPlayerTopic() + Broker.FIELD_TOP;
                broker.publish(topic, Broker.FIELD_CHOOSE_MSG + Broker.DELIMITER + gameCli.readColumn());
            }
            break;
            case Broker.WRONG_COLUMN_MSG:
                gameCli.printWrongColumnError();
                break;
            case Broker.FULL_COLUMN_MSG:
                gameCli.printFullColumnError();
                break;
            case Broker.OPPONENT_MOVE_MSG:
                gameCli.printOpponentTurn();
                break;
        }
    }

    private void resultTopicMsg(String message) {
        if (message.equals(Broker.DRAW_MSG))
            gameCli.printDrawMsg();
        else if (message.contains(Broker.WINNER_MSG)) {
            String[] decoded = message.split(Broker.DELIMITER);
            gameCli.printWinnerMsg(decoded[1].charAt(0));
        } else if (message.equals(Broker.END_GAME)) {
            gameCli.printEndGame();
            System.exit(0);
        }
    }

    private void preparationTopicMsg(String message) {
        switch (message) {
            case Broker.WAITING_FOR_PLAYER_MSG:
                gameCli.printWaitingForPlayers(true);
                break;
            case Broker.START_GAME:
                gameCli.printStartedMsg();
                break;
            case Broker.RESTART_REQUEST_MSG: {
                boolean restart = gameCli.restartGameInput();
                String restartReplyMsg = Broker.RESTART_REPLY_MSG + Broker.DELIMITER + restart;
                if (restart)
                    gameCli.printWaitingForPlayers(false);
                broker.publish(broker.getPlayerTopic() + Broker.PREPARE_TOP, restartReplyMsg);
            }
            break;
            case Broker.OTHER_PLAYERS_IN_GAME: {
                gameCli.printOtherPlayersInGame();
                System.exit(0);
            }
            break;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        //todo:
        //Called when the client lost the connection to the broker
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //todo
        //Called when a outgoing publish is complete
    }
}
