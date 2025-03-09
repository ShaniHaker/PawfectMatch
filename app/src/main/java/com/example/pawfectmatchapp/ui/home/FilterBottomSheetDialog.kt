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

        Log.d("FilterDebug", "📌 נתונים שהתקבלו לדיאלוג: גזעים = $availableBreeds | גילאים = $availableAges")

        // 🔹 הגדרת תיבת בחירה לגזע
        val breedAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableBreeds)
        val breedDropdown = binding.autoCompleteBreed
        breedDropdown.setAdapter(breedAdapter)

        Log.d("FilterDebug", "✅ נתונים הוזנו לאדפטר של גזעים: ${breedDropdown.adapter}")

        // 🔹 כאשר לוחצים על השדה, הצג רשימה
        breedDropdown.setOnClickListener {
            breedDropdown.showDropDown()
        }

        // 🔹 הגדרת תיבת בחירה לגיל
        val ageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableAges.map { it.toString() })
        val ageDropdown = binding.autoCompleteAge
        ageDropdown.setAdapter(ageAdapter)

        Log.d("FilterDebug", "✅ נתונים הוזנו לאדפטר של גילאים: ${ageDropdown.adapter}")

        // 🔹 כאשר לוחצים על השדה, הצג רשימה
        ageDropdown.setOnClickListener {
            ageDropdown.showDropDown()
        }

        // 🔹 כפתור "Apply Filter" – שליחת הבחירות חזרה
        binding.btnApplyFilter.setOnClickListener {
            val selectedName = binding.editTextDogName.text?.toString()?.trim()
            val selectedBreed = binding.autoCompleteBreed.text?.toString()?.trim()
            val selectedAge = binding.autoCompleteAge.text?.toString()?.toIntOrNull()

            Log.d("FilterDebug", "🔍 בחירה סופית של פילטרים: שם = $selectedName, גזע = $selectedBreed, גיל = $selectedAge")

            onFilterApplied(
                if (selectedName.isNullOrEmpty()) null else selectedName,
                if (selectedBreed.isNullOrEmpty()) null else selectedBreed,
                selectedAge
            )

            Log.d("FilterDebug", "📌 משתמש לחץ על Apply - פילטרים שנבחרו:")
            Log.d("FilterDebug", "👉 שם: $selectedName")
            Log.d("FilterDebug", "👉 גזע: $selectedBreed")
            Log.d("FilterDebug", "👉 גיל: $selectedAge")

            dismiss() // סגירת הדיאלוג לאחר בחירה
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
