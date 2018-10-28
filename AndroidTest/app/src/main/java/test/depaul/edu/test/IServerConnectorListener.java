package test.depaul.edu.test;

public interface IServerConnectorListener {
    public void onServerConnected();
    public void onServerDisconnected();
    public void onReceivedMessage(String msg);
}
