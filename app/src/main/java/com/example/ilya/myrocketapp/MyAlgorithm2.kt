package com.example.ilya.myrocketapp

import android.util.Pair

import java.util.ArrayList

class MyAlgorithm2(override val currentBitmapState: Array<BooleanArray>) : BaseAlgorithm() {
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

    public override fun run(row: Int, column: Int, curColor: Boolean) {
        clearFields()
        resultOnUpdate.add(Pair(row, column))
        while (hasCurColorNeighbours(curColor)) {
            // profit // тусим тут пока забивается наш onRevertArray как бицуля
        }
    }

    private fun hasCurColorNeighbours(curColor: Boolean): Boolean {
        var result = false
        for (i in resultOnUpdate.size - 1 downTo -1 + 1) {
            val onUpdateItem = resultOnUpdate[i]
            if (!checkedArray!![onUpdateItem.first][onUpdateItem.second]) {
                checkedArray!![onUpdateItem.first][onUpdateItem.second] = true
                if (onUpdateItem.first - 1 >= 0 && !checkedArray!![onUpdateItem.first - 1][onUpdateItem.second] &&
                        currentBitmapState[onUpdateItem.first - 1][onUpdateItem.second] == curColor) {
                    resultOnUpdate.add(Pair(onUpdateItem.first - 1, onUpdateItem.second))
                    result = true
                }
                if (onUpdateItem.first + 1 < currentBitmapState.size && !checkedArray!![onUpdateItem.first + 1][onUpdateItem.second] &&
                        currentBitmapState[onUpdateItem.first + 1][onUpdateItem.second] == curColor) {
                    resultOnUpdate.add(Pair(onUpdateItem.first + 1, onUpdateItem.second))
                    result = true
                }
                if (onUpdateItem.second - 1 >= 0 && !checkedArray!![onUpdateItem.first][onUpdateItem.second - 1] &&
                        currentBitmapState[onUpdateItem.first][onUpdateItem.second - 1] == curColor) {
                    resultOnUpdate.add(Pair(onUpdateItem.first, onUpdateItem.second - 1))
                    result = true
                }
                if (onUpdateItem.second + 1 < currentBitmapState[0].size && !checkedArray!![onUpdateItem.first][onUpdateItem.second + 1] &&
                        currentBitmapState[onUpdateItem.first][onUpdateItem.second + 1] == curColor) {
                    resultOnUpdate.add(Pair(onUpdateItem.first, onUpdateItem.second + 1))
                    result = true
                }
            }
        }
        return result
    }
}
