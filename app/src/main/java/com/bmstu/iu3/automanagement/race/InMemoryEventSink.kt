package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.RaceLogEntry
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryEventSink : RaceEventSink {
    private val list = CopyOnWriteArrayList<RaceLogEntry>()

    override fun publish(event: RaceLogEntry) {
        list.add(event)
    }

    fun getEvents(): List<RaceLogEntry> = Collections.unmodifiableList(list)
}


