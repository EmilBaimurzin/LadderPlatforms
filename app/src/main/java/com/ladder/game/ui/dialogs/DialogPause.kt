package com.ladder.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ladder.game.R
import com.ladder.game.core.library.ViewBindingDialog
import com.ladder.game.databinding.DialogPauseBinding
import com.ladder.game.ui.ladder_game.CallbackViewModel

class DialogPause: ViewBindingDialog<DialogPauseBinding>(DialogPauseBinding::inflate) {
    private val cbViewModel: CallbackViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack()
                cbViewModel.callback?.invoke()
                true
            } else {
                false
            }
        }

        binding.play.setOnClickListener {
            findNavController().popBackStack()
            cbViewModel.callback?.invoke()
        }
    }
}