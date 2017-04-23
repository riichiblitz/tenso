package com.yufimtsev.mahjongai;

import java.io.*;

/**
 * Created by yufimtsev on 31.07.2016.
 */
public class WinningHandler {

    public static void saveWinningHands(String filename) {
        File file = new File(filename);
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream stream = null;

        try {
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fileOutputStream);

            int count = 0;
            long total = 0;
            int[] reducedHand = new int[5];
            for (int i = 1; i < 35; i++) {
                reducedHand[0] = i;
                for (int j = 1; j < 35; j++) {
                    reducedHand[1] = j;
                    for (int k = 1; k < 35; k++) {
                        reducedHand[2] = k;
                        for (int l = 1; l < 35; l++) {
                            reducedHand[3] = l;
                            for (int m = 1; m < 35; m++) {
                                reducedHand[4] = m;
                                for (int ponCount = 0; ponCount < 5; ponCount++) {
                                    int coded = runForYourLife(reducedHand, ponCount);
                                    total++;
                                    if (coded != 0) {
                                        count++;
                                        stream.write(intToByteArray(coded));
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println("" + i);
            }

            stream.flush();
            stream.close();
            fileOutputStream.close();

            System.out.println("Result calculated: count is " + count + " out of " + total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] loadWinningHands(String filename) {
        File file = new File(filename);
        FileInputStream fileInputStream = null;
        BufferedInputStream stream = null;
        int[][] result = new int[0][0];
        try {
            result = new int[(int) (file.length() / 4)][14];
            fileInputStream = new FileInputStream(file);
            stream = new BufferedInputStream(fileInputStream);
            byte[] bytes = new byte[4];
            for (int i = 0; i < result.length; i++) {
                if (stream.read(bytes) > 0) {
                    result[i] = HandCoDec.decode(byteArrayToInt(bytes));
                }
            }
            stream.close();
            fileInputStream.close();
        } catch (IOException e) {

        }
        return result;
    }

    public static long[][] loadWinningHandsOptimized(String filename) {
        File file = new File(filename);
        FileInputStream fileInputStream = null;
        BufferedInputStream stream = null;
        long[][] result = new long[0][0];
        try {
            result = new long[(int) (file.length() / 4)][4];
            fileInputStream = new FileInputStream(file);
            stream = new BufferedInputStream(fileInputStream);
            byte[] bytes = new byte[4];
            for (int i = 0; i < result.length; i++) {
                if (stream.read(bytes) > 0) {
                    if (i == 128490) {
                        int a = 0;
                    }
                    result[i] = HandCoDec.decodeOptimized(byteArrayToInt(bytes));
                }
            }
            stream.close();
            fileInputStream.close();
        } catch (IOException e) {

        }
        return result;
    }

    static int[] noReduced = new int[] {30, 7, 14, 24, 2};

    public static int runForYourLife(int[] reducedHand, int ponCount) {
        try {
/*            if (Arrays.equals(reducedHand, noReduced)) {
                int a = 0;
            }*/
            int[] hand = HandCoDec.handFromReduced(reducedHand, ponCount);
            int[] counters = new int[34];

            // check if some chii starts with, for example, 8 man
            // and store only unique sorted hands (for example, 111 222 is the same as 222 111)
            int last = 0;
            int lastPons = ponCount;
            for (int i = 0; i < 4; i++) {
                int it = hand[i*3];

                // sort check
                boolean needToCheck = lastPons-- != 0;
                if (needToCheck && last > 0 && last > it) {
                    return 0;
                }
                last = it;

                // chii sanity check
                if (it != hand[i*3 + 1] &&
                        !(it > 0 && it < 8 || it > 9 && it < 17 || it > 18 && it < 26)) {
                    return 0;
                }
            }

            // we can have max 4 of each tile type
            for (int tile : hand) {
                counters[tile]++;
            }
            for (int counter : counters) {
                if (counter > 4) {
                    return 0;
                }
            }

            return HandCoDec.code(hand);
        } catch (Exception e) {
            // it is ok
            return 0;
        }
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static int byteArrayToInt(byte[] array) {
        return (array[0] & 0x000000FF) << 24 | (array[1] & 0x000000FF) << 16 | (array[2] & 0x000000FF) << 8 | (array[3] & 0x000000FF);
    }
}
