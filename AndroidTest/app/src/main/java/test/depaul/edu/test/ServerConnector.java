package test.depaul.edu.test;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerConnector {
    private static char MSG_SEPARATOR = ';';

    private boolean connected = false;
    private Thread socketThread = null;
    private Socket socket = null;
    private Handler mainThreadHandler;
    private IServerConnectorListener listener;
    private OutputStreamWriter writer;

    public ServerConnector(IServerConnectorListener listener) {
        this.listener = listener;
        mainThreadHandler = new Handler();
    }

    public void connect() {
        if(socketThread != null) return;//error

        socketThread = new Thread(new SocketThread());
        socketThread.start();
    }

    public void disconnect() {
        if(socketThread == null) return;//error

        socketThread.interrupt();
        Socket sock = socket;
        writer = null;
        socketThread = null;
        socket = null;
        try {
            if(sock != null) sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        if(!connected) return;//should wait after connection is built
        new SendMessageTask().execute(msg);
    }

    class SocketThread implements Runnable {
        public void run() {
//            Log.v("SocketThread","Thread start");
            try {
                socket = new Socket(GameConfiguration.SERVER_ADDRESS, GameConfiguration.SERVER_PORT);
                this.notifyListenerConnected();
                writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                String strBuffer = "";
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()
                        && (bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.reset();
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    strBuffer = strBuffer+byteArrayOutputStream.toString();
                    int separatorIdx = strBuffer.indexOf(MSG_SEPARATOR);
                    int lastIdx = -1;
                    while(separatorIdx != -1) {
                        String str = strBuffer.substring(lastIdx+1, separatorIdx);
                        this.notifyListenerReceivedMessage(str);
                        lastIdx = separatorIdx;
                        separatorIdx = strBuffer.indexOf(MSG_SEPARATOR, lastIdx+1);
                    }
                    if(lastIdx != -1) strBuffer = strBuffer.substring(lastIdx+1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    this.notifyListenerDisconnected();
                }
            }
//            Log.v("SocketThread","Thread end");
        }
        void notifyListenerConnected() {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    connected = true;
                    if(listener != null) listener.onServerConnected();
                }
            });
        }
        void notifyListenerDisconnected() {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    connected = false;
                    if(listener != null) listener.onServerDisconnected();
                }
            });
        }
        void notifyListenerReceivedMessage(final String msg) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.v("ServerConnector", "received :"+msg);
                    if(listener != null) listener.onReceivedMessage(msg);
                }
            });
        }
    }

    class SendMessageTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strs) {
            try {
                for (int i = 0; i < strs.length; i++) {
                    writer.write(strs[i]);
                }
                writer.write(MSG_SEPARATOR);
                writer.flush();
                Log.v("ServerConnector", "sent :"+strs[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}