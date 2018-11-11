package test.depaul.edu.test;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameClient implements IServerConnectorListener {
    private static GameClient instance = new GameClient();

    private ServerConnector connector = null;
    private GameClient() {
    }

    public static void Connect() {
        instance.connect();
    }

    public static void Disconnect() {
        instance.disconnect();
    }

    public static void SendMessage(Message msg) {
        instance.sendMessage(msg);
    }

    public static void SendMessage(Message msg, OnMessageListener listener) {
        instance.sendMessage(msg, listener);
    }

    public static void AddListener(int type, OnMessageListener listener) {
        instance.addListenerByType(type, listener);
    }

    public static void RemoveListener(OnMessageListener listener) {
        instance.removeListener(listener);
    }

    public static void RemoveAllListner() {
        instance.removeAllListener();
    }

    private void connect() {
        if(connector == null) {
            connector = new ServerConnector(this);
            connector.connect();
        }
    }

    private void disconnect() {
        if(connector != null) {
            connector.disconnect();
        }
    }

    private void sendMessage(Message msg) {
        if(connector != null) {
            connector.sendMessage(msg.getJSONString());
        }
    }

    private void sendMessage(Message msg, OnMessageListener listener) {
        if(connector != null) {
            this.addListenerByRequestId(msg.requestId, listener);
            connector.sendMessage(msg.getJSONString());
        }
    }

    private Map<Integer, OnMessageListener> requestIdListeners  = new HashMap<>();
    private void addListenerByRequestId(int requestId, OnMessageListener listener) {
        requestIdListeners.put(requestId, listener);
    }

    private Map<Integer, ArrayList<OnMessageListener>> typeListeners = new HashMap<>();
    private void addListenerByType(int type, OnMessageListener listener) {
        if(!typeListeners.containsKey(type)) {
            typeListeners.put(type, new ArrayList<OnMessageListener>());
        }
        typeListeners.get(type).add(listener);
    }

    private void removeListener(OnMessageListener listener) {
        for ( ArrayList<OnMessageListener> list : typeListeners.values()) {
            list.remove(listener);
        }
    }

    private void removeAllListener() {
        typeListeners.clear();
    }

    private Handler heartbeat = new Handler();
    private Runnable heartbeatTask = new Runnable() {
        @Override
        public void run() {
            SendMessage(new Message(ServerInterface.RequestType.Heartbeat));
            heartbeat.postDelayed(this, 15000);
        }
    };
    private void startHeartbeat() {
        heartbeat.post(heartbeatTask);
    }

    private void stopHeartbeat() {
        heartbeat.removeCallbacks(heartbeatTask);
    }

    public void onServerConnected() {
        this.notifyListeners(new Message(ServerInterface.ClientEventType.Connected));
//        this.startHeartbeat();
    }

    public void onServerDisconnected() {
        connector = null;
        this.stopHeartbeat();
        this.notifyListeners(new Message(ServerInterface.ClientEventType.Disconnected));
    }

    public void onReceivedMessage(String msg) {
        this.notifyListeners(new Message(msg));
    }

    public interface OnMessageListener {
        void onReceivedMessage(Message msg);
    }

    private void notifyListeners(Message msg) {
        if(requestIdListeners.containsKey(msg.requestId)) {
            requestIdListeners.get(msg.requestId).onReceivedMessage(msg);
            requestIdListeners.remove((msg.requestId));
        }
        if(typeListeners.containsKey(msg.msgType)) {
            for(OnMessageListener listener : typeListeners.get(msg.msgType)) {
                listener.onReceivedMessage(msg);
            }
        }
    }
}
