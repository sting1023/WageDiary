package com.sting.wagediary.data

import java.time.LocalDate
import java.time.YearMonth

/** 工资计算工具(纯函数) */
object WageCalculator {

    /** 给定一个月份的明细,算出本月总工资 */
    fun totalForMonth(entries: List<DayEntry>, settings: Settings): Double =
        entries.sumOf { it.totalWage(settings) }

    /** 过滤出某个月份的条目(按日期升序) */
    fun entriesForMonth(entries: List<DayEntry>, yearMonth: YearMonth): List<DayEntry> =
        entries
            .filter { it.date.month == yearMonth.month && it.date.year == yearMonth.year }
            .sortedBy { it.date }

    /** 按日期索引 */
    fun indexByDate(entries: List<DayEntry>): Map<LocalDate, DayEntry> =
        entries.associateBy { it.date }
}
