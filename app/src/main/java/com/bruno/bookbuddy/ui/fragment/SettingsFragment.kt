package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruno.bookbuddy.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()
    }

    private fun setupSettings() {
        // TODO: Add real settings like theme, language, notifications
        binding.tvSettingsPlaceholder.text = "Settings will be implemented here:\n\n• App Theme (Light/Dark)\n• Language (HR/EN)\n• Reading Reminders\n• Notification Preferences"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}