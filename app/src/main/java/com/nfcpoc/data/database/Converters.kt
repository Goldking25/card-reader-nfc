package com.nfcpoc.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nfcpoc.data.model.ApduExchange
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.MifareSector
import com.nfcpoc.data.model.UltralightPage

/**
 * Room TypeConverters for all complex types stored in NfcCard.
 *
 * IMPORTANT — R8/ProGuard compatibility:
 * Anonymous TypeToken subclasses (object : TypeToken<List<Foo>>() {})
 * are renamed by R8 and break at runtime with "TypeToken compress" errors.
 *
 * All deserializers here use TypeToken.getParameterized() which is
 * the R8-safe API recommended by the Gson team for minified builds.
 */
class Converters {

    private val gson = Gson()

    // ─── CardType ─────────────────────────────────────────────────────────────

    @TypeConverter
    fun fromCardType(type: CardType): String = type.name

    @TypeConverter
    fun toCardType(name: String?): CardType =
        if (name.isNullOrBlank()) CardType.UNKNOWN
        else runCatching { CardType.valueOf(name) }.getOrDefault(CardType.UNKNOWN)

    // ─── List<String> — techList ──────────────────────────────────────────────

    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        gson.toJson(list ?: emptyList<String>())

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        // TypeToken.getParameterized is R8-safe; anonymous subclasses are NOT.
        val type = TypeToken.getParameterized(List::class.java, String::class.java).type
        return runCatching {
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    // ─── List<MifareSector> ───────────────────────────────────────────────────

    @TypeConverter
    fun fromMifareSectors(sectors: List<MifareSector>?): String =
        gson.toJson(sectors ?: emptyList<MifareSector>())

    @TypeConverter
    fun toMifareSectors(json: String?): List<MifareSector> {
        if (json.isNullOrBlank()) return emptyList()
        val type = TypeToken.getParameterized(List::class.java, MifareSector::class.java).type
        return runCatching {
            gson.fromJson<List<MifareSector>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    // ─── List<UltralightPage> ─────────────────────────────────────────────────

    @TypeConverter
    fun fromUltralightPages(pages: List<UltralightPage>?): String =
        gson.toJson(pages ?: emptyList<UltralightPage>())

    @TypeConverter
    fun toUltralightPages(json: String?): List<UltralightPage> {
        if (json.isNullOrBlank()) return emptyList()
        val type = TypeToken.getParameterized(List::class.java, UltralightPage::class.java).type
        return runCatching {
            gson.fromJson<List<UltralightPage>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    // ─── List<ApduExchange> ───────────────────────────────────────────────────

    @TypeConverter
    fun fromApduLog(log: List<ApduExchange>?): String =
        gson.toJson(log ?: emptyList<ApduExchange>())

    @TypeConverter
    fun toApduLog(json: String?): List<ApduExchange> {
        if (json.isNullOrBlank()) return emptyList()
        val type = TypeToken.getParameterized(List::class.java, ApduExchange::class.java).type
        return runCatching {
            gson.fromJson<List<ApduExchange>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
