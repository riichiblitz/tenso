package com.yufimtsev.tenhouj;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Decoder {

    public static String parseAuth(String authMessage) {
        Document document = Jsoup.parse(authMessage);
        Elements helo = document.getElementsByTag("helo");
        if (helo.size() > 0) {
            return helo.get(0).attr("auth");
        }
        return null;
    }

    public static String parseChat(String chatMessage) {
        Document document = Jsoup.parse(chatMessage);
        Elements messages = document.getElementsByTag("chat");
        if (messages.size() > 0) {
            Element message = messages.get(0);
            try {
                String uname = URLDecoder.decode(message.attr("uname"), "UTF-8");
                String lobby = URLDecoder.decode(message.attr("lobby"), "UTF-8");
                String text = URLDecoder.decode(message.attr("text"), "UTF-8");
                if (lobby.length() > 0) {
                    return "LOBBY " + lobby;
                } else if (uname.length() > 0 ) {
                    return uname + ": " + text;
                } else {
                    return text;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    public static String parseLogLink(String taikyokuMessage) {
        Document document = Jsoup.parse(taikyokuMessage);
        Elements elements = document.getElementsByTag("taikyoku");
        if (elements.size() > 0) {
            Element taikyoku = elements.get(0);
            try {
                return URLDecoder.decode(taikyoku.attr("log"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    public static int parseTile(String tileMessage) {
        Document document = Jsoup.parse(tileMessage);
        Element element = document.getElementsByTag("body").get(0).child(0);
        return Integer.parseInt(element.tagName().replaceAll("[tefg]", ""));
    }

    public static Integer parseStartDora(String initMessage) {
        Document document = Jsoup.parse(initMessage);
        String[] tiles = document.getElementsByTag("init").first().attr("seed").split(",");
        return Integer.parseInt(tiles[tiles.length - 1]);
    }

    public static ArrayList<Integer> getTilesByTag(String upperTag, String tag, String initMessage) {
        Document document = Jsoup.parse(initMessage);
        String[] tiles = document.getElementsByTag(upperTag).first().attr(tag).split(",");
        ArrayList<Integer> result = new ArrayList<Integer>(13);
        for (String tile : tiles) {
            if (tile.length() > 0) {
                result.add(Integer.parseInt(tile));
            }
        }
        Collections.sort(result);
        return result;
    }

    public static int parseActions(String actionMessage) {
        Document document = Jsoup.parse(actionMessage);
        try {
            String actions = document.getElementsByAttribute("t").first().attr("t");
            return Integer.parseInt(actions);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public static ArrayList<Integer> encodeHand(ArrayList<Integer> hand) {
        ArrayList<Integer> result = new ArrayList<Integer>(13);
        for (Integer tile : hand) {
            result.add((tile/* - 1*/) / 4 + 1);
        }
        Collections.sort(result);
        return result;
    }

    public static int getFirstEncoded(ArrayList<Integer> hand, int encodedTile) {
        for (Integer tile : hand) {
            if ((tile/* - 1*/) / 4 + 1 == encodedTile) {
                return tile;
            }
        }
        return 0;
    }




}
