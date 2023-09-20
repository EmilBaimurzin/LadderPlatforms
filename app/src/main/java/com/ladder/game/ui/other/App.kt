package com.ladder.game.ui.other

import android.app.Application
import com.ladder.game.data.data_base.Database

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Database.init(this)
    }
}