package test.depaul.edu.test;

public class ServerInterface {
    public final class RequestType {
        public static final int Heartbeat = 0;
        public static final int RegisterUser = 1;
        public static final int GetGamesList = 2;
        public static final int CreateGame = 3;
        public static final int JoinGame = 4;
        public static final int ExitGame = 5;

        public static final int WolfwereChooseToKill = 6;
        public static final int SeerTurnFinished = 7;
        public static final int VoteToKill = 8;
    }

    public final class ResponseType {
        public static final int Error = 200;

        public static final int UpdateGameList = 100;
        public static final int UpdateGameInformation = 101;
        public static final int UpdatePlayersInformation = 102;

        public static final int GameStart = 103;
        public static final int GameOver = 104;
        public static final int WolfwereTurn = 105;
        public static final int SeerTurn = 106;
        public static final int DisscusionTurn = 107;
        public static final int DisscusionEnd = 108;
    }

    public final class ClientEventType {
        public static final int Connected = 10000;
        public static final int Disconnected = 10001;
    }
}
