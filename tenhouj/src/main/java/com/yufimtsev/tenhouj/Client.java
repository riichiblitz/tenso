package com.yufimtsev.tenhouj;

import javax.annotation.Nullable;
import com.yufimtsev.tenhouj.callback.IOnChatMessageReceived;
import com.yufimtsev.tenhouj.callback.IOnGameAction;
import com.yufimtsev.tenhouj.callback.IOnStateChangedCallback;
import com.yufimtsev.tenhouj.callback.IPerformGameAction;

import java.io.IOException;
import java.util.ArrayList;

public class Client implements IPerformGameAction {

    private UserState state;

    private Internal internalApi;
    private DataReceiver receiver;
    private KeepAliver keepAliver;

    private IOnGameAction actionCallback;

    private String userName = "";
    private String logName = "";

    @Nullable IOnStateChangedCallback pendingCallback;
    IOnStateChangedCallback autoCallback = null;
    IOnStateChangedCallback defaultCallback = new IOnStateChangedCallback() {
        public void onStateChanged(UserState state) {
            //System.out.println("STATE CHANGED: " + state.name());
            if (autoCallback != null) {
                autoCallback.onStateChanged(state);
            }
        }
    };

    IOnChatMessageReceived chatCallback = new IOnChatMessageReceived() {
        public void onChatMessageReceived(String message) {
            System.out.println("Chat message: " + message);
        }
    };

    private String gameType;

    public Client(IOnGameAction callback) {
        state = UserState.DISCONNECTED;
        actionCallback = callback;
        actionCallback.linkCallback(this);
    }

    public void connect() {
        connect(null);
    }

    public void setActionCallback(IOnGameAction callback) {
        actionCallback = callback;
    }

    public void setCallback(IOnStateChangedCallback callback) {
        autoCallback = callback;
    }

    public void connect(@Nullable IOnStateChangedCallback callback) {
        if (state != UserState.DISCONNECTED) {
            //break;//throw new IllegalStateException();
        }
        setState(UserState.CONNECTING, callback);
        try {
            internalApi = new Internal();
            receiver = new DataReceiver(internalApi);
            receiver.start();
            setState(UserState.CONNECTED, callback);
        } catch (IOException e) {
            e.printStackTrace();
            setState(UserState.DISCONNECTED, callback);
        }
    }

    public void disconnect(@Nullable IOnStateChangedCallback callback) {
        if (state == UserState.DISCONNECTED || state == UserState.DISCONNECTING) {
            //break;//throw new IllegalStateException();
        }
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        setState(UserState.DISCONNECTING, callback);
        keepAliver.disconnect();
        internalApi.disconnect();
        receiver.disconnect();
        internalApi.destroy();
        setState(UserState.DISCONNECTED, callback);
    }

    public void authenticate(String name, @Nullable IOnStateChangedCallback callback) {
        if (state != UserState.CONNECTED) {
            //break;//throw new IllegalStateException();
        }
        userName = name;
        setState(UserState.AUTHETICATING, callback);
        pendingCallback = callback;
        internalApi.helo(name);
    }

    public void changeLobby(String lobby, @Nullable IOnStateChangedCallback callback) {
        if (state != UserState.IDLE) {
            //break;//throw new IllegalStateException();
        }
        setState(UserState.CHANGING_LOBBY, callback);
        keepAliver.disconnect();
        pendingCallback = callback;
        internalApi.lobby(lobby);
    }

    public void changeChampLobby(String lobby, IOnStateChangedCallback callback) {
        if (state != UserState.IDLE) {
            //break;//throw new IllegalStateException();
        }
        setState(UserState.CHANGING_LOBBY, callback);
        keepAliver.disconnect();
        pendingCallback = callback;
        internalApi.champLobby(lobby);
    }

    public void joinGame(String type, @Nullable IOnStateChangedCallback callback) {
        if (state != UserState.IDLE && state != UserState.JOINING) {
            //break;//throw new IllegalStateException();
        }
        setState(UserState.JOINING, callback);
        pendingCallback = callback;
        gameType = type;
        internalApi.joinGame(gameType);
    }



    public void sendMessage(String message) {
        internalApi.chat(message);
    }

    public void getPlayes() {
        internalApi.who();
    }

    private void setState(UserState state, @Nullable IOnStateChangedCallback callback) {
        System.out.println(state.name() + " : " + userName);
        this.state = state;
        sendState(callback);
        defaultCallback.onStateChanged(state);
    }

    private void sendState(@Nullable IOnStateChangedCallback callback) {
        if (callback != null) {
            callback.onStateChanged(state);
        }
    }

