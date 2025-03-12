package com.example.pawfectmatchapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.util.Log
import com.example.pawfectmatchapp.R
import com.example.pawfectmatchapp.databinding.DialogFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetDialog(
    private val availableBreeds: List<String>,
    private val availableAges: List<Int>,
    private val onFilterApplied: (String?, String?, Int?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FilterDebug", "Received data for the dialog: Breeds = $availableBreeds | Ages = $availableAges")

        // Set up breed selection dropdown
        val breedAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableBreeds)
        val breedDropdown = binding.autoCompleteBreed
        breedDropdown.setAdapter(breedAdapter)

        Log.d("FilterDebug", "Breed adapter initialized with data: ${breedDropdown.adapter}")

        // Show dropdown when the field is clicked
        breedDropdown.setOnClickListener {
            breedDropdown.showDropDown()
        }

        // Set up age selection dropdown
        val ageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableAges.map { it.toString() })
        val ageDropdown = binding.autoCompleteAge
        ageDropdown.setAdapter(ageAdapter)

        Log.d("FilterDebug", "Age adapter initialized with data: ${ageDropdown.adapter}")

        // Show dropdown when the field is clicked
        ageDropdown.setOnClickListener {
            ageDropdown.showDropDown()
        }

        // Apply filter button - send selected values back
        binding.btnApplyFilter.setOnClickListener {
            val selectedName = binding.editTextDogName.text?.toString()?.trim()
            val selectedBreed = binding.autoCompleteBreed.text?.toString()?.trim()
            val selectedAge = binding.autoCompleteAge.text?.toString()?.toIntOrNull()

            Log.d("FilterDebug", "Final filter selection: Name = $selectedName, Breed = $selectedBreed, Age = $selectedAge")

            onFilterApplied(
                if (selectedName.isNullOrEmpty()) null else selectedName,
                if (selectedBreed.isNullOrEmpty()) null else selectedBreed,
                selectedAge
            )

            Log.d("FilterDebug", "User clicked Apply - Selected filters:")
            Log.d("FilterDebug", "Name: $selectedName")
            Log.d("FilterDebug", "Breed: $selectedBreed")
            Log.d("FilterDebug", "Age: $selectedAge")

            dismiss() // Close the dialog after selection
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
