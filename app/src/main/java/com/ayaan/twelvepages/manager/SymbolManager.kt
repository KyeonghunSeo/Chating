package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.R

object SymbolManager{

    val stamps = arrayOf(
            R.drawable.add
    )

    enum class Symbol(val resId: Int) {
        DOT(R.drawable.dot),
        IDEA(R.drawable.idea),
        NOTE(R.drawable.note),
        STAR(R.drawable.star),
        HEART(R.drawable.heart)
    }

    fun getSymbolResId(symbol: String?): Int {
        return if (symbol == null) {
            R.drawable.dot
        } else {
            return try {
                Symbol.valueOf(symbol).resId
            } catch (ignored: Exception) {
                R.drawable.dot
            }
        }
    }
}