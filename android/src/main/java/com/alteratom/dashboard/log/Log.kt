package com.alteratom.dashboard.log


class Log {
    val list: MutableList<LogEntry> = mutableListOf()

    fun newEntry(text: String) {
        list.add(0, LogEntry(text))
        while (list.size > 100) list.removeLast()
    }

    fun flush() = list.clear()
}