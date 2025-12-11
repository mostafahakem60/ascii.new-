package com.smali.translator.util

import java.security.MessageDigest

object HashUtils {
    fun calculateFileHash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
