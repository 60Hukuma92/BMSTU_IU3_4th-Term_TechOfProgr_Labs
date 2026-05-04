package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.CommentatorMessage
import com.bmstu.iu3.automanagement.models.RaceLogEntry
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale

class CommentatorWorker(
    private val sourceEvents: ReceiveChannel<RaceLogEntry>,
    private val outputMessages: CopyOnWriteArrayList<CommentatorMessage>
) : RaceWorker {

    override val id: String = "commentator"
    @Volatile
    private var active: Boolean = true

    override suspend fun start() {
        while (active) {
            val event = sourceEvents.receiveCatching().getOrNull() ?: break
            outputMessages.add(event.toCommentatorMessage())
        }
    }

    override suspend fun stop() {
        active = false
    }

    private fun RaceLogEntry.toCommentatorMessage(): CommentatorMessage {
        return CommentatorMessage(
            displayTime = formatTimestamp(timestampMs),
            source = source,
            message = message,
            severity = severity
        )
    }

    private fun formatTimestamp(timestampMs: Long): String {
        val totalSeconds = timestampMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = timestampMs % 1000
        return String.format(Locale.US, "[%02d:%02d.%03d]", minutes, seconds, millis)
    }
}



