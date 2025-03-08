package com.example.pawfectmatchapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ אתחול האדפטר עם רשימה ריקה של כלבים ורשימת מועדפים ריקה
        dogAdapter = DogAdapter(
            dogList,
            favoriteDogs,
            { dog -> openDogDetails(dog) },
            { dog, isFavorite -> handleFavoriteClick(dog, isFavorite) }
        )

        binding.recyclerViewDogs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDogs.adapter = dogAdapter

        // ✅ הקשבה למועדפים ועדכון בזמן אמת
        homeViewModel.favoriteDogs.observe(viewLifecycleOwner) { favorites ->
            dogAdapter.updateFavorites(favorites) // ✅ עדכון המועדפים באדפטר
        }

        // ✅ הקשבה לכלבים מה-ViewModel
        homeViewModel.dogs.observe(viewLifecycleOwner) { dogs ->
            dogList.clear()
            dogList.addAll(dogs)
            dogAdapter.notifyDataSetChanged()
        }

        // ✅ הבאת הנתונים מה-Firestore
        homeViewModel.fetchDogsFromFirestore()
    }

    /**
     * ✅ עדכון מצב המועדפים ב-Firestore
     */
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
                    println("✅ Favorites updated successfully")
                    homeViewModel.fetchFavoriteDogs() // 🔹 עדכון רשימת המועדפים אחרי שינוי
                }
                .addOnFailureListener { println("❌ Failed to update favorites") }
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
