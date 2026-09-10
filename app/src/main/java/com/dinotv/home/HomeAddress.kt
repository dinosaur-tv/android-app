package com.dinotv.home

import java.net.URI

object HomeAddress {
    fun normalize(input: String): String? = try {
        val uri = URI(input.trim())
        if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.rawUserInfo != null ||
            uri.rawQuery != null || uri.rawFragment != null ||
            uri.port !in -1..65535 || uri.port == 0 ||
            uri.path !in listOf("", "/", "/console", "/console/")) null
        else URI("https", null, uri.host.lowercase(), uri.port, "/console/", null, null).toASCIIString()
    } catch (_: Exception) { null }

    fun sameOrigin(home: String, target: String): Boolean = try {
        val a = URI(home)
        val b = URI(target)
        b.scheme == "https" && b.rawUserInfo == null &&
            a.host.equals(b.host, ignoreCase = true) &&
            (if (a.port == -1) 443 else a.port) == (if (b.port == -1) 443 else b.port)
    } catch (_: Exception) { false }
}
