package com.yufimtsev.mahjongai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Util {

    private static final char[] TYPES = new char[] {'m', 'p', 's', 'z'};

    public static String writeOptimizedHand(long[] hand) {
        String builder = "man:" + Long.toBinaryString(hand[0]) + "\n" +
                "pin:" + Long.toBinaryString(hand[1]) + "\n" +
                "sou:" + Long.toBinaryString(hand[2]) + "\n" +
                "dra:" + Long.toBinaryString(hand[3]) + "\n";
        return builder;
    }

    public static String writeHand(ArrayList<Integer> hand) {
        StringBuilder builder = new StringBuilder();
        int currentType = (hand.get(0) - 1) / 9;
        char currentChar = TYPES[currentType];
        for (Integer tile : hand) {
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

    public static String writeHand(int[] hand) {
        StringBuilder builder = new StringBuilder();
        int currentType = (hand[0] - 1) / 9;
        char currentChar = TYPES[currentType];
        for (int i = 0; i < hand.length; i++) {
            currentType = (hand[i] - 1) / 9;
            if (TYPES[currentType] != currentChar) {
                builder.append(currentChar);
                currentChar = TYPES[currentType];
            }
            builder.append(hand[i] - currentType * 9);
        }
        builder.append(currentChar);
        return builder.toString();
    }

    public static ArrayList<Integer> readHand(String input) {
        ArrayList<Integer> integers = new ArrayList<Integer>(14);

        char lastChar = ' ';
        for (int i = input.length() - 1; i >= 0; i--) {
            char currentChar = input.charAt(i);
            if (currentChar == 'm' || currentChar == 'p' || currentChar == 's' || currentChar == 'z') {
                lastChar = currentChar;
            } else {
                int addition = lastChar == 'm' ? 0
                        : lastChar == 'p' ? 9
                        : lastChar == 's' ? 18
                        : lastChar == 'z' ? 27 : 0;
                integers.add(addition + currentChar - 48);
            }
        }
        Collections.sort(integers);
        return integers;
    }

    public static int getLength(long[] tileSet) {
        int length = 0;
        for (int i = 0; i < tileSet.length; i++) {
            length += Long.bitCount(tileSet[i]);
        }
        return length;
    }

    public static void getTranslatedDifference(long[] currentHand, long[] remoteHand, long[] differenceToGet, long[] differenceToCut) {
        /* ex: currentHand: 0001 0111 0000, remoteHand: 0011 0001 0011
           to get remote from current we need to take   0010 0000 0011
           after translation all 1s to the left we got  1000 0000 1100
         */
        Arrays.fill(differenceToGet, 0);
        Arrays.fill(differenceToCut, 0);
        for (int i = 0; i < differenceToGet.length; i++) {
            long binaryDifference = currentHand[i] ^ remoteHand[i];
            differenceToGet[i] =  binaryDifference & ~currentHand[i]; // tiles are wanted for remoteHand
            differenceToCut[i] = binaryDifference & ~remoteHand[i]; // tiles are not needed for remoteHand
            // check all the tiles, if it is presented - translate all 1s to the left side
            long mask = 0x0F;//0b1111; // mask of any tile
            int lastIndex = i == 4 ? 7 : 9; // 9 values in suits and 7 in winds/dragons
            for (int j = 0; j < lastIndex; j++) {
                long value = differenceToGet[i] & mask;
                if (value != 0) { // tile "j" in suit "i" wanted, translate it to the left side
                    int count = Long.bitCount(value); // get count of 1s
                    differenceToGet[i] ^= value; // remove tile value from difference
                    // create the translated value
                    value = 0;
                    for (int k = 0; k < count; k++) {
                        value <<= 1;
                        value++;
                    }
                    value <<= j*4 + (4 - count); // return it to original position of tile "j" with translation
                    differenceToGet[i] |= value;
                }
                mask <<= 4;
            }
            // we don't need to translate differenceToCut the same way
            // only thing that makes us do it for differenceToGet - fast bitwise check for used tiles
            // maybe it has bad performance this way
            // TODO: check this out
        }
    }

    public static int getDistance(long[] currentHand, long[] remoteHand) {
        int distance = 0;
        for (int i = 0; i < currentHand.length; i++) {
            distance += Long.bitCount(currentHand[i] ^ remoteHand[i]);
        }
        // we need to know if some distance is produced by difference of hands' length
        int leftSize = getLength(currentHand);
        int rightSize = getLength(remoteHand);
        // divide result by 2 since each different tile produces x2 differences
        return (distance - (rightSize - leftSize)) >> 1;
    }

}
