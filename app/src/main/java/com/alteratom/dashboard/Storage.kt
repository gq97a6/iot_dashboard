package com.alteratom.dashboard

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass

object Storage {
    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var rootFolder: String = ""
        set(value) {
            field = value
            path = mapOf(
                Theme::class to "$value/theme",
                Settings::class to "$value/settings",
                Dashboard::class to "$value/dashboards"
            )
        }

    lateinit var path: Map<KClass<out Any>, String>

    fun Any.prepareSave(): String = mapper.writeValueAsString(this)

    fun Any.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[this::class]
            File(path).writeText(save)
        } catch (e: Exception) {
        }
    }

    inline fun <reified T> Collection<T>.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[T::class]
            File(path).writeText(save)
        } catch (e: Exception) {
        }
    }

    inline fun <reified T> getSave() = try {
        FileReader(path[T::class]).readText()
    } catch (e: Exception) {
        ""
    }

    inline fun <reified T> parseSave(save: String = getSave<T>()): T? =
        try {
            mapper.readValue(save, T::class.java)
        } catch (e: Exception) {
            null
        }

    inline fun <reified T> parseListSave(save: String = getSave<T>()): MutableList<T> =
        try {
            mapper.readerForListOf(T::class.java).readValue(save)
        } catch (e: Exception) {
            try {
                mapper.readerForListOf(T::class.java).readValue(save.fixSave())
            } catch (e: Exception) {
                mutableListOf()
            }
        }

    fun String.fixSave(): String {
        var s = this.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.thermostat.", "")
        s = s.replace("com.alteratom.tile.types.time.", "")
        s = s.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.pick.", "")
        s = s.replace("com.alteratom.tile.types.lights.", "")
        s = s.replace("com.alteratom.tile.types.color.", "")
        s = s.replace("com.alteratom.tile.types.switch.", "")
        s = s.replace("com.alteratom.tile.types.slider.", "")
        s = s.replace("com.alteratom.tile.types.terminal.", "")
        return s
    }
}

//com.alteratom.tile.types.button
//com.alteratom.tile.types.thermostat
//com.alteratom.tile.types.time
//com.alteratom.tile.types.button
//com.alteratom.tile.types.pick
//com.alteratom.tile.types.lights
//com.alteratom.tile.types.color
//com.alteratom.tile.types.switch
//com.alteratom.tile.types.slider
//com.alteratom.tile.types.terminal