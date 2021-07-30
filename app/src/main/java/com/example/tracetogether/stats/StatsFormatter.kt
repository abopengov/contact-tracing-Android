package com.example.tracetogether.stats

import com.example.tracetogether.util.Extensions.withCommas

object StatsFormatter {
    fun formatWithCommas(value: Long?): String {
        return value?.withCommas() ?: "-"
    }
}