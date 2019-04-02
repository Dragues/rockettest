package com.example.ilya.myrocketapp

import android.util.Pair

import java.util.ArrayList

class MyAlgorithm(override val currentBitmapState: Array<BooleanArray>) : BaseAlgorithm() {
    private var checkedArray: Array<BooleanArray>? = null
    override val resultOnUpdate: ArrayList<Pair<Int, Int>>

    init {
        initCheckedArray()
        this.resultOnUpdate = ArrayList()
    }

    private fun initCheckedArray() {
        checkedArray = Array(currentBitmapState.size) { BooleanArray(currentBitmapState[0].size) }
    }

    override fun clearFields() {
        for (i in checkedArray!!.indices) {
            for (j in 0 .. checkedArray!![i].size - 1) {
                checkedArray!![i][j] = false
            }
        }
        resultOnUpdate.clear()
    }

    override fun run(row: Int, column: Int, curColor: Boolean) {
        clearFields()

        checkedArray!![row][column] = true
        resultOnUpdate.add(Pair(row, column))
        // MyAlgorithm
        tryUp(row, column + 1, curColor)
        tryDown(row, column - 1, curColor)
        tryRight(row + 1, column, curColor)
        tryLeft(row - 1, column, curColor)
    }

    private fun tryUp(i: Int, j: Int, isBlack: Boolean) {
        razbegFunction(i, j, isBlack)
    }

    private fun tryDown(i: Int, j: Int, isBlack: Boolean) {
        razbegFunction(i, j, isBlack)
    }

    private fun tryLeft(i: Int, j: Int, isBlack: Boolean) {
        razbegFunction(i, j, isBlack)
    }

    private fun tryRight(i: Int, j: Int, isBlack: Boolean) {
        razbegFunction(i, j, isBlack)
    }

    private fun razbegFunction(i: Int, j: Int, isBlack: Boolean) {
        if (i > -1 && j > -1 && i < checkedArray!!.size && j < checkedArray!![0].size &&
                !checkedArray!![i][j] && currentBitmapState[i][j] == isBlack) {
            checkedArray!![i][j] = true
            resultOnUpdate.add(Pair(i, j))
            tryUp(i + 1, j, isBlack)
            tryDown(i - 1, j, isBlack)
            tryRight(i, j + 1, isBlack)
            tryLeft(i, j - 1, isBlack)
        } else
            return
    }
}
