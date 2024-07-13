package kr.blugon.melodio

import dev.kord.common.Color
import org.json.JSONObject
import java.io.File

object Settings {
    private val configFile = File("config.json")
    private val settings = JSONObject(configFile.readText())

    private fun JSONObject.getStringOrNull(key: String): String? {
        if(!settings.has(key)) return null
        val value = settings.get(key)
        return if (value is String) return value else null
    }
    private fun JSONObject.getIntOrNull(key: String): Int? {
        if(!settings.has(key)) return null
        val value = settings.get(key)
        return if (value is Int) return value else null
    }
    private fun JSONObject.getLongOrNull(key: String): Long? {
        if(!settings.has(key)) return null
        val value = settings.get(key)
        return if (value is Long) return value else null
    }

    fun nullInspect(inspectTest: Boolean = false): Pair<String, String>? {
        if(inspectTest) {
            when(null) {
                TEST_TOKEN -> return "testToken" to "string"
                TEST_GUILD_ID -> return "testGuildId" to "int"
            }
        }
        return when(null) {
            TOKEN -> "token" to "string"
            GUILD_ID -> "guildId" to "int"

            COLOR_NORMAL -> "token" to "string(hex)"
            COLOR_LOADING -> "token" to "string(hex)"
            COLOR_ERROR -> "token" to "string(hex)"

            LAVALINK_HOST -> "lavalinkHost" to "string"
            LAVALINK_PORT -> "lavalinkPort" to "int"
            LAVALINK_PASSWORD -> "lavalinkPassword" to "string"
            else -> null
        }
    }

    val TOKEN = settings.getStringOrNull("token")
    val GUILD_ID = settings.getLongOrNull("guildId")

    val TEST_TOKEN = settings.getStringOrNull("testToken")
    val TEST_GUILD_ID = settings.getLongOrNull("testGuildId")

    private val COLOR_NORMAL_VALUE = settings.getStringOrNull("colorNormal")
    private val COLOR_LOADING_VALUE = settings.getStringOrNull("colorLoading")
    private val COLOR_ERROR_VALUE = settings.getStringOrNull("colorError")

    val COLOR_NORMAL = if(COLOR_NORMAL_VALUE == null) null else Color(Integer.parseInt(COLOR_NORMAL_VALUE, 16))
    val COLOR_LOADING = if(COLOR_LOADING_VALUE == null) null else Color(Integer.parseInt(COLOR_LOADING_VALUE, 16))
    val COLOR_ERROR = if(COLOR_ERROR_VALUE == null) null else Color(Integer.parseInt(COLOR_ERROR_VALUE, 16))

    val LAVALINK_HOST = settings.getStringOrNull("lavalinkHost")
    val LAVALINK_PORT = settings.getIntOrNull("lavalinkPort")
    val LAVALINK_PASSWORD = settings.getStringOrNull("lavalinkPassword")
}