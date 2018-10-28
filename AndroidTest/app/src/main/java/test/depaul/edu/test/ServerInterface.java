package test.depaul.edu.test;

public class ServerInterface {
    public final class RequestType {
        public static final int Heartbeat = 0;
        public static final int RegisterUser = 1;
        public static final int GetGamesList = 2;
        public static final int CreateGame = 3;
        public static final int JoinGame = 4;
        public static final int ExitGame = 5;
    }

    public final class ResponseType {
        public static final int Error = 200;

        public static final int UpdateGameList = 100;
        public static final int UpdateGameInformation = 101;
        public static final int UpdatePlayersInformation = 102;
    }

    public final class ClientEventType {
        public static final int Connected = 10000;
        public static final int Disconnected = 10001;
    }
}
