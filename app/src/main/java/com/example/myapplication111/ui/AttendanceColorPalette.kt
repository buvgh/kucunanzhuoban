package com.example.myapplication111.ui

import androidx.compose.ui.graphics.Color

data class AttendanceColorOption(
    val label: String,
    val colorInt: Int,
)

val attendanceClassicColors = listOf(
    AttendanceColorOption("默认", 0),
    AttendanceColorOption("米黄", 0xFFFFF3CD.toInt()),
    AttendanceColorOption("浅绿", 0xFFD9F2E6.toInt()),
    AttendanceColorOption("浅蓝", 0xFFD9ECFF.toInt()),
    AttendanceColorOption("浅粉", 0xFFFFE0E6.toInt()),
    AttendanceColorOption("浅橙", 0xFFFFE7CC.toInt()),
    AttendanceColorOption("浅紫", 0xFFE8DDF8.toInt()),
)

fun resolveAttendanceCellColor(colorInt: Int, defaultColor: Color): Color {
    return if (colorInt == 0) defaultColor else Color(colorInt)
}
