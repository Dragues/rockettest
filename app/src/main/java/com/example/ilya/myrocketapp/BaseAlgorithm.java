package com.example.ilya.myrocketapp;

import android.util.Pair;

import java.util.ArrayList;

public abstract class BaseAlgorithm {

    abstract ArrayList<Pair<Integer, Integer>> getResultOnUpdate();

    abstract void clearFields();

    abstract void run(int row, int column, boolean curColor);

    abstract boolean[][] getCurrentBitmapState();
}
