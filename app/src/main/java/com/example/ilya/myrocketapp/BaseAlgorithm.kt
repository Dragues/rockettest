package com.example.ilya.myrocketapp

import android.util.Pair

import java.util.ArrayList

abstract class BaseAlgorithm {

    internal abstract val resultOnUpdate: ArrayList<Pair<Int, Int>>

    internal abstract val currentBitmapState: Array<BooleanArray>

    internal abstract fun clearFields()

    internal abstract fun run(row: Int, column: Int, curColor: Boolean)
}
