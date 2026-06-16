package com.sting.wagediary.data

import android.content.Context
import org.json.JSONArray

/**
 * 最近填写的输入历史(最多 3 条)
 *  - 用 SharedPreferences 持久化
 *  - 添加时:去重 → 移到最前 → 截断到 3
 *  - 用途:额外加班金额、备注
 */
object RecentInputs {
    private const val MAX = 3
    private const val PREF_NAME = "wage_diary_recent"
    private const val KEY_EXTRA_OVERTIME = "recent_extra_overtime"
    private const val KEY_DAY_NOTE = "recent_day_note"

    fun getExtraOvertime(ctx: Context): List<String> = getList(ctx, KEY_EXTRA_OVERTIME)
    fun getDayNote(ctx: Context): List<String> = getList(ctx, KEY_DAY_NOTE)

    fun addExtraOvertime(ctx: Context, value: String) {
        if (value.isBlank()) return
        val list = getList(ctx, KEY_EXTRA_OVERTIME).toMutableList()
        list.remove(value)
        list.add(0, value)
        saveList(ctx, KEY_EXTRA_OVERTIME, list.take(MAX))
    }

    fun addDayNote(ctx: Context, value: String) {
        if (value.isBlank()) return
        val list = getList(ctx, KEY_DAY_NOTE).toMutableList()
        list.remove(value)
        list.add(0, value)
        saveList(ctx, KEY_DAY_NOTE, list.take(MAX))
    }

    private fun getList(ctx: Context, key: String): List<String> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveList(ctx: Context, key: String, list: List<String>) {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
