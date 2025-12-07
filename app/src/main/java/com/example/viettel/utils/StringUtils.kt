package com.example.viettel.utils

import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLEncoder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StringUtils {
    fun getThrowCause(ex: Exception): String {
        val errors = StringWriter()
        ex.printStackTrace(PrintWriter(errors))
        return errors.toString()
    }

    fun currentDateSQLiteformat(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    fun currentDateLog(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)

    fun currentDatetimeSQLiteformat(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)

    fun currentDatetimeMilisecondSQLiteformat(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(Calendar.getInstance().time)

    fun datetimeSQLiteformat(date: Date): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)

    fun stringToDateSQLiteformat(date: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            sdf.parse(date) ?: Date()
        } catch (ex: Exception) {
            Date()
        }
    }

    fun dateSQLiteformat(): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            sdf.parse(currentDateSQLiteformat()) ?: Date()
        } catch (_: Exception) {
            Date()
        }
    }

    fun getBetweenSecond(dtStartDate: Date, dtEndDate: Date): Long {
        return dtEndDate.time - dtStartDate.time
    }

    fun nvl(obj: String?, replace: String = ""): String = obj ?: replace

    @Throws(Exception::class)
    fun encodeParams(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params.get(key)
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }

    fun convertDateToString(date: Date, format: String = "dd/MM/yyyy"): String? {
        val dateFormat: DateFormat = SimpleDateFormat(format, Locale.getDefault())
        return runCatching { dateFormat.format(date) }.getOrNull()
    }

    fun convertStringToDate(strDate: String, format: String = "dd/MM/yyyy"): Date? {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return try {
            formatter.parse(strDate)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun getNameOfDate(date: Date): String? {
        return try {
            SimpleDateFormat("EEEE", Locale.ENGLISH).format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun plusDayToDate(date: String, addDay: Int): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(date)!!
            calendar.add(Calendar.DAY_OF_MONTH, addDay)
            sdf.format(calendar.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
            date
        }
    }

    fun plusDayToDate(date: Date, addDay: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_MONTH, addDay)
            calendar.time
        } catch (ex: Exception) {
            ex.printStackTrace()
            date
        }
    }

    fun plusTimeToDate(date: Date, plusTime: Int, timeType: Int): Date {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(timeType, plusTime)
            calendar.time
        } catch (ex: Exception) {
            ex.printStackTrace()
            date
        }
    }

    fun parseInteger(input: String?): Int {
        if (input.isNullOrEmpty()) return 0
        return input.toInt()
    }

    fun getDateTime(format: String): String {
        return try {
            val dateFormat: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date = Calendar.getInstance().time
            dateFormat.format(date)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getDayAndTime(): String {
        return try {
            getWeekdays() + getDateTime("',' dd 'tháng' MM '/' HH:mm:ss")
        } catch (_: Exception) {
            ""
        }
    }

    fun getTime(): String {
        return try {
            getDateTime("HH:mm:ss")
        } catch (_: Exception) {
            ""
        }
    }

    fun getWeekdays(): String {
        return try {
            when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Thứ hai"
                Calendar.TUESDAY -> "Thứ ba"
                Calendar.WEDNESDAY -> "Thứ tư"
                Calendar.THURSDAY -> "Thứ năm"
                Calendar.FRIDAY -> "Thứ sáu"
                Calendar.SATURDAY -> "Thứ bảy"
                Calendar.SUNDAY -> "Chủ nhật"
                else -> ""
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }
}
