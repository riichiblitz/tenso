package com.yufimtsev.tenhouj;

import javax.net.SocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

public class Internal {
    private final String TENHOU_ADDR = "133.242.10.78";
    private final int TENHOU_PORT = 10080;
    private String tenhouName = "";

    private final Socket socket;
    private final DataOutputStream serverOutput;
    private final DataInputStream serverInput;

    public Internal() throws IOException {
        String localHost = System.getenv("OPENSHIFT_INTERNAL_IP");
        socket = localHost != null ? SocketFactory.getDefault().createSocket(InetAddress.getByName(TENHOU_ADDR), TENHOU_PORT,
                InetAddress.getByName(localHost), 8080)
                : SocketFactory.getDefault().createSocket(InetAddress.getByName(TENHOU_ADDR), TENHOU_PORT);
        //socket = new Socket(InetAddress.getByName(TENHOU_ADDR), TENHOU_PORT);
        socket.setSoTimeout(5000);
        serverOutput = new DataOutputStream(socket.getOutputStream());
        serverInput = new DataInputStream(socket.getInputStream());
    }

    public void helo(String name) {
        tenhouName = name;
        sendMessage(String.format("<HELO name=\"%s\" tid=\"f0\" sx=\"M\" />", name));
    }

    public void auth(String token) {
        sendMessage(String.format("<AUTH val=\"%s\"/>", token));
        sendMessage(getPrxTag(false));
    }

    public void lobby(String num) {
        chat(String.format("/lobby %s", num));
    }

    public void champLobby(String num) {
        sendMessage(String.format("<CS lobby=\"%s\"/>", num));
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Log.d(e);
        }
        sendMessage("<DATE />");
    }

    public void chat(String message) {
        try {
            sendMessage(String.format("<CHAT text=\"%s\" />", URLEncoder.encode(message, "UTF-8").replace("+", "%20")));
        } catch (UnsupportedEncodingException e) {
            Log.d(e);
        }
        sendMessage(getPrxTag(true));
    }

    public void who() {
        sendMessage("<CHAT text=\"%2Fw\" />");
    }

    public void disconnect() {
        sendMessage("<BYE />");
    }

    public void joinGame(String type) {
        sendMessage(String.format("<JOIN t=\"%s\" />", type));
    }

    public void destroy() {
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            serverOutput.close();
            serverInput.close();
        } catch (IOException e) {
            Log.d(e);
        }
    }

    public void sendMessage(String message) {
        Log.d(tenhouName + " Send: " + message);
        message += '\0';
        try {
            //serverOutput.writeByte(0);
            serverOutput.writeBytes(message);
            serverOutput.flush();
        } catch (IOException e) {
            Log.d(e);
        }
    }

    public void sendMessage(final String message, long delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage(message);
            }
        }, delay);
    }

    public static byte[] lastBytes = null;

    public String readMessage() {
        try {
            byte[] bytes = new byte[4096];
            StringBuilder builder = new StringBuilder();
            int read = serverInput.read(bytes);
            while (read > 0) {
                builder.append(new String(bytes, Charset.forName("utf8")).trim().toLowerCase());
                if (read < bytes.length - 1) {
                    break;
                }
                read = serverInput.read(bytes);
            }
            lastBytes = bytes;

            String message = builder.toString();
            if (message.trim().length() == 0) {
                return "<DISCONNECT />";
            } else {
                Log.d(tenhouName + " Received: " + message);
            }
            return message;
        } catch (SocketTimeoutException e) {
            // DO NOTHING
        } catch (IOException e) {
            Log.d(e);
        }
        return "";
    }

    public String[] readMultipleMessages() {
        return readMessage().split("\0");
    }

    // should execute the method each 15 seconds
    public void keepAlive() {
        sendMessage("<Z />");
        //sendMessage(getPrxTag(true));
    }

    public String getPrxTag(boolean lobby) {
        // I have no idea why we need to send it, but better to do it
        /*
         */
        if (tenhouName.equals("NoName")) {
            return "<PXR V=\"1\" />";
        } else if (lobby) {
            return "<PXR V=\"-1\" />";
        } else {
            return "<PXR V=\"9\" />";
        }
    }

    public String generateAuthToken(String auth) {
        int[] salt = new int[] {63006, 9570, 49216, 45888, 9822, 23121, 59830, 51114, 54831, 4189, 580, 5203, 42174, 59972,
                55457, 59009, 59347, 64456, 8673, 52710, 49975, 2006, 62677, 3463, 17754, 5357};
        String[] parts = auth.split("-");
        if (parts.length != 2) {
            throw new RuntimeException();
        }
        for (int i = 0; i < 2; i++) {
            if (parts[i].length() != 8) {
                throw new RuntimeException();
            }
        }
        int tableIndex = Integer.parseInt("2" + parts[0].substring(2, 8)) % (12 - Integer.parseInt(parts[0].substring(7, 8))) * 2;
        int a = salt[tableIndex] ^ Integer.parseInt(parts[1].substring(0, 4), 16);
        int b = salt[tableIndex + 1] ^ Integer.parseInt(parts[1].substring(4, 8), 16);
        String postfix = Integer.toHexString(a) + Integer.toHexString(b);

        return parts[0] + "-" + postfix;
    }

}
