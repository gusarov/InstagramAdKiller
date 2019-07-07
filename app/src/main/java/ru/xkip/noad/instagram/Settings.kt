package ru.xkip.noad.instagram

import android.os.Environment
import android.util.Log
import java.io.*

object Settings {
	internal fun setSetting(setting: String, value: String) {
		try {
			val root = File(Environment.getExternalStorageDirectory().toString(), ".Instagram")
			if (!root.exists()) {
				root.mkdirs()
			}
			val file = File(root, "_noad_$setting.txt")
			val writer = FileWriter(file)
			writer.append(value)
			writer.flush()
			writer.close()
			cache[setting] = value
		} catch (e: IOException) {
			Log.e("NOAD", "save error", e)
		}
	}

	val cache = mutableMapOf<String, String>()

	internal fun getSetting(setting: String): String {
		return cache.getOrPut(setting) {
			getSettingInternal(setting)
		}
	}

	fun getSettingInternal(setting: String): String {
		var value: String
		val file = File(Environment.getExternalStorageDirectory().toString() + "/.Instagram/_noad_" + setting + ".txt")
		try {
			val br = BufferedReader(FileReader(file))
			value = br.readLine()
			br.close()
		} catch (e: Exception) {
			value = ""
		}
		return value
	}

}