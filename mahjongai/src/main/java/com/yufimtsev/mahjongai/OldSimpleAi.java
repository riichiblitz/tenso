package com.yufimtsev.mahjongai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.yufimtsev.mahjongai.Util.readHand;

public class OldSimpleAi {

    private final int[][] hands;
    private final int interestLevel;
    private ArrayList<ArrayList<Integer>> interestingHands;
    private ArrayList<ArrayList<Integer>> mostInterestingHands;
    private final int[] visibleTiles;
    private final ArrayList<Integer> currentHand;

    private boolean updatingInterestingHands = false;

    private boolean shouldDeclareRiichi = false;

    public OldSimpleAi(int[][] hands, ArrayList<Integer> startHand, int interestLevel) {
        shouldDeclareRiichi = false;
        this.hands = hands;
        currentHand = startHand;
        interestingHands = setup();
        visibleTiles = new int[34];
        for (Integer tile: currentHand) {
            visibleTiles[tile - 1]++; // tile-1 since index starts from 0 and tiles starts from 1
        }
        this.interestLevel = interestLevel;
        updateMostInterestingHands(this.interestingHands, this.interestLevel);
    }

    public OldSimpleAi(int[][] hands, String startHand, int interestLevel) {
        this(hands, readHand(startHand), interestLevel);
    }

    private void updateInterestingHands() {
        if (updatingInterestingHands) {
            return;
        }
        updatingInterestingHands = true;
        //System.out.println("UPDATING STARTED");
        new Thread() {
            @Override
            public void run() {
                final ArrayList<ArrayList<Integer>> interestingHands = setup();
                updateMostInterestingHands(interestingHands, interestLevel);
                critical(new Runnable() {
                    @Override
                    public void run() {

                        OldSimpleAi.this.interestingHands = interestingHands;
                        //System.out.println("UPDATING COMPLETED");
                        updatingInterestingHands = false;
                    }
                });
            }
        }.start();
    }

    private void updateMostInterestingHands(ArrayList<ArrayList<Integer>> interestingHands, int interestLevel) {
        //ArrayList<ArrayList<Integer>> interestingHands = this.interestingHands;
        mostInterestingHands = new ArrayList<ArrayList<Integer>>(interestingHands.size());
        int cursor = 0;
        while (interestLevel > 0 && cursor < interestingHands.size()) {
            ArrayList<Integer> handIds = interestingHands.get(cursor);
            mostInterestingHands.add(cursor, handIds);
            if (handIds.size() > 0) {
                interestLevel--;
            }
            cursor++;
        }
    }

    private synchronized void critical(Runnable criticalSection) {
        criticalSection.run();
    }

    private ArrayList<ArrayList<Integer>> setup() {
        ArrayList<ArrayList<Integer>> handIdsByDistance = new ArrayList<ArrayList<Integer>>(15);
        for (int i = 0; i < 15; i++) {
            handIdsByDistance.add(new ArrayList<Integer>(10000));
        }
        for (int i = 0; i < hands.length; i++) {
            int distance = getDistance(hands[i]);
            if (distance <= 7) {
                handIdsByDistance.get(distance).add(i);
            }
        }
        return handIdsByDistance;
    }

    public void disable(int tile) {
        visibleTiles[tile-1]++;
    }

    public void tsumoRemovingNorth(int tile) {
        currentHand.add(tile / 4 + 1);
        currentHand.remove(new Integer(31));
        Collections.sort(currentHand);
    }

    public boolean shouldDeclareRiichi() {
        return shouldDeclareRiichi;
    }

    public int tsumo(int tile) {
        //System.out.println("Tsumo of code: " + tile + " : " + writeHand(new int[] {tile}));
        currentHand.add(tile);
        Collections.sort(currentHand);
        visibleTiles[tile - 1]++;
        recalculateDistance();
        int discard = naniKiri();
        if (discard == 0) {
            System.out.println("WE WON");
            return 0;
        }
        currentHand.remove(new Integer(discard));

        updateInterestingHands();
        //System.out.println("Discard of code: " + discard + " : " + writeHand(new int[] {discard}));
        //System.out.println("Current hand: " + writeHand(currentHand));
        shouldDeclareRiichi = mostInterestingHands.get(1).size() > 0;
        return discard;
    }

