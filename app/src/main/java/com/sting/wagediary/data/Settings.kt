package com.sting.wagediary.data

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 默认设置(全局)
 *  - defaultDailyRate: 默认日薪(¥/天)
 *  - defaultHourlyRate: 默认加班时薪(¥/小时)
 *  - weekendMultiplier: 周末倍数(工作日默认 1.0,周末自动填这个值)
 */
data class Settings(
    val defaultDailyRate: Double = 400.0,
    val defaultHourlyRate: Double = 50.0,
    val weekendMultiplier: Double = 2.0
) {
    companion object {
        /** 给定日期返回默认倍数:周末用 weekendMultiplier,工作日用 1.0 */
        fun defaultMultiplierFor(date: LocalDate, weekendMultiplier: Double): Double {
            val dow = date.dayOfWeek
            return if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendMultiplier
            } else {
                1.0
            }
        }

        /** 日期类型的中文标签(用于明细列表显示) */
        fun dateTypeLabel(date: LocalDate): String = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }
}
