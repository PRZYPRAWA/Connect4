package controller;

public class MqttProperty {
    public static final String SERVER_URI = "tcp://broker.mqttdashboard.com:1883";
    public static final int QoS = 1;

    public static final String DELIMITER = ":";

    //messages: field
    public static final String WRONG_COLUMN_MSG = "WRONG_COLUMN";
    public static final String FULL_COLUMN_MSG = "FULL_COLUMN";
    public static final String FIELD_REQUEST_MSG = "FIELD_REQUEST";
    public static final String FIELD_CHOOSE_MSG = "FIELD_CHOOSE"; //todo: DAC

    //messages: preparation
    public static final String GIVEN_SIGN_MSG = "GIVEN_SIGN";
    public static final String RESTART_REQUEST_MSG = "RESTART_REQUEST";
    public static final String RESTART_REPLY_MSG = "RESTART_REPLY";
    public static final String CLIENT_CONNECTED_MSG = "CLIENT_CONNECTED";
    public static final String START_GAME = "START_GAME";

    //messages: results
    public static final String DRAW_MSG = "DRAW";
    public static final String WINNER_MSG = "WINNER";

    //topics prefixes
    private static final String THIS_PLAYER_TOPICS = "connect4/player";
    private static final String ALL_PLAYERS_TOPICS = "connect4/all";

    //published topics
    private static final String FIELD_TOPIC = "/board/field";

    //subscribed topics
    public static final String RESULTS_TOPIC = ALL_PLAYERS_TOPICS + "/result";
    public static final String BOARD_LOOK_TOPIC = ALL_PLAYERS_TOPICS + "/board/look";
//    public static final String SERVER_ERROR_TOPIC = ALL_PLAYERS_TOPICS + "/serverError";

    //published + subscribed
    private static final String PLAYER_PREPARATION_TOPIC = "/preparation";

    //----------------------------------------------------------------------------------------------------------------//
    private char playerSign = '\0';

    //----------------------------------------------------------------------------------------------------------------//
    public void setPlayerSign(char playerSign) {
        this.playerSign = playerSign;
    }

    public char getPlayerSign() {
        return playerSign;
    }

    public final String getFieldTopic() {
        return THIS_PLAYER_TOPICS + playerSign + FIELD_TOPIC;
    }

    public final String getPreparationTopic() {
        return THIS_PLAYER_TOPICS + playerSign + PLAYER_PREPARATION_TOPIC;
    }
}
