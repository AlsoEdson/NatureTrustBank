package com.example.naturetrustbank.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.naturetrustbank.databinding.FragmentRewardsBinding

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val rewardsViewModel =
                ViewModelProvider(this).get(RewardsViewModel::class.java)

        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        rewardsViewModel.text.observe(viewLifecycleOwner) {

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}