package com.example.pawfectmatchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawfectmatchapp.R
import com.example.pawfectmatchapp.adapters.DogAdapter
import com.example.pawfectmatchapp.models.FeedItem
import com.example.pawfectmatchapp.models.DogData
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FeedAdapter(
    private val feedList: List<FeedItem>,
    private val onFolderClicked: (FeedItem) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ShapeableImageView = view.findViewById(R.id.userProfileImage)
        val userName: MaterialTextView = view.findViewById(R.id.userName)
        val btnExpand: MaterialButton = view.findViewById(R.id.btnExpand)
        val recyclerViewDogs: RecyclerView = view.findViewById(R.id.recyclerViewDogs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feedItem = feedList[position]

        // 🔹 הצגת תמונת המשתמש ושם
        holder.userName.text = feedItem.userName
        Glide.with(holder.itemView.context)
            .load(feedItem.userProfileImageUrl)
            .placeholder(R.drawable.account) // תמונה ברירת מחדל
            .into(holder.userProfileImage)

        // 🔹 הגדרת כפתור ההרחבה
        holder.btnExpand.setOnClickListener {
            onFolderClicked(feedItem)
        }

        // 🔹 הצגת הכלבים אם התקייה פתוחה
        if (feedItem.isExpanded) {
            holder.recyclerViewDogs.visibility = View.VISIBLE
            setupDogsRecyclerView(holder.recyclerViewDogs, feedItem.sharedDogs)
        } else {
            holder.recyclerViewDogs.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = feedList.size

    //inside recycle view for each dog
    private fun setupDogsRecyclerView(recyclerView: RecyclerView, dogIds: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val dogList = mutableListOf<DogData>()

        if (dogIds.isEmpty()) {
            recyclerView.adapter = FeedDogAdapter(dogList)
            return
        }

        db.collection("Dogs")
            .whereIn(FieldPath.documentId(), dogIds)
            .get()
            .addOnSuccessListener { documents ->
                dogList.clear()
                for (document in documents) {
                    val dog = DogData(
                        id = document.id,
                        name = document.getString("name") ?: "Unknown",
                        age = document.getLong("age")?.toInt() ?: 0,
                        breed = document.getString("breed") ?: "Unknown",
                        gender = document.getString("gender") ?: "Unknown",
                        description = document.getString("description") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    dogList.add(dog)
                }
                recyclerView.adapter = FeedDogAdapter(dogList)
            }
            .addOnFailureListener {
                println("error in data")
            }
    }



}
