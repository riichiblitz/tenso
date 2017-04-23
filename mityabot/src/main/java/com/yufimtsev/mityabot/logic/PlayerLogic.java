package com.yufimtsev.mityabot.logic;

import com.yufimtsev.mityabot.api.EfficiencyService;
import com.yufimtsev.mityabot.model.ApiResponse;
import com.yufimtsev.tenhouj.callback.IOnGameAction;
import com.yufimtsev.tenhouj.callback.IPerformGameAction;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PlayerLogic implements IOnGameAction {

    private int NORTH_DORA_FLAG = 0x00000080;
    private int RIICHI_FLAG = 0x00000020;
    private int TSUMO_FLAG = 0x00000010;
    private int RON_FLAG = 0x00000008;

    private IPerformGameAction callback;
    private ArrayList<Integer> hand;
    private ArrayList<Integer> used;
    private boolean inRiichi = false;

    public void linkCallback(IPerformGameAction callback) {
        this.callback = callback;
    }

    public void onRoundStart(ArrayList<Integer> hand) {
        this.hand = (ArrayList<Integer>) hand.clone();
        this.used = new ArrayList<Integer>();
        inRiichi = false;
    }

    public void onDoraOpen(int doraIndicator) {
        used.add(doraIndicator / 4 + 1);
    }

    public void onDraw(int tile, int actions) {
        if ((actions & TSUMO_FLAG) > 0) {
            callback.call(7, 0);
            return;
        }
        int botTile = tile / 4 + 1;
        if (inRiichi) {
            used.add(botTile);
            callback.discard(tile);
            return;
        }
        hand.add(tile);
        Collections.sort(hand);
        Collections.sort(used);
        try {
            Response<ApiResponse> response = EfficiencyService.getInstance().getEfficiency(writeHand(), writeUsed()).execute();
            if (response.isSuccessful()) {
                int discard = response.body().results.get(0).tile + 1; // since we work not in 0..35, but in 1..36
                int tileInHand = getLastTile(discard);
                hand.remove(new Integer(tileInHand));
                used.add(discard);
                if ((actions & RIICHI_FLAG) > 0) {
                    callback.riichi(tileInHand);
                    inRiichi = true;
                    Thread.sleep(1000);
                }
                callback.discard(tileInHand);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hand.remove(new Integer(tile));
        used.add(botTile);
        callback.discard(tile);
    }

    public void onDiscard(int who, int tile, int actions) {
        if ((actions & RON_FLAG) > 0) {
            callback.call(6, 0);
            return;
        }
        if (actions > 0) {
            callback.call(0, 0);
        }
        used.add(tile / 4 + 1);
    }

    public void onCall(int who, int m, int actions) {
        if ((actions & RON_FLAG) > 0) {
            callback.call(6, 0);
            return;
        }
        if (actions > 0) {
            callback.call(0, 0);
        }
        if ((m & 0x04) > 0) { // chii
            int baseTile = (m >> 10) / 3;
            baseTile = (baseTile / 7) * 9 + baseTile % 7;
            used.add(++baseTile);
            used.add(++baseTile);
            used.add(++baseTile);
        } else if ((m & 0x18) > 0) { // pon
            int baseTile = (m >> 9) / 3;
            //baseTile = (baseTile / 7) * 9 + baseTile % 7;
            used.add(baseTile + 1);
            used.add(baseTile + 1);
            used.add(baseTile + 1);
        } else if ((m & 0x20) > 0) { // nuki
            used.add(31);
        } else { // kan
            int baseTile = (m >> 8) / 4;
            //baseTile = (baseTile / 7) * 9 + baseTile % 7;
            used.add(baseTile + 1);
            used.add(baseTile + 1);
            used.add(baseTile + 1);
            used.add(baseTile + 1);
        }
    }

    public void onRiichi(int who, int step) {

    }

    public void onRoundEnd(boolean gameEnd, int type, int data) {
        if (!gameEnd) {
            callback.ready();
        }
    }

    public int getLastTile(int botTile) {
        for (int i = hand.size() - 1; i >= 0; i--) {
            Integer tile = hand.get(i);
            if (botTile == tile / 4 + 1) {
                return tile;
            }
        }
        return -1;
    }

    private static final char[] TYPES = new char[]{'m', 'p', 's', 'z'};

    public String writeHand() {
        if (hand.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        int currentType = (hand.get(0) / 4) / 9;
        char currentChar = TYPES[currentType];
        for (Integer tile : hand) {
            currentType = (tile / 4) / 9;
            if (TYPES[currentType] != currentChar) {
                builder.append(currentChar);
                currentChar = TYPES[currentType];
            }
            builder.append(tile / 4 + 1 - currentType * 9);
        }
        builder.append(currentChar);
        return builder.toString();
    }

    public String writeUsed() {
        if (used.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        int currentType = (used.get(0) - 1) / 9;
        char currentChar = TYPES[currentType];
        for (Integer tile : used) {
            currentType = (tile - 1) / 9;
            if (TYPES[currentType] != currentChar) {
                builder.append(currentChar);
                currentChar = TYPES[currentType];
            }
            builder.append(tile - currentType * 9);
        }
        builder.append(currentChar);
        return builder.toString();
    }
}