    private void parseMessage(String message) {
        //System.out.println("Parsing message: " + message);
        if (message.startsWith("<HELO")) {
            switch (state) {
                case AUTHETICATING:
                    internalApi.auth(internalApi.generateAuthToken(Decoder.parseAuth(message)));
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<LN")) {
            switch (state) {
                case AUTHETICATING:
                    keepAliver = KeepAliver.startNew(internalApi);
                    IOnStateChangedCallback currentPendingCallback = pendingCallback;
                    pendingCallback = null;
                    setState(UserState.IDLE, currentPendingCallback);
                    break;
                case CHANGING_LOBBY:
                case IDLE:
                    break;
                case JOINING:
                case PLAYING:
                    internalApi.sendMessage(internalApi.getPrxTag(false));
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<CHAT")) {
            chatCallback.onChatMessageReceived(Decoder.parseChat(message));
            if (message.startsWith("<CHAT LOBBY")) {
                switch (state) {
                    case CHANGING_LOBBY:
                        keepAliver = KeepAliver.startNew(internalApi);
                        setState(UserState.IDLE, pendingCallback);
                        //pendingCallback = null;
                        break;
                    default:
                        break;//throw new IllegalStateException();
                }
            }
        } else if (message.startsWith("<REJOIN")) {
            switch (state) {
                case JOINING:
                    joinGame(gameType, pendingCallback);
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<GO")) {
            switch (state) {
                case AUTHETICATING:
                    internalApi.sendMessage("<GOK />");
                    internalApi.sendMessage("<NEXTREADY />");
                    setState(UserState.JOINING, pendingCallback);
                case JOINING:
                case IDLE:
                    setState(UserState.PLAYING, pendingCallback);
                    internalApi.sendMessage("<GOK />");
                    internalApi.sendMessage("<NEXTREADY />");
                    break;
                default:
                    break;//break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<UN")) {
            switch (state) {
                case JOINING:
                    // todo: decode names and ranks
                case PLAYING:
                    // todo: decode rejoined or leaved player
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<TAIKYOKU")) {
            switch (state) {
                case JOINING:
                case PLAYING:
                    setState(UserState.PLAYING, pendingCallback);
                    logName = "http://tenhou.net/0/?log=" + Decoder.parseLogLink(message).toLowerCase();
                    System.out.println("Game started, log link: " + Decoder.parseLogLink(message).toLowerCase());
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<INIT")) {
            // ex: <init seed="2,4,0,1,4,52" ten="230,228,0,592" oya="1" hai="113,82,2,51,99,119,33,81,58,128,109,50,41"/>
            // seed: round number, renchan, riichi stack, dice - 1, dice - 1, dora indicator

            // REINIT: <reinit seed="0,0,0,4,4,133" ten="350,350,350,0" oya="0" hai="32,37,38,40,42,44,45,87,90,100,102,110,111" kawa0="47,130,96,84,33,59" kawa1="39,103,134,73,94,43" kawa2="78,95,55,52,81,56"/> <t119/>
            setState(UserState.PLAYING, pendingCallback);
            internalApi.keepAlive();
            actionCallback.onRoundStart(Decoder.getTilesByTag("init", "hai", message));
            actionCallback.onDoraOpen(Decoder.parseStartDora(message));
        } else if (message.startsWith("<REINIT")) {
            //<reinit seed="4,1,0,5,2,92" ten="182,395,62,361" oya="1" hai="11,16,25,28,37,66,85,86,89,96,100,121,133" m1="17815" m2="43487,40555" kawa0="115,130,124,64,112,123,84" kawa1="129,35,113,132,81,23,118" kawa2="7,45,27,6,131,65,59,9,62" kawa3="32,4,128,80,15,90,127,68"/> <w/>

            actionCallback.onRoundStart(Decoder.getTilesByTag("reinit", "hai", message));
            for (int i = 0; i < 4; i++) {
                ArrayList<Integer> kawai = Decoder.getTilesByTag("reinit", "kawa"+i, message);
                for (Integer tile : kawai) {
                    actionCallback.onDiscard(i, tile, 0);
                }
                ArrayList<Integer> melds = Decoder.getTilesByTag("reinit", "m" + i, message);
                for (Integer meld : melds) {
                    actionCallback.onCall(i, meld, 0);
                }
            }
        } else if (message.startsWith("<DORA")) {
            // new dora indicator after kan
        } else if (message.startsWith("<REACH")) {
            // check STEP attr: 1 if just called, 2 tenbo is on the table

        } else if (message.startsWith("<T")) {
            // tsumo
            // if DORA NORTH: t=128
            // if DORA NORTH AND RIICHI t=160
            // 7 6 5 4 3 2 1 0
            // 4 == 16 - tsumo flag (sometimes it is lost, only Riichi is open)
            // 5 == 32 - riichi flag
            // 7 == 128 - dora north flag
            int action = Decoder.parseActions(message);
            int tile = Decoder.parseTile(message);
            actionCallback.onDraw(tile, action);
        } else if (message.startsWith("<FURITEN")) {
            // <furiten show="1" />
        } else if (message.startsWith("<E") || message.startsWith("<F") || message.startsWith("<G")) {
            // someone discard
            int action = Decoder.parseActions(message);
            actionCallback.onDiscard(message.startsWith("<E") ? 0 :
                    message.startsWith("<F") ? 1 : 2, Decoder.parseTile(message), action);

        } else if (message.startsWith("<N WHO")) {
            // ex: <N who="0" m="31264" />
            // someone claims call
            // m & 0x4 = chii
            // m & 0x8 = pon
            // m & 0x10 = chakan
            // m & 0x20 = dora north (tile = m >> 8)
            // else = kan
            int action = Decoder.parseActions(message);
            // TODO: think how to send it to AI
        } else if (message.contains("AGARI") || message.contains("RYUUKYOKU")) {
            // ex: <ryuukyoku ba="4,0" sc="230,0,228,0,0,0,592,0" owari="230,-17.0,228,-37.0,0,0.0,592,54.0" />
            actionCallback.onRoundEnd(message.contains("OWARI"),  message.contains("RYUUKYOKU")? 0 : 1, 0);
            if (message.contains("OWARI")) {
                disconnect(pendingCallback);
            }
        } else if (message.startsWith("<PROF")) {
            // ex: <prof lobby="1489" type="81" add="-17.0,0,1,0,0,0,5,0,1,0,0"/>
            // states changed for lobby and game type
            switch (state) {
                case PLAYING:
                case IDLE:
                    //pendingCallback = null;
                    break;
                default:
                    break;//throw new IllegalStateException();
            }
        } else if (message.startsWith("<DISCONNECT")) {
            disconnect(pendingCallback);
        }
    }

    @Override
    public void discard(int tile) {
        internalApi.sendMessage(String.format("<D p=\"%d\" />", tile));
    }

    @Override
    public void call(int type, int data) {
        switch (type) {
            case 0: internalApi.sendMessage("<N />"); break;
            case 1: internalApi.sendMessage("<N />"); break;
            case 2: internalApi.sendMessage("<N />"); break;
            case 3: internalApi.sendMessage("<N />"); break;
            case 4: internalApi.sendMessage("<N />"); break;
            case 5: internalApi.sendMessage("<N />"); break;
            case 6: internalApi.sendMessage("<N type=\"6\" />", 500); break; // ron
            case 7: internalApi.sendMessage("<N type=\"7\" />", 500); break; // tsumo
            case 8: internalApi.sendMessage("<N />"); break;
            case 9: internalApi.sendMessage("<N />"); break;
            case 10: internalApi.sendMessage("<N type=\"10\" />", 500); break; // nuki / dora north
        }
    }

    @Override
    public void riichi(int tile) {
        internalApi.sendMessage(String.format("<REACH hai=\"%d\" />", tile));
    }

    @Override
    public void ready() {
        internalApi.sendMessage("<NEXTREADY />", 2000);
    }

    public UserState getState() {
        return state;
    }

    public String getStatusJson() {
        return "{\"status\":\"" + state.name() + "\",\"log\":\"" + logName + "\"}";
    }

    private class DataReceiver extends Thread {

        private Internal internal;
        private boolean receiving;

        public DataReceiver(Internal internal) {
            this.internal = internal;
            receiving = true;
        }

        public void disconnect() {
            receiving = false;
            interrupt();
        }

        @Override
        public void run() {
            while (receiving) {
                String[] messages = internal.readMultipleMessages();
                for (String message: messages) {
                    if (message != null && message.length() > 0) {
                        parseMessage(message.trim().toUpperCase());
                    }
                }
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private static class KeepAliver extends Thread {
        private Internal internal;
        private boolean working;

        private static KeepAliver instance;

        public static KeepAliver startNew(Internal internal) {
            if (instance != null) {
                instance.disconnect();
            }
            instance = new KeepAliver(internal);
            instance.start();
            return instance;
        }

        public KeepAliver(Internal internal) {
            this.internal = internal;
            working = true;
        }

        public void disconnect() {
            working = false;
            interrupt();
        }

        @Override
        public void run() {
            while (working) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }

                internal.keepAlive();

                try {
                    sleep(14000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }


}
