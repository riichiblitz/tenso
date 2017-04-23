package com.yufimtsev.mityabot.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ApiResponse {
    public ArrayList<Integer> hand;
    public ArrayList<Integer> used;
    public String string;
    @SerializedName("sorted-string") public String sortedString;
    @SerializedName("used-str") public String usedString;
    public ArrayList<TileDefinition> results;
}
