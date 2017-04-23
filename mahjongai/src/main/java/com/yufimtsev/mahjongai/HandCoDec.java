package com.yufimtsev.mahjongai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by yufimtsev on 29.07.2016.
 */
public class HandCoDec {

    /* Coded format:
       First and second PON tooks 6 bits, third and forth - 5 bits.
       Each CHII tooks 5 bits.
       PAIR tooks 6 bits.
       Header is [1] if hand has 4 pons or [0XX] if hand has XX pons (0-3).
       1) if 4 pons: --- 111111 11111 11111 111111 111111 1
                          pair   pon   pon   pon    pon
       2) if 2 pons  - 111111 11111 11111 111111 111111 010
                        pair   chi   chi    pon    pon


     */

    private static final int MASK_1 = 0x00000001;
    private static final int MASK_2 = 0x00000003;
    private static final int MASK_5 = 0x0000001F;
    private static final int MASK_6 = 0x0000003F;

    private static Integer[] POSSIBLE_PON = new Integer[] {
         1,  2,  3,  4,  5,  6,  7,  8,  9,
        10, 11, 12, 13, 14, 15, 16, 17, 18,
        19, 20, 21, 22, 23, 24, 25, 26, 27,
        28, 29, 30, 31, 32, 33, 34
    };
    private static Integer[] POSSIBLE_CHII = new Integer[] {
             1,  2,  3,  4,  5,  6,  7,
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    public static void removeTileFromHand(long[] hand, int tile) {
        // from [1..34] to [0..135];
        tile = (tile - 1) * 4;
        int index = tile / 36;
        long removeMask = 1L << (tile - index * 36);
        long tileMask = 0x0FL/*0b1111L*/ << (tile - index * 36);
        long value = hand[index] & tileMask;
        if (value != 0) {
            hand[index] ^= value;
            value ^= removeMask;
            value >>= 1;
            hand[index] |= value;
        }
    }

    public static void addTileToHand(long[] hand, int tile) {
        // from [1..34] to [0..135];
        tile = (tile - 1) * 4;
        int index = tile / 36;
        long tileMask = 1L << (tile - index * 36);
        // check if the tile with such mask is already added to the hand
        int counter = 0;
        while ((hand[index] & tileMask) != 0) {
            counter++;
            if (counter == 4) {
                throw new RuntimeException("Impossible to add 5th tile of type " + tile);
            }
            tileMask <<= 1;
        }
        hand[index] |= tileMask;
    }

    public static ArrayList<Integer> encodeOptimized(long[] hand) {
        ArrayList<Integer> result = new ArrayList<Integer>(14);
        for (int i = 0; i < 34; i++) {
            int suit = i / 9;
            long tile = 0x0FL << (i % 9)*4;
            int tileCount = Long.bitCount(hand[suit] & tile);
            for (int j = 0; j < tileCount; j++) {
                result.add(i+1);
            }
        }
        Collections.sort(result);
        return result;
    }

    public static long[] decodeOptimized(ArrayList<Integer> hand) {
        long[] result = new long[4];
        for (Integer tile : hand) {
            addTileToHand(result, tile);
        }
        return result;
    }

    public static long[] decodeOptimized(int[] hand) {
        long[] result = new long[4];
        for (int i = 0; i < hand.length; i++) {
            addTileToHand(result, hand[i]);
        }
        return result;
    }

    public static long[] decodeOptimized(int coded) {
        return decodeOptimized(decode(coded));
    }

    public static int[] decode(int coded) {
        int[] result = new int[14];
        int ponCount = 0;
        int lowestBit = coded & MASK_1;
        coded >>= 1;
        if (lowestBit > 0) {
            ponCount = 4;
        } else {
            ponCount = coded & MASK_2;
            coded >>= 2;
        }

        ArrayList<Integer> possibleValues = new ArrayList<Integer>(Arrays.asList(POSSIBLE_PON));
        int cursor = 0;
        int settingValue = 0;

        // extract pons
        for (int i = 0; i < ponCount; i++) {
            if (i > 1) {
                settingValue = possibleValues.remove(coded & MASK_5);
                coded >>= 5;
            } else {
                settingValue = possibleValues.remove(coded & MASK_6);
                coded >>= 6;
            }
            for (int j = 0; j < 3; j++) {
                result[cursor++] = settingValue;
            }
        }

        // excract chiis
        for (int i = ponCount; i < 4; i++) {
            settingValue = POSSIBLE_CHII[coded & MASK_5];
            coded >>= 5;
            for (int j = 0; j < 3; j++) {
                result[cursor++] = settingValue + j;
            }
        }

        // exctract pair
        settingValue = possibleValues.remove(coded & MASK_6);
        for (int j = 0; j < 2; j++) {
            result[cursor++] = settingValue;
        }
        return result;
    }

    /**
     *
     * @param hand in format: [PONS] [CHIIS] PAIR
     * @return
     */
    public static int code(int[] hand) {
        if (hand.length != 14) {
            throw new RuntimeException();
        }
        int result = 0;
        ArrayList<Integer> possibleValues = new ArrayList<Integer>(Arrays.asList(POSSIBLE_PON));

        int ponsCount = 0;
        for (int cursor = 0; cursor < hand.length-2; cursor += 3) {
            if (hand[cursor] == hand[cursor + 1]) {
                // code pon

                int removingIndex = possibleValues.indexOf(hand[cursor]);
                result |= removingIndex;
                possibleValues.remove(removingIndex);

                if (++ponsCount > 2) {
                    result = Integer.rotateRight(result, 5);
                } else {
                    result = Integer.rotateRight(result, 6);
                }
            } else {
                // code chii
                result |= Arrays.binarySearch(POSSIBLE_CHII, hand[cursor]);
                result = Integer.rotateRight(result, 5);
            }
        }

        // code pair
        result |= possibleValues.indexOf(hand[hand.length - 1]);
        result = Integer.rotateRight(result, 6);

        // code header
        if (ponsCount == 4) {
            result = Integer.rotateRight(result, 3);
            result |= 1;
        } else {
            result = Integer.rotateRight(result, Math.max(4 - ponsCount, 2));
            result |= ponsCount;
            result = Integer.rotateLeft(result, 1);
        }

        return result;
    }

    public static int codeReduced(int[] reducedHand, int ponCount) {
        return code(handFromReduced(reducedHand, ponCount));
    }

    public static int[] handFromReduced(int[] reducedHand, int ponCount) {
        int[] hand = new int[14];
        int cursor = 0;
        for (int i = 0; i < 4; i++) {
            if (ponCount > 0) {
                ponCount--;
                for (int j = 0; j < 3; j++) {
                    hand[cursor++] = reducedHand[i];
                }
            } else {
                for (int j = 0; j < 3; j++) {
                    hand[cursor++] = reducedHand[i] + j;
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            hand[cursor++] = reducedHand[reducedHand.length - 1];
        }
        return hand;
    }

}
