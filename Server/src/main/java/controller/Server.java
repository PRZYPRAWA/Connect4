package controller;

import applicationLogic.Board;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

//todo: fix tests
public class Server implements MqttCallback {
    private ConnectFour gameLogic;
    private Broker broker;

    //todo: refactor
    private Map<Character, String> signWitClientID;
    private boolean atLeastOnePlayerWantRestart = false;

    //----------------------------------------------------------------------------------------------------------------//
    public Server(ConnectFour gameLogic) {
        this.gameLogic = gameLogic;
        this.broker = new Broker();
        signWitClientID = new HashMap<>(2);
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void run() {
        broker.connect(this);
    }

    @Override
    public void connectionLost(Throwable cause) {
        //todo:
        //Called when the client lost the connection to the broker
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        //todo: dac komunikat ze juz jest 2 graczy
        String textMessage = message.toString();
        if (topic.contains(Broker.SPECIFIED_PLAYER_TOPICS) && (messageFromCurrentPlayer(topic) || !isCorrectPlayersQty() || (gameLogic.getResult() != ConnectFour.EMPTY)))
            checkTopicsForSpecifiedPlayer(topic, textMessage, getCurrentPlayer());
    }

    private boolean messageFromCurrentPlayer(String topic) {
        String currentPlayerID;
        if (signWitClientID.get(getCurrentPlayer()) == null)
            currentPlayerID = "null"; //todo: poprawic
        else currentPlayerID = signWitClientID.get(getCurrentPlayer());
        return topic.contains(currentPlayerID);
    }

    private boolean isCorrectPlayersQty() {
        return signWitClientID.size() == 2;
    }

    private void checkTopicsForSpecifiedPlayer(String topic, String message, char player) {
        if (topic.contains(Broker.FIELD_TOPIC)) {
            if (message.contains(Broker.FIELD_CHOOSE_MSG))
                processFieldChooseMsg(message, player);
        } else if (topic.contains(Broker.PLAYER_PREPARATION_TOPIC)) {
            if (message.contains(Broker.CLIENT_CONNECTED_MSG))
                processClientConnectedMsg(player, message);
            else if (message.contains(Broker.RESTART_REPLY_MSG))
                processRestartReplyMsg(message);
            else if (message.equals(Broker.START_GAME)) {
                String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
                broker.publish(topicPrefix + Broker.PLAYER_PREPARATION_TOPIC, Broker.START_GAME);
            }
        }
    }

    private void processFieldChooseMsg(String message, char player) {
        String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
        try {
            String[] splitedMsg = message.split(Broker.DELIMITER);
            int column = Integer.parseInt(splitedMsg[1]);
            nextTurn(column, player);
        } catch (NumberFormatException e) {
            broker.publish(topicPrefix + Broker.FIELD_TOPIC, Broker.WRONG_COLUMN_MSG);
            sendFieldRequestWithBoardMessage(player);
        }
    }

    private String getBoardLookMsg() {
        StringBuilder builder = new StringBuilder(Board.ROWS + Broker.DELIMITER + Board.COLUMNS);
        for (int row = 0; row < Board.ROWS; row++)
            for (int col = 0; col < Board.COLUMNS; col++)
                builder.append(Broker.DELIMITER).append(gameLogic.getBoard().getSign(row, col));
        return builder.toString();
    }

    private void sendFieldRequestWithBoardMessage(char player) {
        String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
        broker.publish(Broker.BOARD_LOOK_TOPIC, getBoardLookMsg()); //todo: dac tylko dla poszczegolnych graczy
        broker.publish(topicPrefix + Broker.FIELD_TOPIC, Broker.FIELD_REQUEST_MSG);
    }

    private void processClientConnectedMsg(char player, String message) {
        String[] splited = message.split(Broker.DELIMITER);
        signWitClientID.put(player, splited[1]);

        String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
        String signMessage = Broker.GIVEN_SIGN_MSG + Broker.DELIMITER + player;
        broker.publish(topicPrefix + Broker.PLAYER_PREPARATION_TOPIC, signMessage);

        if (!isCorrectPlayersQty()) {
            broker.publish(topicPrefix + Broker.PLAYER_PREPARATION_TOPIC, Broker.WAITING_FOR_PLAYER_MSG);
            gameLogic.changePlayer();
        } else sendFieldRequestWithBoardMessage(getCurrentPlayer());
    }

    private void processRestartReplyMsg(String message) {
        String[] splited = message.split(Broker.DELIMITER);
        boolean restart = Boolean.parseBoolean(splited[1]);
        if (restart) {
            if (!atLeastOnePlayerWantRestart)
                atLeastOnePlayerWantRestart = true;
            else restartGame(); //both player confirm restart request
        } else
            broker.disconnect();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //todo
        //Called when a outgoing publish is complete
    }


    private void nextTurn(int col, char player) {
        try {
            gameLogic.dropDisc(col, player);
        } catch (FullColumnException e) {
            String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            broker.publish(topicPrefix + Broker.FIELD_TOPIC, Broker.FULL_COLUMN_MSG);
            sendFieldRequestWithBoardMessage(player);
            return;
        } catch (WrongColumnOrRowException e) {
            String topicPrefix = Broker.SPECIFIED_PLAYER_TOPICS + "/" + signWitClientID.get(player);
            broker.publish(topicPrefix + Broker.FIELD_TOPIC, Broker.WRONG_COLUMN_MSG);
            sendFieldRequestWithBoardMessage(player);
            return;
        }
        checkGameStatus(player);
    }

    private void checkGameStatus(char player) {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY) {
            if (result == ConnectFour.DRAW)
                broker.publish(Broker.RESULTS_TOPIC, Broker.DRAW_MSG);
            else
                broker.publish(Broker.RESULTS_TOPIC, Broker.WINNER_MSG + Broker.DELIMITER + result);
            broker.publish(Broker.BOARD_LOOK_TOPIC, getBoardLookMsg());
            broker.publish(Broker.ALL_PLAYERS_TOPICS + Broker.PLAYER_PREPARATION_TOPIC, Broker.RESTART_REQUEST_MSG);
        } else {
            gameLogic.changePlayer();
            sendFieldRequestWithBoardMessage(getCurrentPlayer());
        }
    }

    //todo: pozamieniac player w param na currentPlayer

    private void restartGame() {
        gameLogic.restartGame();
        broker.publish(Broker.ALL_PLAYERS_TOPICS + Broker.PLAYER_PREPARATION_TOPIC, Broker.START_GAME);
        sendFieldRequestWithBoardMessage(getCurrentPlayer());
    }


    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }
}
