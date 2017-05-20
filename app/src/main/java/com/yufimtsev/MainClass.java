package com.yufimtsev;

import com.google.gson.Gson;
import com.yufimtsev.mahjongai.*;
import com.yufimtsev.mahjongai.Util;
import com.yufimtsev.tenhouj.Decoder;
import com.yufimtsev.tenhouj.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;

public class MainClass {

    private static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        port(getAssignedPort());
        staticFileLocation("/public");

        get("/hello", (req, res) -> "{\"status\":\"ok\"}");

        get("/startA", (req, res) -> {
            final String name = "NoName";
            final String type = "9";
            final String lobby = "7994";
            final int currentId = com.yufimtsev.MainUtil.getNewId();

            new Thread(() -> MainUtil.startBot(currentId, name, type, lobby, false)).start();
            return "ok";
        });

        get("/startBot", (req, res) -> {
            final String name = req.queryParams("name") != null ? req.queryParams("name") : "NoName";
            final String type = req.queryParams("type") != null ? req.queryParams("type") : "9";
            final String lobby = req.queryParams("lobby") != null ? req.queryParams("lobby") : "7994";
            final boolean autoreconnect = req.queryParams("autoreconnect") != null ? Boolean.parseBoolean(req.queryParams("autoreconnect")) : false;
            final int currentId = com.yufimtsev.MainUtil.getNewId();

            new Thread(new Runnable() {

                @Override
                public void run() {
                    com.yufimtsev.MainUtil.startBot(currentId, name, type, lobby, autoreconnect);
                }

            }).start();
            return "{\"status\":\"ok\",\"id\":" + currentId + "}";
        });

        get("/stopBot", (req, res) -> {
            if (req.queryParams("id") == null) {
                return "{\"status\":\"error\",\"message\":\"no id param\"}";
            }
            final int id = Integer.parseInt(req.queryParams("id"));
            return com.yufimtsev.MainUtil.stopBotWithId(id);
        });

        get("/info", (req, res) -> {
            if (req.queryParams("id") == null) {
                return "{\"status\":\"error\",\"message\":\"no id param\"}";
            }
            final int id = Integer.parseInt(req.queryParams("id"));
            return com.yufimtsev.MainUtil.getBotInfo(id);
        });

        get("/status", (req, res) -> {
            StatusResponse response = new StatusResponse();
            response.log = Log.collect();
            response.infos = new ArrayList<>();
            for (Integer key : MainUtil.runningGames.keySet()) {
                InfoResponse info = new InfoResponse();
                info.id = key;
                info.log = MainUtil.getBotInfo(key);
                response.infos.add(info);
            }
            return gson.toJson(response);
        });

        if (true) return;

        //runConsoleBot();
        //MainUtil.startGameDelayed("ID5291632A-DWgLGEZR", 0, true);
        for (int i = 0; i < 1; i++) {
            MainUtil.startGameDelayed("NoName", i*10000, true);
        }
//        MainUtil.startGameDelayed("NoName", 0, true);
//        MainUtil.startGameDelayed("NoName", 0, true);
//        MainUtil.startGameDelayed("NoName", 0, true);

        if (true) return;


        MainUtil.startGameDelayed("ID5291632A-DWgLGEZR", 0, true);
        /*MainUtil.startGameDelayed("ID54EE1E02-J79Jecg6", 0, true);
        for (int i = 0; i < 4; i++) {
            MainUtil.startGameDelayed("NoName", (i+1)*3000, true);
        }*/
        /*com.yufimtsev.MainUtil.startGameDelayed("NoName", 3000, true);
        com.yufimtsev.MainUtil.startGameDelayed("NoName", 6000, true);
        com.yufimtsev.MainUtil.startGameDelayed("NoName", 9000, true);
        com.yufimtsev.MainUtil.startGameDelayed("NoName", 12000, true);
        com.yufimtsev.MainUtil.startGameDelayed("NoName", 15000, true);*/

