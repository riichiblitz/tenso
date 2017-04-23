package com.yufimtsev.mityabot.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TileDefinition {

    public int tile;
    public int value;
    public int total;
    @SerializedName("improve-mask") public long improveMask;
    public int types;
    @SerializedName("random-string") public String randomString;
    ArrayList<TileDetail> detailed;

}
