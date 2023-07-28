package com.unero.pinar.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.unero.pinar.databinding.FragmentCustomSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomSplashFragment : Fragment() {

    private var _binding: FragmentCustomSplashBinding? = null
    private val binding get() = _binding as FragmentCustomSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(2000)
            findNavController().navigate(CustomSplashFragmentDirections.toHome())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}