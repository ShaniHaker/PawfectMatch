package com.example.pawfectmatchapp.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectmatchapp.FeedAdapter
import com.example.pawfectmatchapp.databinding.FragmentFeedBinding
import com.example.pawfectmatchapp.models.FeedItem
import com.google.firebase.firestore.FirebaseFirestore

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter
    private val feedList = mutableListOf<FeedItem>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  RecyclerView
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        feedAdapter = FeedAdapter(feedList) { feedItem ->
            toggleFolder(feedItem)
        }
        binding.recyclerViewFeed.adapter = feedAdapter

        // loading data from firestore
        loadFeedUsers()
    }

    //loading only the ones are shared
    private fun loadFeedUsers() {
        db.collection("users")
            .whereEqualTo("sharedInFeed", true) // check if the value in firestore appear true
            .get()
            .addOnSuccessListener { documents ->
                feedList.clear()
                for (document in documents) {
                    val userId = document.id
                    val userName = document.getString("name") ?: "Unknown"
                    val userProfileImageUrl = document.getString("profileImageUrl") ?: ""
                    val sharedDogs = document.get("favorites") as? List<String> ?: emptyList()

                    // adds to the feed only if there are dogs in his favorites
                    if (sharedDogs.isNotEmpty()) {
                        feedList.add(FeedItem(userId, userName, userProfileImageUrl, sharedDogs))
                    }
                }
                feedAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                println("error in loading to Feed")
            }
    }



    //opening folder of the dog
    private fun toggleFolder(feedItem: FeedItem) {
        val index = feedList.indexOf(feedItem)
        if (index != -1) {
            feedList[index].isExpanded = !feedList[index].isExpanded
            feedAdapter.notifyItemChanged(index) // updating adapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
