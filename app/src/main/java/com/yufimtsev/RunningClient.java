package com.yufimtsev;

import com.yufimtsev.tenhouj.Client;

public class RunningClient {

    public Client client;
    public Thread thread;

    public RunningClient(Client client, Thread thread) {
        this.client = client;
        this.thread = thread;
    }
}
