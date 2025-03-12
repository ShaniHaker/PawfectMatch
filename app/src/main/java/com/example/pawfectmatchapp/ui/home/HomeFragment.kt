package com.example.pawfectmatchapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectmatchapp.adapters.DogAdapter
import com.example.pawfectmatchapp.databinding.FragmentHomeBinding
import com.example.pawfectmatchapp.models.DogData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var dogAdapter: DogAdapter
    private val dogList = mutableListOf<DogData>()
    private val favoriteDogs = mutableListOf<String>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Variables to store selected filters
    private var selectedBreed: String? = null
    private var selectedAge: Int? = null
    private var selectedName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter
        dogAdapter = DogAdapter(
            dogList,
            favoriteDogs,
            { dog -> openDogDetails(dog) },
            { dog, isFavorite -> handleFavoriteClick(dog, isFavorite) }
        )

        binding.recyclerViewDogs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDogs.adapter = dogAdapter

        // Listen for dog data updates from ViewModel
        homeViewModel.dogs.observe(viewLifecycleOwner) { dogs ->
            dogList.clear()
            dogList.addAll(dogs)
            applyFilters() // Apply filters if any are selected
        }

        // Listen for favorite dog updates
        homeViewModel.favoriteDogs.observe(viewLifecycleOwner) { favorites ->
            dogAdapter.updateFavorites(favorites)
        }

        // Fetch dog data from Firestore
        homeViewModel.fetchDogsFromFirestore()

        // Listen for filter button click
        binding.btnFilter.setOnClickListener {
            openFilterDialog()
        }

        // Listen for clear filter button click
        binding.btnClearFilter.setOnClickListener {
            clearFilters()
        }
    }

    /**
     * Opens the filter dialog
     */
    private fun openFilterDialog() {
        val availableBreeds = homeViewModel.getAvailableBreeds()
        val availableAges = homeViewModel.getAvailableAges()

        Log.d("FilterDebug", "Sending breeds to filter dialog: $availableBreeds")
        Log.d("FilterDebug", "Sending ages to filter dialog: $availableAges")

        val filterDialog = FilterBottomSheetDialog(
            availableBreeds,
            availableAges
        ) { name, breed, age ->
            selectedBreed = breed
            selectedName = name
            selectedAge = age
            applyFilters()
        }
        filterDialog.show(parentFragmentManager, "FilterBottomSheetDialog")
    }

    /**
     * Filters the dog list based on selected criteria
     */
    private fun applyFilters() {
        val filteredList = homeViewModel.dogs.value?.filter { dog ->
            val dogName = dog.name.trim()
            val dogBreed = dog.breed.trim()
            val dogAge = dog.age

            val selectedBreedFixed = selectedBreed?.trim()
            val selectedNameFixed = selectedName?.trim()
            val selectedAgeFixed = selectedAge

            // Check if each filter criterion matches
            val nameMatch = selectedNameFixed?.let { it.equals(dogName, ignoreCase = true) } ?: true
            val breedMatch = selectedBreedFixed?.let { it.equals(dogBreed, ignoreCase = true) } ?: true
            val ageMatch = selectedAgeFixed?.let { it == dogAge } ?: true

            nameMatch && breedMatch && ageMatch
        } ?: emptyList()

        if (filteredList.isEmpty()) {
            Log.w("FilterDebug", "No matching dogs found.")

            // Show a toast message to inform the user
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "No matching dogs found. Try different filters!", Toast.LENGTH_LONG).show()
            }

            // Keep the list empty instead of showing all dogs again
            dogAdapter.updateDogs(emptyList())

        } else {
            Log.d("FilterDebug", "${filteredList.size} dogs match the filters.")
            dogAdapter.updateDogs(filteredList)
        }
    }


     // Clears all selected filters and restores the full list

    private fun clearFilters() {
        selectedBreed = null
        selectedName = null
        selectedAge = null
        dogAdapter.updateDogs(homeViewModel.dogs.value ?: emptyList())
    }


     // Updates the favorite dogs list in Firestore

    private fun handleFavoriteClick(dog: DogData, isFavorite: Boolean) {
        val currentUser = auth.currentUser ?: return
        val userDoc = db.collection("users").document(currentUser.uid)

        userDoc.get().addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()

            if (isFavorite) {
                if (!favorites.contains(dog.id)) favorites.add(dog.id)
            } else {
                favorites.remove(dog.id)
            }

            userDoc.update("favorites", favorites)
                .addOnSuccessListener {
                    println("Favorites updated successfully")
                    homeViewModel.fetchFavoriteDogs()
                }
                .addOnFailureListener { println("Failed to update favorites") }
        }
    }

    private fun openDogDetails(dog: DogData) {
        println("Clicked on: ${dog.name}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
