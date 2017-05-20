package com.yufimtsev;

import com.yufimtsev.mityabot.logic.PlayerLogic;
import com.yufimtsev.tenhouj.Client;
import com.yufimtsev.tenhouj.Log;
import com.yufimtsev.tenhouj.UserState;
import com.yufimtsev.tenhouj.callback.IOnGameAction;
import com.yufimtsev.tenhouj.callback.IOnStateChangedCallback;

import java.util.*;

public class MainUtil {
    /* game type on tenhou.net is byte in format 76543210

	flags from lower:
	0 - 1 - online, 0 - bots
	1 - aka forbidden
	2 - kuitan forbidden
	3 - hanchan
	4 - 3man
	5 - dan flag
	6 - fast game
	7 - dan flag

	1 + (KUITAN_FORBIDDEN*2) + (AKA_FORBIDDEN*4) + (HANCHAN*8) + (3MAN*16) + (FAST*64)

	dan flags: 5,7: 0,0 - kyuu
	                1,0 - first dan
	                0,1 - upperdan
	                1,1 - fenix
    */

    public static void startGameDelayed(final String name, final long delay, final boolean autoreconnect) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.d(e);
                }
                //startGameWithName(name, "9", "C41190514", autoreconnect);
                startBot(getNewId(), name, "9", "C90701723", autoreconnect);
            }
        }.start();
    }

    public static HashMap<Integer, RunningClient> runningGames = new HashMap<Integer, RunningClient>();
    public static Random random = new Random(System.nanoTime());

    public static String getBotInfo(int id) {
        if (runningGames.containsKey(id)) {
            RunningClient runningClient = runningGames.get(id);
            return runningClient.client.getStatusJson();
        }
        return "{\"status\":\"error\",\"message\":\"no such active bot\"}";
    }

    public static String stopBotWithId(int id) {
        if (runningGames.containsKey(id)) {
            final RunningClient runningClient = runningGames.remove(id);
            runningClient.client.setCallback(new IOnStateChangedCallback() {
                @Override
                public void onStateChanged(UserState state) {
                    switch (state) {
                        case DISCONNECTED:
                            runningClient.thread.interrupt();
                            break;
                    }
                }
            });
            runningClient.client.disconnect(null);
            return "{\"status\":\"ok\",\"message\":\"bot has been removed\"}";
        }
        return "{\"status\":\"error\",\"message\":\"no such active bot\"}";
    }

    public static int getNewId() {
        int newId;
        do {
            newId = Math.abs(random.nextInt());
        } while (runningGames.containsKey(newId));
        return newId;
    }

    public static String startBot(final int currentId, final String name, final String gameType, final String lobby, final boolean autoReconnect) {
        if (runningGames.containsKey(currentId)) {
            return "{\"status\":\"error\",\"message\":\"impossible error, try again\"}";
        }

        final Client client = new Client(createMityaBot());
        final Thread thread = Thread.currentThread();

        IOnStateChangedCallback callback = new IOnStateChangedCallback() {

            private boolean inLobby = false;
            private boolean inPlay = false;

            @Override
            public void onStateChanged(UserState state) {
                switch (state) {
                    case CONNECTED:
                        client.authenticate(name, null);
                        break;
                    case IDLE:
                        if (inPlay) {
                            client.disconnect(null);
                        } else if (inLobby || lobby == null || lobby.equals("0")) {
                            if (lobby.equals("0")) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Log.d(e);
                                }
                            }
                            if (!lobby.startsWith("C")) {
                                client.joinGame(lobby + "," + gameType, null);
                            }
                            inPlay = true;
                        } else {
                            if (lobby.startsWith("C")) {
                                client.changeChampLobby(lobby, null);
                            } else {
                                client.changeLobby(lobby, null);
                            }
                            inLobby = true;
                        }
                        break;
                    case PLAYING:
                        inPlay = true;
                        break;
                    case DISCONNECTED:
                        inPlay = false;
                        inLobby = false;
                        if (autoReconnect) {
                            client.connect(null);
                        } else {
                            stopBotWithId(currentId);
                        }
                        break;
                }
            }
        };
        client.setCallback(callback);
        client.connect(null);
        killOnTimeout(currentId, autoReconnect);
        runningGames.put(currentId, new RunningClient(client, thread));
        return "{\"status\":\"ok\",\"id\":" + currentId + "}";
    }

    private static void killOnTimeout(final int id, final boolean reconnect) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (runningGames.containsKey(id)) {
                    RunningClient runningClient = runningGames.get(id);
                    if (runningClient.client.getState() != UserState.PLAYING) {
                        if (reconnect) {
                            Log.d("Reconnecting " + id + " on timeout");
                            runningClient.client.disconnect(null);
                        } else {
                            Log.d("Disconnecting " + id + " on timeout");
                            stopBotWithId(id);
                        }
                    }
                }
            }
        }, 60*1000); // one minute
    }

    public static IOnGameAction createMityaBot() {
        return new PlayerLogic();
    }

    /*public static IOnGameAction createInterface() {
        return new IOnGameAction() {

            private IPerformGameAction callback;
            private OldSimpleAi ai;
            private ArrayList<Integer> tenhouHand;

            @Override
            public void linkCallback(IPerformGameAction callback) {
                this.callback = callback;
            }

            @Override
            public void onRoundStart(ArrayList<Integer> hand) {
                ai = new OldSimpleAi(null, hand, 4);
                tenhouHand = hand;
            }

            @Override
            public void onDoraOpen(int doraIndicator) {

            }

            @Override
            public void onDraw(int tile, int actions) {
                tenhouHand.add(tile);
                int discard = ai.tsumo(tile / 4 + 1);
                if (discard == 0) {
                    callback.call(7, 0);
                } else {
                    discard = Decoder.getFirstEncoded(tenhouHand, discard);
                    callback.discard(discard);
                }
            }

            @Override
            public void onDiscard(int who, int tile, int actions) {

            }

            @Override
            public void onCall(int who, int m) {

            }

            @Override
            public void onRiichi(int who) {

            }

            @Override
            public void onRoundEnd(boolean gameEnd, int type, int data) {

            }
        };
    }*/
}