    private void recalculateDistance() {
        // recalculate is called after tsumo, so all the distances are increased by 1 from now on
        int newSize = 15;
        ArrayList<ArrayList<Integer>> newMostInterestingHands = new ArrayList<ArrayList<Integer>>(newSize);
        for (int i = 0; i < newSize; i++) {
            newMostInterestingHands.add(new ArrayList<Integer>());
        }
        for (ArrayList<Integer> handIds : mostInterestingHands) {
            for (Integer handId : handIds) {
                newMostInterestingHands.get(getDistance(hands[handId])).add(handId);
            }
        }
        mostInterestingHands = newMostInterestingHands;
    }

    /**
     *
     * @return 0 if tsumo, >0 - tile value (1..34)
     */
    private int naniKiri() {
        if ((currentHand.size() - 2) % 3 != 0) {
            // this is NOT a hand from which we should discard something
            return -1;
        }
        if (mostInterestingHands.get(0).size() > 0) {
            return 0;
        }
        int maxDistance = mostInterestingHands.size();
        int minDistance = -1;
        int handsWanted = 6; // hand picked value
        for (int i = 0; i < mostInterestingHands.size(); i++) {
            ArrayList<Integer> handsByDistance = mostInterestingHands.get(i);
            int size = handsByDistance.size();
            if (size > 0 && minDistance == -1) {
                minDistance = i;
            }
            if ((handsWanted -= size) < 0) {
                maxDistance = Math.min(i + 1, mostInterestingHands.size() - 1);
                break;
            }
        }
        if (minDistance < 2) {
            maxDistance = minDistance + 1;
        }
        int[] discardSuggestionsByDistance = new int[34];
        for (int distance = 0; distance < maxDistance; distance++) {
            ArrayList<Integer> handsOfDistance = mostInterestingHands.get(distance);
            for (Integer hand : handsOfDistance) {
                addDiscardSuggestions(hands[hand], discardSuggestionsByDistance);
            }
        }
        int maxIndex = discardSuggestionsByDistance.length - 1;
        // going backwards to discard honors first
        // TODO: maybe should leave dragons / our wind
        for (int i = discardSuggestionsByDistance.length - 1; i >= 0; i--) {
            if (discardSuggestionsByDistance[i] > discardSuggestionsByDistance[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex + 1;
    }

    private void addDiscardSuggestions(int[] hand, int[] suggestions) {
        int[] visibleTiles = Arrays.copyOf(this.visibleTiles, this.visibleTiles.length);
        ArrayList<Integer> inputHand = new ArrayList<Integer>(currentHand);
        ArrayList<Integer> neededTiles = new ArrayList<Integer>();
        for (int tile : hand) {
            if (!inputHand.remove(new Integer(tile))) {
                neededTiles.add(tile);
            }
        }
        int possibleWanted = 0;
        int lastTile = 0;
        for (Integer tile : neededTiles) {
            if (++visibleTiles[tile - 1] > 4) {
                // impossible to catch a hand, no need to check what to discard for it
                return;
            }
            if (tile != lastTile) {
                possibleWanted += 4 - this.visibleTiles[tile - 1];
                lastTile = tile;
            }
        }
        int distance = inputHand.size();
        for (Integer tile : inputHand) {
            suggestions[tile - 1] += 100000 * possibleWanted * possibleWanted / Math.pow(5, distance - 1); // tile - 1 since we start from 0
        }
    }


    private int getDistance(int[] hand) {
        // TODO: while recalculating, we should notice visible tiles
        // since distance is not just "hand difference", but also "uke-ire"
        ArrayList<Integer> inputHand = new ArrayList<Integer>(currentHand);
        for (int tile : hand) {
            inputHand.remove(new Integer(tile));
        }
        return inputHand.size();
    }



}
