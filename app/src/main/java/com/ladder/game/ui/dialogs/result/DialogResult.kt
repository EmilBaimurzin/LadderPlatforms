package com.ladder.game.ui.dialogs.result

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ladder.game.R
import com.ladder.game.core.library.ViewBindingDialog
import com.ladder.game.databinding.DialogResultBinding

class DialogResult : ViewBindingDialog<DialogResultBinding>(DialogResultBinding::inflate) {
    private val args: DialogResultArgs by navArgs()
    private lateinit var viewModel: ResultViewModel
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack(R.id.fragmentMain, false, false)
                true
            } else {
                false
            }
        }

        viewModel =
            ViewModelProvider(this, ResultViewModelFactory(args.stars))[ResultViewModel::class.java]

        binding.close.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }

        binding.restart.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentLadderGame)
        }

        viewModel.results.observe(viewLifecycleOwner) {
            it.forEachIndexed { index, pair ->
                when (index) {
                    0 -> {
                        binding.top1.text = pair.second.toString()
                    }

                    1 -> {
                        binding.top2.text = pair.second.toString()
                    }

                    else -> {
                        binding.yourPlace.text = pair.first.toString()
                        binding.yourTop.text = pair.second.toString()
                    }
                }
            }
        }
    }
}