package com.yufimtsev.tenhouj.callback;

import java.util.ArrayList;

public interface IOnGameAction {

    void linkCallback(IPerformGameAction callback);

    void onRoundStart(ArrayList<Integer> hand);

    void onDoraOpen(int doraIndicator);

    void onDraw(int tile, int actions);

    void onDiscard(int who, int tile, int actions);

    void onCall(int who, int m, int actions);

    void onRiichi(int who, int step);

    void onRoundEnd(boolean gameEnd, int type, int data);

}
