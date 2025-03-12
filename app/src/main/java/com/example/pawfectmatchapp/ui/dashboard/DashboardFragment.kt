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
import com.google.firebase.firestore.FirebaseFirestore

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

        // define RecyclerView
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())

        //  initialize adapter
        dogAdapter = DogAdapter(
            favoriteDogsList,
            emptyList(),
            { dog -> openDogDetails(dog) },
            { dog, isFavorite -> dashboardViewModel.updateFavoriteStatus(dog, isFavorite) }
        )
        binding.recyclerViewFavorites.adapter = dogAdapter

        //listen to changes in the favorites
        dashboardViewModel.favoriteDogs.observe(viewLifecycleOwner) { dogs ->
            Log.d("DashboardFragment", "🔹 favorites updated: ${dogs.size} dogs")

            favoriteDogsList.clear()
            favoriteDogsList.addAll(dogs)

            dogAdapter.updateFavorites(favoriteDogsList.map { it.id })

            if (dogs.isEmpty()) {
                Toast.makeText(requireContext(), "🐶 Woof woof! Don't forget to add your favorites!", Toast.LENGTH_SHORT).show()
            }

            dogAdapter.notifyDataSetChanged()
        }

        //share in feed bottom listener
        binding.btnShareInFeed.setOnClickListener {
            val currentUser = dashboardViewModel.getCurrentUser()
            if (currentUser != null) {
                shareFavoritesToFeed(currentUser.uid)
            } else {
                Toast.makeText(requireContext(), "error no user logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }



//function to listen to button of share in feed and update firestore

    private fun shareFavoritesToFeed(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(userId)

        userDoc.update("sharedInFeed", true) // mark that user shared his favorites
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Your favorites shared on feed! ✅ ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "error with sharing on feed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun openDogDetails(dog: DogData) {
        println("press on dog: ${dog.name}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
