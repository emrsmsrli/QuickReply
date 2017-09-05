package tr.edu.iyte.quickreply.helper

import android.app.Service
import android.content.Context
import android.content.Intent

inline fun <reified T: Service> Context.stopService() = this.stopService(Intent(this, T::class.java))