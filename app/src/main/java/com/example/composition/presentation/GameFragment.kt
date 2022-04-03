package com.example.composition.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.composition.databinding.FragmentGameBinding
import com.example.composition.domain.entity.GameResult

class GameFragment : Fragment() {
    private val args by navArgs<GameFragmentArgs>()
    private val viewModelFactory by lazy {

        GameViewModelFactory(
            level = args.level,
            application = requireActivity().application,
        )
    }
    private val viewModel: GameViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
    }

    private var _binding: FragmentGameBinding? = null
    private val binding: FragmentGameBinding
        get() = _binding ?: throw RuntimeException("FragmentGameBinding == null")

    private val tvOptions by lazy {
        mutableListOf<TextView>().apply {
            with(binding) {
                add(tvOption1)
                add(tvOption2)
                add(tvOption3)
                add(tvOption4)
                add(tvOption5)
                add(tvOption6)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        observeViewModal()
        setClickListeners()
    }

    private fun observeViewModal() {
        with(binding) {
            viewModel?.currentQuestion?.observe(viewLifecycleOwner) {
                tvSum.text = it.sum.toString()
                tvLeftNumber.text = it.visibleNumber.toString()

                tvOptions.forEachIndexed { index, textView ->
                    textView.text = it.options[index].toString()
                }
            }
        }

        viewModel.gameResult.observe(viewLifecycleOwner) {
            launchGameFragment(it)
        }
    }


    private fun setClickListeners() {
        tvOptions.forEach() { textView ->
            textView.setOnClickListener {
                val value = textView.text
                viewModel.chooseAnswer(Integer.parseInt(value as String))
            }
        }
    }

    private fun launchGameFragment(gameResult: GameResult) {
        findNavController().navigate(
            GameFragmentDirections.actionGameFragmentToGameFinishedFragment(
                gameResult
            )
        )
    }

}