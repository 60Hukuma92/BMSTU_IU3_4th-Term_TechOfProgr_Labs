@file:Suppress("UseKtx", "CommitPrefEdits")

package com.bmstu.iu3.automanagement.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.bmstu.iu3.automanagement.models.CompromisingEvidence
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlin.random.Random

class CompromisingEvidenceSecureStore(context: Context) {
	private val appContext = context.applicationContext
	private val sharedPreferences: SharedPreferences =
		appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	fun awardCompromisingEvidence(playerName: String, pushBackValue: Int = defaultPushBackValue()): CompromisingEvidence? {
		if (playerName.isBlank()) return null

		val evidence = CompromisingEvidence().apply {
			setPlayerName(playerName)
			setPushBackValue(pushBackValue)
			setIssuedAt(System.currentTimeMillis())
		}

		return if (saveInternal(evidence)) evidence else null
	}

	fun loadCompromisingEvidence(playerName: String): CompromisingEvidence? {
		if (playerName.isBlank()) return null

		val encodedPayload = sharedPreferences.getString(keyForPlayer(playerName), null) ?: return null
		return try {
			val xml = decrypt(encodedPayload)
			CompromisingEvidenceXmlSerializer.fromXml(xml)?.takeIf { it.getPlayerName() == playerName }
		} catch (_: Exception) {
			null
		}
	}

	fun hasCompromisingEvidence(playerName: String): Boolean {
		if (playerName.isBlank()) return false
		return sharedPreferences.contains(keyForPlayer(playerName))
	}

	fun consumeCompromisingEvidence(playerName: String): CompromisingEvidence? {
		val evidence = loadCompromisingEvidence(playerName) ?: return null
		deleteCompromisingEvidence(playerName)
		return evidence
	}

	fun deleteCompromisingEvidence(playerName: String): Boolean {
		if (playerName.isBlank()) return false
		return sharedPreferences.edit().remove(keyForPlayer(playerName)).commit()
	}

	private fun saveInternal(evidence: CompromisingEvidence): Boolean {
		return try {
			val xml = CompromisingEvidenceXmlSerializer.toXml(evidence)
			val encryptedPayload = encrypt(xml)
			sharedPreferences.edit().putString(keyForPlayer(evidence.getPlayerName()), encryptedPayload).apply()
			true
		} catch (_: Exception) {
			false
		}
	}

	private fun encrypt(plainText: String): String {
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
		val iv = cipher.iv
		val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
		val payload = ByteArray(iv.size + encrypted.size)
		System.arraycopy(iv, 0, payload, 0, iv.size)
		System.arraycopy(encrypted, 0, payload, iv.size, encrypted.size)
		return Base64.encodeToString(payload, Base64.NO_WRAP)
	}

	private fun decrypt(encodedPayload: String): String {
		val payload = Base64.decode(encodedPayload, Base64.NO_WRAP)
		require(payload.size > GCM_IV_LENGTH_BYTES) { "Encrypted payload is too short" }

		val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
		val encrypted = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(
			Cipher.DECRYPT_MODE,
			getOrCreateSecretKey(),
			GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
		)
		return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
	}

	private fun getOrCreateSecretKey(): SecretKey {
		val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
		val existing = keyStore.getKey(KEY_ALIAS, null)
		if (existing is SecretKey) {
			return existing
		}

		val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
		val keySpec = KeyGenParameterSpec.Builder(
			KEY_ALIAS,
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		)
			.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
			.setKeySize(KEY_SIZE_BITS)
			.build()

		keyGenerator.init(keySpec)
		return keyGenerator.generateKey()
	}

	private fun keyForPlayer(playerName: String): String {
		val encodedName = Base64.encodeToString(
			playerName.toByteArray(StandardCharsets.UTF_8),
			Base64.NO_WRAP or Base64.URL_SAFE
		)
		return "compromisingEvidence_$encodedName"
	}

	private fun defaultPushBackValue(): Int = Random.nextInt(DEFAULT_PUSH_BACK_MIN, DEFAULT_PUSH_BACK_MAX + 1)

	private companion object {
		private const val PREFS_NAME = "compromising_evidence_store"
		private const val KEY_ALIAS = "auto_management_compromising_evidence_key"
		private const val ANDROID_KEY_STORE = "AndroidKeyStore"
		private const val TRANSFORMATION = "AES/GCM/NoPadding"
		private const val KEY_SIZE_BITS = 256
		private const val GCM_TAG_LENGTH_BITS = 128
		private const val GCM_IV_LENGTH_BYTES = 12
		private const val DEFAULT_PUSH_BACK_MIN = 5
		private const val DEFAULT_PUSH_BACK_MAX = 15
	}
}





