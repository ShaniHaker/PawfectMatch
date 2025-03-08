package com.example.pawfectmatchapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectmatchapp.adapters.DogAdapter
import com.example.pawfectmatchapp.databinding.FragmentDashboardBinding
import com.example.pawfectmatchapp.models.DogData
import android.util.Log
import android.widget.Toast

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var dogAdapter: DogAdapter
    private val favoriteDogsList = mutableListOf<DogData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ שינוי ל-LinearLayoutManager כדי להציג בעמודה אחת
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())


        // ✅ אתחול האדפטר
        dogAdapter = DogAdapter(
            favoriteDogsList,
            emptyList(),  // ⬅️ יתעדכן מאוחר יותר
            { dog -> openDogDetails(dog) },
            { dog, isFavorite -> dashboardViewModel.updateFavoriteStatus(dog, isFavorite) }
        )
        binding.recyclerViewFavorites.adapter = dogAdapter

        // ✅ מאזין לשינויים במועדפים
        dashboardViewModel.favoriteDogs.observe(viewLifecycleOwner) { dogs ->
            Log.d("DashboardFragment", "🔹 מועדפים התעדכנו: ${dogs.size} כלבים") // 📌 הדפסה ללוג

            favoriteDogsList.clear()
            favoriteDogsList.addAll(dogs)

            // ✅ עדכון IDs של המועדפים לפני העדכון הכללי
            dogAdapter.updateFavorites(favoriteDogsList.map { it.id })

            if (dogs.isEmpty()){
                Toast.makeText(requireContext(), "🐶 Woof woof! Don't forget to add your favorites!",Toast.LENGTH_SHORT).show()
            }

            // 📌 רק קריאה אחת ל- notifyDataSetChanged()
            dogAdapter.notifyDataSetChanged()

        }
    }

    private fun openDogDetails(dog: DogData) {
        println("✅ נלחץ על כלב: ${dog.name}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
