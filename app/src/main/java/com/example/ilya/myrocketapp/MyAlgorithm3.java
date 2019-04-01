package com.example.ilya.myrocketapp;

import android.util.Pair;

import java.util.ArrayList;

public class MyAlgorithm3 extends BaseAlgorithm{

    private boolean arrBoolean[][];
    private boolean checkedArray[][];
    private ArrayList<Pair<Integer, Integer>> onRevertArray;

    public MyAlgorithm3(boolean[][] arrBoolean) {
        this.arrBoolean = arrBoolean;
        initCheckedArray();
        this.onRevertArray = new ArrayList<>();
    }

    private void initCheckedArray() {
        checkedArray = new boolean[arrBoolean.length][arrBoolean[0].length];
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> getResultOnUpdate() {
        return onRevertArray;
    }

    @Override
    public void clearFields() {
        for (int i = 0; i < checkedArray.length; i++) {
            for (int j = 0; j < checkedArray[i].length; j++) {
                checkedArray[i][j] = false;
            }
        }
        onRevertArray.clear();
    }

    @Override
    public void run(int row, int column, boolean curColor) {
        clearFields();

        checkedArray[row][column] = true;
        onRevertArray.add(new Pair<>(row, column));
        // MyAlgorithm
        tryRight(row + 1, column, curColor);
        tryDown(row, column - 1, curColor);
        tryLeft(row - 1, column, curColor);
        tryUp(row, column + 1, curColor);
    }

    private  void tryUp(int i, int j, boolean isBlack) {
        razbegFunction(i,j,isBlack);
    }

    private  void tryDown(int i, int j, boolean isBlack) {
        razbegFunction(i,j,isBlack);
    }
    private  void tryLeft(int i, int j, boolean isBlack) {
        razbegFunction(i,j,isBlack);
    }
    private  void tryRight(int i, int j, boolean isBlack) {
        razbegFunction(i,j, isBlack);
    }
    private void razbegFunction(int i, int j, boolean isBlack) {
        if (i > -1 && j > -1 && i < checkedArray.length && j < checkedArray[0].length &&
                !checkedArray[i][j] && arrBoolean[i][j] == isBlack) {
            checkedArray[i][j] = true;
            onRevertArray.add(new Pair<>(i, j));
            tryRight(i,j+1, isBlack);
            tryDown(i-1, j, isBlack);
            tryLeft(i,j-1, isBlack);
            tryUp(i+1, j, isBlack);
        }
        else return;
    }

    @Override
    public boolean[][] getCurrentBitmapState() {
        return arrBoolean;
    }
}
