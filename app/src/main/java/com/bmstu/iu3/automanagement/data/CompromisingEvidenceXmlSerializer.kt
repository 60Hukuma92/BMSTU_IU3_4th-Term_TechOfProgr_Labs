@file:Suppress("unused")

package com.bmstu.iu3.automanagement.data

import android.util.Xml
import com.bmstu.iu3.automanagement.models.CompromisingEvidence
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.io.StringWriter

object CompromisingEvidenceXmlSerializer {
	fun toXml(compromisingEvidence: CompromisingEvidence): String {
		val writer = StringWriter()
		val serializer = Xml.newSerializer()

		serializer.setOutput(writer)
		serializer.startDocument("UTF-8", true)
		serializer.startTag("", ROOT_TAG)

		writeTextTag(serializer, PLAYER_NAME_TAG, compromisingEvidence.getPlayerName())
		writeTextTag(serializer, PUSH_BACK_VALUE_TAG, compromisingEvidence.getPushBackValue().toString())
		writeTextTag(serializer, ISSUED_AT_TAG, compromisingEvidence.getIssuedAt().toString())

		serializer.endTag("", ROOT_TAG)
		serializer.endDocument()
		return writer.toString()
	}

	fun fromXml(xml: String): CompromisingEvidence? {
		if (xml.isBlank()) return null

		return try {
			val parser = Xml.newPullParser()
			parser.setInput(StringReader(xml))

			var playerName = ""
			var pushBackValue = 0
			var issuedAt = 0L

			var eventType = parser.eventType
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					when (parser.name) {
						PLAYER_NAME_TAG -> playerName = parser.nextText()
						PUSH_BACK_VALUE_TAG -> pushBackValue = parser.nextText().toIntOrNull() ?: 0
						ISSUED_AT_TAG -> issuedAt = parser.nextText().toLongOrNull() ?: 0L
					}
				}
				eventType = parser.next()
			}

			if (playerName.isBlank() || pushBackValue <= 0) {
				null
			} else {
				CompromisingEvidence().apply {
					setPlayerName(playerName)
					setPushBackValue(pushBackValue)
					setIssuedAt(issuedAt)
				}
			}
		} catch (_: Exception) {
			null
		}
	}

	private fun writeTextTag(serializer: org.xmlpull.v1.XmlSerializer, tag: String, value: String) {
		serializer.startTag("", tag)
		serializer.text(value)
		serializer.endTag("", tag)
	}

	private const val ROOT_TAG = "compromisingEvidence"
	private const val PLAYER_NAME_TAG = "playerName"
	private const val PUSH_BACK_VALUE_TAG = "pushBackValue"
	private const val ISSUED_AT_TAG = "issuedAt"
}


