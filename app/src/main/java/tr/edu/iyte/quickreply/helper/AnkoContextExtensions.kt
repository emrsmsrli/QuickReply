package tr.edu.iyte.quickreply.helper

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService

inline fun <reified T: Service> Context.stopService() = this.stopService(Intent(this, T::class.java))

inline fun <reified T: Activity> TileService.startActivityAndCollapse()
        = this.startActivityAndCollapse(Intent(this, T::class.java))