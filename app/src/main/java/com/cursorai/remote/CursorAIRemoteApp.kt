package com.cursorai.remote

import android.app.Application

class CursorAIRemoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CursorAIRemoteApp
            private set
    }
}