        if (true) return;
        //TenhouClient client = new TenhouClient();
/*        String hand1 = "123466789m13s12z";
        String hand2 = "123456789m123s11z";
        long[] longHandLeft = HandCoDec.decodeOptimized(Util.readHand(hand1));
        long[] longHandRight = HandCoDec.decodeOptimized(Util.readHand(hand2));
        long[] differenceToGet = new long[4];
        long[] differenceToCut = new long[4];
        Util.getTranslatedDifference(longHandLeft, longHandRight, differenceToGet, differenceToCut);
        System.out.println(Util.writeOptimizedHand(longHandLeft));
        System.out.println(Util.writeOptimizedHand(longHandRight));
        System.out.println(Util.writeOptimizedHand(differenceToGet));
        System.out.println(Util.writeOptimizedHand(differenceToCut));
        System.out.println(Util.getDistance(longHandLeft, longHandRight));

        if (true) return;*/

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        int a = 0;
        while (a < 10) {
            println("Enter hand > ");
            String input = br.readLine();
            ArrayList<Integer> hand = Util.readHand(input);
            NewSimpleAi ai = new NewSimpleAi(hand, 14);
            println("Enter tile > ");
            input = br.readLine();
            hand = Util.readHand(input);
            ai.tsumo(hand.get(0));
        }

        long[] longHand1 = readHand(br);
        System.out.println(Util.writeOptimizedHand(longHand1));
        long[] longHand2 = readHand(br);
        System.out.println(Util.writeOptimizedHand(longHand2));

        System.out.println(Util.getDistance(longHand1, longHand2));

        if (true) return;

        String message = "<init seed=\"0,0,0,4,0,34\" ten=\"350,350,350,0\" oya=\"0\" hai=\"108,126,46,109,121,76,72,110,39,125,77,129,65\"/>";
        ArrayList<Integer> tiles = Decoder.getTilesByTag("init", "hai", message);
        ArrayList<Integer> aiHand = Decoder.encodeHand(tiles);
        int discard = Decoder.getFirstEncoded(tiles, 17);

        //final Client client = new Client(false);
        //Util.startGameDelayed("ID54EE1E02-J79Jecg6", 0, true);
        /*for (int i = 0; i < 9; i++) {
            startGameDelayed("NoName", i*3000, i%3 == 0);
        }*/
        /*startGameDelayed("NoName", 3000, true);
        startGameDelayed("NoName", 6000, true);
        startGameDelayed("NoName", 9000, true);
        startGameDelayed("NoName", 30000, true);
        startGameDelayed("NoName", 33000, true);
        startGameDelayed("NoName", 36000, true);
        startGameDelayed("NoName", 39000, true);*/

        //client.auth();
    }

    private static long[] readHand(BufferedReader br) throws IOException {
        println("Enter hand > ");
        String input = br.readLine();
        ArrayList<Integer> hand = Util.readHand(input);
        int[] intHand = new int[hand.size()];
        for (int i = 0; i < intHand.length; i++) {
            intHand[i] = hand.get(i);
        }
        return HandCoDec.decodeOptimized(intHand);
    }


    public static void runConsoleBot() throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        println("Enter hand > ");
        String input = br.readLine();

        NewSimpleAi ai = new NewSimpleAi(Util.readHand(input), 15);

        while (true) {
            println("Enter tsumo > ");
            input = br.readLine();

            if (ai.tsumo(Util.readHand(input).get(0)) == -1) {
                break;
            }

            if ("exit".equals(input)) {
                break;
            }

            //SimpleAI.proceed(hands, input);
        }


    }

    private static long now = System.nanoTime();

    public static void println(String message) {
        long currentTime = System.nanoTime();
        System.out.print((currentTime - now) / 1000000000f);
        System.out.print(": ");
        System.out.println(message);
        now = currentTime;
    }



    private static int getAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
