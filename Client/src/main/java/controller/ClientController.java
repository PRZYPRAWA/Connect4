package controller;

import logic.Broker;
import org.eclipse.paho.client.mqttv3.*;
import ui.GameCli;

import java.util.ArrayList;
import java.util.List;

public class ClientController implements MqttCallback {
    private String actualPlayerTurn = "NO-ONE";
    private String actualPlayerColorMsg = Broker.EMPTY_COLOR;
    private GameCli gameCli;
    private Broker broker;

    //----------------------------------------------------------------------------------------------------------------//
    public ClientController(GameCli gameCli) {
        this.gameCli = gameCli;
        this.broker = new Broker(gameCli);
    }

    //----------------------------------------------------------------------------------------------------------------//
    public void startGame() {
        String serverID = gameCli.readServerID();
        broker.connect(this, serverID);
        String connectMsg = Broker.CLIENT_CONNECTED_MSG + Broker.DELIMITER + broker.getClientId();
        broker.publish(broker.getPlayerTopic() + Broker.PREPARE_TOP, connectMsg);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String textMessage = message.toString();
        if (topic.contains(Broker.BOARD_TOP)) {
            List<String[][]> board = decodeBoardLookMsg(message.toString());
            gameCli.setBoardColumnsQty(board.get(0)[0].length);
            gameCli.printActualTurn(actualPlayerTurn, actualPlayerColorMsg);
            gameCli.printBoard(board.get(0), board.get(1));
        } else if (topic.contains(Broker.FIELD_TOP))
            fieldTopicMsg(textMessage);
        else if (topic.contains(Broker.RESULTS_TOP))
            resultTopicMsg(textMessage);
        else if (topic.contains(Broker.PREPARE_TOP))
            preparationTopicMsg(textMessage);
        else if (topic.contains(Broker.SERVER_ERROR_TOP))
            broker.criticalErrorAction("Server internal error: " + textMessage);
    }

    // List[boardSigns, boardColorsMsg]
    private List<String[][]> decodeBoardLookMsg(String msg) {
        String[] decoded = msg.split(Broker.DELIMITER);
        int rows = getNumberFromMsg(decoded[0]);
        int columns = getNumberFromMsg(decoded[1]);
        actualPlayerTurn = decoded[2];
        actualPlayerColorMsg = decoded[3];

        List<String[][]> result = new ArrayList<>(2);
        result.add(decodeBoardSigns(decoded, rows, columns));
        result.add(decodeBoardColorsMsg(decoded, rows, columns));
        return result;
    }

    private String[][] decodeBoardSigns(String[] decodedMsg, int allRows, int allColumns) {
        String[][] board = new String[allRows][allColumns];
        for (int row = 0; row < allRows; row++)
            for (int col = 0; col < allColumns; col++)
                board[row][col] = decodedMsg[allColumns * row + col + 4];
        return board;
    }

    private String[][] decodeBoardColorsMsg(String[] decodedMsg, int allRows, int allColumns) {
        int lastNotColorCharOffset = allRows * allColumns + 4;
        String[][] colors = new String[allRows][allColumns];
        for (int row = 0; row < allRows; row++)
            for (int col = 0; col < allColumns; col++)
                colors[row][col] = decodedMsg[lastNotColorCharOffset + allColumns * row + col];
        return colors;
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
            gameCli.printWinnerMsg(decoded[1].charAt(0), decoded[2]);
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
