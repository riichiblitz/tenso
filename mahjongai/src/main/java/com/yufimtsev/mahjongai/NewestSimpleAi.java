package com.yufimtsev.mahjongai;

import java.util.ArrayList;
import java.util.Arrays;

import static com.yufimtsev.mahjongai.Util.getLength;
import static com.yufimtsev.mahjongai.Util.readHand;

public class NewestSimpleAi {

    private final static long[][] hands;
    private final static long[][] context;
    private final int interestLevel;
    private int[][] interestingHands;
    private final long[] visibleTiles;
    private final long[] currentHand;

    private boolean shouldDeclareRiichi = false;

    static {
        System.out.println("Start reading");
        hands = /*new long[0][0];//*/WinningHandler.loadWinningHandsOptimized("C:/bin/test2.bin");
        System.out.println("Read. " + hands.length);
        context = new long[8][4]; //
    }

    public NewestSimpleAi(ArrayList<Integer> startHand, int interestLevel) {
        // tiles in range [1...34]
        shouldDeclareRiichi = false;
        currentHand = HandCoDec.decodeOptimized(startHand);
        System.out.println("Start hand: " + Util.writeHand(HandCoDec.encodeOptimized(currentHand)));
        this.interestLevel = interestLevel;
        calculateInterestingHands();
        visibleTiles = new long[4];
        for (Integer tile: startHand) {
            HandCoDec.addTileToHand(visibleTiles, tile);
        }
        //updateMostInterestingHands(this.interestingHands, this.interestLevel);
    }

    public NewestSimpleAi(int[][] hands, String startHand, int interestLevel) {
        this(readHand(startHand), interestLevel);
    }

    private void calculateInterestingHands() {
        System.out.println("calculateInterestingHands start");
        if (interestingHands == null) {
            interestingHands = new int[interestLevel][hands.length];
        }
        for (int i = 0; i < interestingHands.length; i++) {
            Arrays.fill(interestingHands[i], -1);
        }

        int[] cursors = new int[interestLevel];

        for (int i = 0; i < hands.length; i++) {
            int distance = Util.getDistance(currentHand, hands[i]);
            if (distance < interestLevel) {
                interestingHands[distance][cursors[distance]++] = i;
            }
        }
        System.out.println("calculateInterestingHands end");
    }

    public void discard(int who, int tile) {
        //context[who]
    }

    public int tsumo(int tile) { // tile in range [1..34]
        HandCoDec.addTileToHand(currentHand, tile);
        HandCoDec.addTileToHand(visibleTiles, tile);
        System.out.println("Hand after tsumo: " + Util.writeHand(HandCoDec.encodeOptimized(currentHand)));
        calculateInterestingHands();
        if (interestingHands[0][0] != -1) {
            // TODO: agari!
            return -1;
        }

        int discard = naniKiri();
        System.out.println("Discard: " + discard);

        HandCoDec.removeTileFromHand(currentHand, discard);

        System.out.println("Hand after discard: " + Util.writeHand(HandCoDec.encodeOptimized(currentHand)));

        return discard;
    }

    public int naniKiri() {
        System.out.println("naniKiri start");
        long[] differenceToGet = new long[4];
        long[] differenceToCut = new long[4];
        double[] tileValues = new double[34];
        Arrays.fill(tileValues, Double.MIN_VALUE);

        int tilesLeftInWall = 136 - getLength(visibleTiles);

        int maxDistanceDIfference = 1;

        for (int distance = 0; distance < interestingHands.length; distance++) {
            if (interestingHands[distance][0] != -1) {
                if (maxDistanceDIfference-- == 0) {
                    break;
                }
            }

            for (int i = 0; i < interestingHands[distance].length; i++) {
                if (interestingHands[distance][i] == -1) {
                    break;
                }
                Util.getTranslatedDifference(currentHand, hands[interestingHands[distance][i]], differenceToGet, differenceToCut);
                boolean possible = true;
                for (int j = 0; j < differenceToGet.length; j++) {
                    if ((differenceToGet[j] & visibleTiles[j]) != 0) {
                        possible = false;
                        break;
                    }
                }
                if (possible) {
                    double value = getHandValue(differenceToGet, differenceToCut, tilesLeftInWall);
                    for (int j = 0; j < tileValues.length; j++) {
                        int suit = j / 9;
                        long tile =  0x0FL << (j % 9)*4;
                        if ((differenceToCut[suit] & tile) != 0) {
                            if (j == 0) {
                                int a = 0;
                            }
                            tileValues[j] += value;
                        }
                    }
                }
            }
        }

        int maxIndex = 0;
        for (int i = 1; i < tileValues.length; i++) {
            if (tileValues[i] > tileValues[maxIndex]) {
                maxIndex = i;
            }
        }
        System.out.println("naniKiri end");
        return maxIndex + 1; // from 0..33 to 1..34
    }

    public double getHandValue(long[] differenceToGet, long[] differenceToCut, int tilesLeftInWall) {
        // this is not actual out count
        // if we wait for 2-5 with 4 available tiles at the same
        double result = 1;
        for (int i = 0; i < 34; i++) {
            int leftInWall = tilesLeftInWall;
            int suit = i / 9;
            long tileMask = 0x0FL << (i % 9) * 4;
            long tileDifference = differenceToGet[suit] & tileMask;
            double tempResult = 1;
            if (tileDifference != 0) {
                int tilesLeft = Long.bitCount(~visibleTiles[suit] & tileMask);
                int tilesNeed = Long.bitCount(tileDifference);
                for (int j = 0; j < tilesNeed; j++) {
                    tempResult *= tilesLeft-- / (double) leftInWall--;
                }
            }
            /*// TODO; start of strange hack
            if ((differenceToCut[suit] & tileMask) != 0) {
                int tilesInHand = Long.bitCount(currentHand[suit] & tileMask);
                if (tilesInHand > 1) {
                    tempResult /= (double) tilesInHand;
                }
            }
            // TODO: end of strange hack*/
            result *= tempResult;
        }
        return result /*/ (double) getLength(differenceToGet)*/;
    }




}
