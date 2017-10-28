package tr.edu.iyte.quickreply.helper

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView
import tr.edu.iyte.quickreply.Database
import android.support.v4.app.Fragment as SupportFragment

inline fun <reified T: Service> Context.stopService() = this.stopService(Intent(this, T::class.java))
inline fun <reified T: Service> Fragment.stopService() = activity.stopService(Intent(activity, T::class.java))
inline fun <reified T: Service> SupportFragment.stopService() = activity.stopService(Intent(activity, T::class.java))

inline fun <reified T: Activity> TileService.startActivityAndCollapse()
        = this.startActivityAndCollapse(Intent(this, T::class.java))

val Context.database: Database
    get() = Database.getInstance(this)
val Fragment.database: Database
    get() = Database.getInstance(activity)
val SupportFragment.database: Database
    get() = Database.getInstance(activity)

fun <T> Collection<T>.asSameAs(other: Collection<T>): Boolean {
    if(this.size != other.size)
        return false
    return all { other.contains(it) }
}