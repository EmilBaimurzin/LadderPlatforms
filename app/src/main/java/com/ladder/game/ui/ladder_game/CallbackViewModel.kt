package com.ladder.game.ui.ladder_game

import androidx.lifecycle.ViewModel

class CallbackViewModel: ViewModel() {
    var callback: (()->Unit)? = null
}