package tr.edu.iyte.quickreply.rules

import android.content.Context
import tr.edu.iyte.quickreply.R
import java.text.SimpleDateFormat
import java.util.*

data class Rule(val id: String = "",
                var reply: String = "",
                var startTime: Long = 0,
                var endTime: Long = 0,
                val days: MutableList<String> = ArrayList<String>(),
                var isEnabled: Boolean = false) {
    private val FORMATTER = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getDaysString(context: Context): String {
        if (days.size == 1)
            return days[0]

        return with(StringBuilder(32)) {
            append(days[0])
            for (i in 1..days.size - 1 - 1)
                append(", ").append(days[i])
            append(" ").append(context.getString(R.string.and))
            append(" ").append(days[days.size - 1])
        }.toString()
    }

    fun getTimeString(context: Context) =
            "${FORMATTER.format(Date(startTime))} ${context.getString(R.string.clock_seperator)} ${FORMATTER.format(Date(endTime))}"

    fun canClashWith(other: Rule) = this.endTime < other.startTime
            || other.endTime < this.startTime
}