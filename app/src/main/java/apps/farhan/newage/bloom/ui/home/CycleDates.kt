package apps.farhan.newage.bloom.ui.home


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Days are stored as "epoch day" longs, which avoids java.time.*/
object CycleDates {
    const val DEFAULT_LENGTH = 28
    private const val MS_PER_DAY = 86_400_000L
    private val utc = TimeZone.getTimeZone("UTC")

    fun dayOf(year: Int, month: Int, day: Int): Long {
        val c = Calendar.getInstance(utc)
        c.clear()
        c.set(year, month, day)
        return c.timeInMillis / MS_PER_DAY
    }

    fun today(): Long {
        val c = Calendar.getInstance()
        return dayOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }

    fun format(day: Long, pattern: String = "MMM d"): String {
        val f = SimpleDateFormat(pattern, Locale.getDefault())
        f.timeZone = utc
        return f.format(Date(day * MS_PER_DAY))
    }
}

fun cycleSummary(state: HomeUiState): String {
    val last = state.lastPeriodDay ?: return "Log your last period date"
    val next = last + CycleDates.DEFAULT_LENGTH
    return "Last period ${CycleDates.format(last)} · next around ${CycleDates.format(next)}"
}