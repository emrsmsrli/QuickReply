package tr.edu.iyte.quickreply.helper

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import android.support.v4.app.Fragment as SupportFragment

inline fun <reified T: Service> Context.stopService() = this.stopService(Intent(this, T::class.java))
inline fun <reified T: Service> Fragment.stopService() = activity.stopService(Intent(activity, T::class.java))
inline fun <reified T: Service> SupportFragment.stopService() = activity.stopService(Intent(activity, T::class.java))

inline fun <reified T: Activity> TileService.startActivityAndCollapse()
        = this.startActivityAndCollapse(Intent(this, T::class.java))

fun <T> Collection<T>.asSameAs(other: Collection<T>): Boolean {
    if(this.size != other.size)
        return false
    return all { other.contains(it) }
}