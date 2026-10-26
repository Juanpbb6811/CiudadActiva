package com.ciudadactiva.app

import android.app.Application
import com.ciudadactiva.app.data.local.SessionManager

class CiudadActivaApplication : Application() {

    companion object {
        lateinit var sessionManager: SessionManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
    }
}
