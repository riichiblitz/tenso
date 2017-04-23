package com.yufimtsev.tenhouj.callback;

public interface IPerformGameAction {

    void discard(int tile);

    void call(int type, int data);

    void riichi(int tile);

    void ready();

}
