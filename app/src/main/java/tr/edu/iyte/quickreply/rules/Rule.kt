package tr.edu.iyte.quickreply.rules

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import tr.edu.iyte.quickreply.R
import java.text.SimpleDateFormat
import java.util.*

data class Rule(val id: String = "",
                var reply: String = "",
                var startTime: Long = 0,
                var endTime: Long = 0,
                val days: MutableList<String> = ArrayList<String>(),
                var isEnabled: Boolean = false) : Parcelable {
    private val FORMATTER = SimpleDateFormat("HH:mm", Locale.getDefault())

    private constructor(parcel: Parcel): this(
            id = parcel.readString(),
            reply = parcel.readString(),
            startTime = parcel.readLong(),
            endTime = parcel.readLong()) {
        parcel.readList(days, null)
        isEnabled = parcel.readInt() == 1
    }

    fun getDaysString(context: Context): String {
        if (days.size == 1)
            return days[0]

        return with(StringBuilder(32)) {
            append(days[0])
            for (i in 1..days.size - 2)
                append(", ").append(days[i])
            append(" ").append(context.getString(R.string.and))
            append(" ").append(days[days.size - 1])
        }.toString()
    }

    fun getTimeString(context: Context) =
            "${FORMATTER.format(Date(startTime))} ${context.getString(R.string.clock_seperator)} ${FORMATTER.format(Date(endTime))}"

    fun canClashWith(other: Rule) = this.endTime < other.startTime
            || other.endTime < this.startTime

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeString(id)
        dest.writeString(reply)
        dest.writeLong(startTime)
        dest.writeLong(endTime)
        dest.writeList(days)
        dest.writeInt(if(isEnabled) 1 else 0)
    }

    override fun describeContents() = 0

    @Suppress("unused")
    val CREATOR = object : Parcelable.Creator<Rule> {
        override fun createFromParcel(source: Parcel?) = Rule(source!!)
        override fun newArray(size: Int): Array<Rule?> {
            return arrayOfNulls(size)
        }
    }
}