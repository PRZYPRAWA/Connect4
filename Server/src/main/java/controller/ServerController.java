package controller;

import applicationLogic.Board;
import applicationLogic.Broker;
import applicationLogic.ConnectFour;
import applicationLogic.exceptions.FullColumnException;
import applicationLogic.exceptions.WrongColumnOrRowException;
import org.eclipse.paho.client.mqttv3.*;

import java.util.*;

public class ServerController implements MqttCallback {
    private ConnectFour gameLogic;
    private Broker broker;

    private Map<Character, String> inGameClients; //<sign,clientID>
    private boolean atLeastOnePlayerWantRestart = false;

    //----------------------------------------------------------------------------------------------------------------//
    public ServerController(ConnectFour gameLogic) {
        this.gameLogic = gameLogic;
        this.broker = new Broker();
        inGameClients = new HashMap<>(2);
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void run() {
        broker.connect(this);
        System.out.println("Server is running now...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String textMessage = message.toString();
        if (messageFromPlayer(topic, getCurrentPlayer()) || needToReadMessageFromAll())
            checkMessage(topic, textMessage);
        else if (messageFromPlayer(topic, gameLogic.getNextPlayer())) {
            //powiadomienie zeby czekal spokojnie na swoja kolej todo
        } else {
            //powiadomienie ze juz jest 2 graczy todo
        }
    }

    private boolean messageFromPlayer(String topic, char player) {
        String playerId = inGameClients.get(player);
        return playerId != null && topic.contains(playerId);
    }

    private boolean needToReadMessageFromAll() {
        return inGameClients.size() < 2 || gameLogic.getResult() != ConnectFour.EMPTY;
    }

    private void checkMessage(String topic, String message) {
        if (topic.contains(Broker.FIELD_TOP) && message.contains(Broker.FIELD_CHOOSE_MSG))
            processFieldChooseMsg(message);
        else if (topic.contains(Broker.PREPARE_TOP))
            checkPreparationMessage(message);
    }

    private void processFieldChooseMsg(String message) {
        try {
            String[] splitedMsg = message.split(Broker.DELIMITER);
            int column = Integer.parseInt(splitedMsg[1]);
            nextTurn(column);
        } catch (NumberFormatException e) {
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.WRONG_COLUMN_MSG);
            publishBoardWithFieldMsg(getCurrentPlayer());
        }
    }

    private void publishBoardWithFieldMsg(char player) {
        String topPrefix = getPlayerTopic(player);
        broker.publish(topPrefix + Broker.BOARD_TOP, getBoardLookMsg(Character.toString(player)));
        broker.publish(topPrefix + Broker.FIELD_TOP, Broker.FIELD_REQUEST_MSG);
    }

    private String getPlayerTopic(char player) {
        return Broker.SPECIF_PLAYER_TOP + inGameClients.get(player) + "/";
    }

    private void checkPreparationMessage(String message) {
        if (message.contains(Broker.CLIENT_CONNECTED_MSG))
            processClientConnectedMsg(message);
        else if (message.contains(Broker.RESTART_REPLY_MSG))
            processRestartReplyMsg(message);
        else if (message.equals(Broker.START_GAME))
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.PREPARE_TOP, Broker.START_GAME);
    }

    //rowsQty:columnsQty:playerTurn:sign1:sign2:sig3 ...
    private String getBoardLookMsg(String playerTurn) {
        StringBuilder builder = new StringBuilder(Board.ROWS + Broker.DELIMITER + Board.COLUMNS + Broker.DELIMITER + playerTurn);
        for (int row = 0; row < Board.ROWS; row++)
            for (int col = 0; col < Board.COLUMNS; col++)
                builder.append(Broker.DELIMITER).append(gameLogic.getBoard().getSign(row, col));
        return builder.toString();
    }

    private void processClientConnectedMsg(String message) {
        String[] splited = message.split(Broker.DELIMITER);
        inGameClients.put(getCurrentPlayer(), splited[1]);

        if (inGameClients.size() < 2) { //need to wait for another player
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.PREPARE_TOP, Broker.WAITING_FOR_PLAYER_MSG);
            gameLogic.changePlayer();
        } else {
            broker.publish(Broker.ALL_PLAYERS_TOP + Broker.BOARD_TOP, getBoardLookMsg(Character.toString(getCurrentPlayer())));
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.FIELD_REQUEST_MSG);
        }
    }

    private void processRestartReplyMsg(String message) {
        String[] splited = message.split(Broker.DELIMITER);
        boolean restart = Boolean.parseBoolean(splited[1]);
        if (restart) {
            if (!atLeastOnePlayerWantRestart)
                atLeastOnePlayerWantRestart = true;
            else restartGame(); //both player confirm restart request
        } else {
            broker.publish(Broker.ALL_PLAYERS_TOP + Broker.RESULTS_TOP, Broker.END_GAME);
            System.out.println("Server is down");
            System.exit(0);
        }
    }

    private void restartGame() {
        gameLogic.restartGame();
        atLeastOnePlayerWantRestart = false;
        broker.publish(Broker.ALL_PLAYERS_TOP + Broker.PREPARE_TOP, Broker.START_GAME);
        broker.publish(Broker.ALL_PLAYERS_TOP + Broker.BOARD_TOP, getBoardLookMsg(Character.toString(getCurrentPlayer())));
        broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.FIELD_REQUEST_MSG);
    }

    private void nextTurn(int col) {
        try {
            gameLogic.dropDisc(col, getCurrentPlayer());
        } catch (FullColumnException e) {
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.FULL_COLUMN_MSG);
            publishBoardWithFieldMsg(getCurrentPlayer());
            return;
        } catch (WrongColumnOrRowException e) {
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.WRONG_COLUMN_MSG);
            publishBoardWithFieldMsg(getCurrentPlayer());
            return;
        }
        checkGameStatus();
    }

    private void checkGameStatus() {
        char result = gameLogic.getResult();
        if (result != ConnectFour.EMPTY)
            finishedCurrentGame(result);
        else {
            String nextPlayer = Character.toString(gameLogic.getNextPlayer());
            broker.publish(Broker.ALL_PLAYERS_TOP + Broker.BOARD_TOP, getBoardLookMsg(nextPlayer));
            gameLogic.changePlayer();
            broker.publish(getPlayerTopic(getCurrentPlayer()) + Broker.FIELD_TOP, Broker.FIELD_REQUEST_MSG);
        }
    }

    private void finishedCurrentGame(char result) {
        if (result == ConnectFour.DRAW)
            broker.publish(Broker.ALL_PLAYERS_TOP + Broker.RESULTS_TOP, Broker.DRAW_MSG);
        else
            broker.publish(Broker.ALL_PLAYERS_TOP + Broker.RESULTS_TOP, Broker.WINNER_MSG + Broker.DELIMITER + result);
        broker.publish(Broker.ALL_PLAYERS_TOP + Broker.BOARD_TOP, getBoardLookMsg("NO-ONE"));
        broker.publish(Broker.ALL_PLAYERS_TOP + Broker.PREPARE_TOP, Broker.RESTART_REQUEST_MSG);
    }

    public char getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
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
