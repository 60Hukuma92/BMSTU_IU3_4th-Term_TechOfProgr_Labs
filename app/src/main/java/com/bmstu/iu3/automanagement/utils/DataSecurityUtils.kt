package com.bmstu.iu3.automanagement.utils

import android.util.Base64
import com.bmstu.iu3.automanagement.models.RaceResult
import com.google.gson.Gson
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/**
 * Утилита для работы с данными: сериализация (JSON/XML) и безопасность (AES).
 * Используется для подготовки ответов на вопросы по лабораторной работе.
 */
object DataSecurityUtils {
    private val gson = Gson()
    
    // Ключ должен быть 32 байта для AES-256
    private val secretKey = "BMSTU_AUTO_MGMT_SECURE_KEY_2024!".toByteArray()
    private val algorithm = "AES/CBC/PKCS5Padding"

    // --- СЕРИАЛИЗАЦИЯ (JSON) ---
    // Используется для сохранения состояния игры (Progress Saving)
    fun <T> toJson(data: T): String = gson.toJson(data)
    fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

    // --- ШИФРОВАНИЕ (AES-256) ---
    // Защищает файлы сохранений от прямого редактирования пользователем.
    // Использует режим CBC (Cipher Block Chaining) для высокой безопасности.
    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(algorithm)
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey, "AES"), ivSpec)
        
        val encrypted = cipher.doFinal(data.toByteArray())
        // Соединяем IV и данные для последующей расшифровки
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 16)
        val encrypted = combined.sliceArray(16 until combined.size)
        
        val cipher = Cipher.getInstance(algorithm)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey, "AES"), ivSpec)
        
        return String(cipher.doFinal(encrypted))
    }

    // --- ЭКСПОРТ (XML) ---
    // Демонстрация работы с форматом XML для отчетов.
    fun generateXmlReport(results: List<RaceResult>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<RaceReport>\n")
        results.forEach { res ->
            sb.append("  <Result>\n")
            sb.append("    <Team>${res.getTeamName()}</Team>\n")
            sb.append("    <Position>${res.getPosition()}</Position>\n")
            sb.append("    <Time>${res.getTimeFormatted()}</Time>\n")
            sb.append("  </Result>\n")
        }
        sb.append("</RaceReport>")
        return sb.toString()
    }
}
