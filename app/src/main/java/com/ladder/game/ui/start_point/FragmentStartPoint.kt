package com.ladder.game.ui.start_point

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ladder.game.R
import com.ladder.game.core.library.GameFragment
import com.ladder.game.core.library.shortToast
import com.ladder.game.databinding.FragmentStartPointBinding
import kotlinx.coroutines.launch

class FragmentStartPoint :
    GameFragment<FragmentStartPointBinding>(FragmentStartPointBinding::inflate) {
    private val viewModel: StartPointViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            newGame.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.deleteGame()
                    findNavController().navigate(FragmentStartPointDirections.actionFragmentMainToFragmentLadderGame())
                }
            }
            coontinue.setOnClickListener {
                lifecycleScope.launch {
                    if (viewModel.getGame() != null) {
                        findNavController().navigate(FragmentStartPointDirections.actionFragmentMainToFragmentLadderGame())
                    } else {
                        shortToast(requireContext(), "You have no savings")
                    }
                }
            }
            exit.setOnClickListener {
                requireActivity().finish()
            }
        }

        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}