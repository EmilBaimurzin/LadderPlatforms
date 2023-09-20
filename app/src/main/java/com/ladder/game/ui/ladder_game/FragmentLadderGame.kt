package com.ladder.game.ui.ladder_game

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ladder.game.R
import com.ladder.game.core.library.GameFragment
import com.ladder.game.core.library.l
import com.ladder.game.databinding.FragmentLadderGameBinding
import com.ladder.game.domain.PlatformSize
import com.ladder.game.ui.start_point.FragmentStartPointDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FragmentLadderGame :
    GameFragment<FragmentLadderGameBinding>(FragmentLadderGameBinding::inflate) {
    private val viewModel: LadderGameViewModel by viewModels()
    private val cbViewModel: CallbackViewModel by activityViewModels()
    private var moveScope = CoroutineScope(Dispatchers.Default)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //platform 1 = 230
        //platform 2 = 170
        //platform 3 = 250
        //platform 4 = 215

        //height = 260

        setButtons()

        binding.home.setOnClickListener {
            findNavController().popBackStack()
        }

        cbViewModel.callback = {
            viewModel.pauseState = false
            viewModel.start(
                xy.x.toInt(),
                binding.linearLayout.y.toInt(),
                binding.linearLayout.y.toInt() + dpToPx(80),
                binding.linearLayout.y.toInt() + dpToPx(80) * 2,
                binding.linearLayout.y.toInt() + dpToPx(80) * 3,
                dpToPx(250),
                dpToPx(230),
                dpToPx(215),
                dpToPx(170),
                dpToPx(80),
                binding.player.height,
                binding.player.width,
                dpToPx(60),
                dpToPx(30)
            )
        }

        binding.pause.setOnClickListener {
            viewModel.pauseState = true
            viewModel.stop()
            viewModel.stopMoving()
            findNavController().navigate(R.id.action_fragmentLadderGame_to_dialogPause)
        }

        binding.restart.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(FragmentStartPointDirections.actionFragmentMainToFragmentLadderGame())
        }

        viewModel.playerXY.observe(viewLifecycleOwner) {
            binding.player.x = it.x
            binding.player.y = it.y

            lifecycleScope.launch {
                delay(10)
                if ((it.x + binding.player.width <= 0 || it.y >= xy.y) && viewModel.gameState && !viewModel.pauseState) {
                    viewModel.stop()
                    viewModel.gameState = false
                    viewModel.addResult()
                    viewModel.deleteGame()
                    findNavController().navigate(
                        FragmentLadderGameDirections.actionFragmentLadderGameToDialogResult(
                            viewModel.points.value!!
                        )
                    )
                }
            }
        }

        viewModel.platforms.observe(viewLifecycleOwner) {
            binding.platformsLayout.removeAllViews()
            binding.initialPlatform.isVisible = viewModel.isInitial

            it.forEach { platform ->
                val platformView = ImageView(requireContext())
                val width = when (platform.platformSize) {
                    PlatformSize.EXTRA_LARGE -> dpToPx(250)
                    PlatformSize.LARGE -> dpToPx(230)
                    PlatformSize.MEDIUM -> dpToPx(215)
                    PlatformSize.SMALL -> dpToPx(170)
                }
                val img = when (platform.platformSize) {
                    PlatformSize.EXTRA_LARGE -> R.drawable.platform03
                    PlatformSize.LARGE -> R.drawable.platform01
                    PlatformSize.MEDIUM -> R.drawable.platform04
                    PlatformSize.SMALL -> R.drawable.platform02
                }
                platformView.layoutParams = ViewGroup.LayoutParams(width, dpToPx(60))
                platformView.setImageResource(img)
                platformView.x = platform.x
                platformView.y = platform.y
                binding.platformsLayout.addView(platformView)

                if (platform.hasLadder) {
                    val ladderView = ImageView(requireContext())
                    ladderView.layoutParams = ViewGroup.LayoutParams(dpToPx(40), dpToPx(70))
                    ladderView.setImageResource(R.drawable.ladder01)
                    ladderView.x = platform.x + width - dpToPx(60)
                    ladderView.y =
                        if (platform.isLadderUp) platform.y - dpToPx(60) else platform.y + dpToPx(20)
                    binding.platformsLayout.addView(ladderView)
                }
            }
        }

        viewModel.stars.observe(viewLifecycleOwner) {
            binding.starsLayout.removeAllViews()
            it.forEach { star ->
                val starView = ImageView(requireContext())
                starView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                starView.setImageResource(R.drawable.star)
                starView.x = star.x
                starView.y = star.y
                binding.starsLayout.addView(starView)
            }
        }

        viewModel.points.observe(viewLifecycleOwner) {
            binding.score.text = it.toString()
        }

        lifecycleScope.launch {
            delay(10)
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    xy.x.toInt(),
                    binding.linearLayout.y.toInt(),
                    binding.linearLayout.y.toInt() + dpToPx(80),
                    binding.linearLayout.y.toInt() + dpToPx(80) * 2,
                    binding.linearLayout.y.toInt() + dpToPx(80) * 3,
                    dpToPx(250),
                    dpToPx(230),
                    dpToPx(215),
                    dpToPx(170),
                    dpToPx(80),
                    binding.player.height,
                    binding.player.width,
                    dpToPx(60),
                    dpToPx(30)
                )
            }

            if (viewModel.playerXY.value!!.x == 0f) {
                viewModel.initPlayer(dpToPx(50).toFloat(), xy.y / 2 - (binding.player.height / 2))

                delay(6000)
                if (viewModel.isInitial) {
                    viewModel.jump(
                        binding.linearLayout.y.toInt(),
                        binding.linearLayout.y.toInt() + dpToPx(80),
                        dpToPx(250),
                        dpToPx(230),
                        dpToPx(215),
                        dpToPx(170),
                        dpToPx(80),
                        binding.player.height,
                        binding.player.width,
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingLeft = true
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingLeft = true
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    viewModel.isGoingLeft = false
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingRight = true
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingRight = true
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    viewModel.isGoingRight = false
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonUp.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.jump(
                                    binding.linearLayout.y.toInt(),
                                    binding.linearLayout.y.toInt() + dpToPx(80),
                                    dpToPx(250),
                                    dpToPx(230),
                                    dpToPx(215),
                                    dpToPx(170),
                                    dpToPx(80),
                                    binding.player.height,
                                    binding.player.width,
                                )
                            }
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.jump(
                                    binding.linearLayout.y.toInt(),
                                    binding.linearLayout.y.toInt() + dpToPx(80),
                                    dpToPx(250),
                                    dpToPx(230),
                                    dpToPx(215),
                                    dpToPx(170),
                                    dpToPx(80),
                                    binding.player.height,
                                    binding.player.width,
                                )
                            }
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonDown.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.goDown(dpToPx(80))
                            }
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.goDown(dpToPx(80))
                            }
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}