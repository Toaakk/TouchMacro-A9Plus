package com.touchmacro.taba9.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.touchmacro.taba9.data.model.MacroStep
import com.touchmacro.taba9.data.model.ScreenOrientation
import com.touchmacro.taba9.data.model.StepType

/**
 * Room Veritabanı için JSON ve Enum Tür Dönüştürücüler
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStepList(value: List<MacroStep>?): String {
        return gson.toJson(value ?: emptyList<MacroStep>())
    }

    @TypeConverter
    fun toStepList(value: String?): List<MacroStep> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<MacroStep>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStepType(type: StepType?): String {
        return type?.name ?: StepType.TAP.name
    }

    @TypeConverter
    fun toStepType(value: String?): StepType {
        return try {
            if (value != null) StepType.valueOf(value) else StepType.TAP
        } catch (e: Exception) {
            StepType.TAP
        }
    }

    @TypeConverter
    fun fromOrientation(orientation: ScreenOrientation?): String {
        return orientation?.name ?: ScreenOrientation.LANDSCAPE.name
    }

    @TypeConverter
    fun toOrientation(value: String?): ScreenOrientation {
        return try {
            if (value != null) ScreenOrientation.valueOf(value) else ScreenOrientation.LANDSCAPE
        } catch (e: Exception) {
            ScreenOrientation.LANDSCAPE
        }
    }
}
