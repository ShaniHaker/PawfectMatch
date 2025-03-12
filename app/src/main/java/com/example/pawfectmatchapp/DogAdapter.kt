package com.example.pawfectmatchapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawfectmatchapp.R
import com.example.pawfectmatchapp.models.DogData
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.widget.AppCompatImageView
import com.example.pawfectmatchapp.PlacesDetailsActivity
import com.google.android.material.button.MaterialButton

class DogAdapter(
    private val dogs: MutableList<DogData>,
    private var favoriteDogs: List<String>,
    private val onDogClicked: (DogData) -> Unit,
    private val onFavoriteClicked: (DogData, Boolean) -> Unit
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dogImage: AppCompatImageView = view.findViewById(R.id.dogImage)
        val dogName: MaterialTextView = view.findViewById(R.id.dogName)
        val dogAge: MaterialTextView = view.findViewById(R.id.dogAge)
        val dogBreed: MaterialTextView = view.findViewById(R.id.dogBreed)
        val dogGender: MaterialTextView = view.findViewById(R.id.dogGender)
        val dogDescription: MaterialTextView = view.findViewById(R.id.dogDescription)
        val favoriteButton: MaterialButton = view.findViewById(R.id.favoriteButton)
        val moreInfoButton: MaterialButton = view.findViewById(R.id.main_BTN)
        val expandableLayout: View = view.findViewById(R.id.expandableLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]

        holder.dogName.text = dog.name
        holder.dogAge.text = "Age: ${dog.age}"
        holder.dogBreed.text = "Breed: ${dog.breed}"
        holder.dogGender.text = "Gender: ${dog.gender}"
        holder.dogDescription.text = "Description: ${dog.description}"

        // Logging data for debugging
        Log.d("DogAdapter", "Updating view for dog: ${dog.name} | Breed: ${dog.breed} | Gender: ${dog.gender} | Description: ${dog.description}")

        // Load dog image using Glide
        Glide.with(holder.itemView.context)
            .load(dog.imageUrl)
            .fitCenter()
            .into(holder.dogImage)

        holder.itemView.findViewById<MaterialButton>(R.id.btnShelterDetails).setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PlacesDetailsActivity::class.java)
            intent.putExtra("PLACE_ID", "ChIJc9YSFYxc-hQRYIrEUpKSg_Y") // Place ID
            context.startActivity(intent)
        }

        // Update favorite status
        val isFavorite = favoriteDogs.contains(dog.id)
        Log.d("DogAdapter", "Dog: ${dog.name} | ID: ${dog.id} | Favorite: $isFavorite | Favorite List: ${favoriteDogs.joinToString()}")
        updateFavoriteUI(holder.favoriteButton, isFavorite)

        // Click listener for favorite button
        holder.favoriteButton.setOnClickListener {
            val newFavoriteStatus = !isFavorite
            Log.d("DogAdapter", "Changing favorite status for ${dog.name}: $newFavoriteStatus")
            updateFavoriteUI(holder.favoriteButton, newFavoriteStatus)
            onFavoriteClicked(dog, newFavoriteStatus)
        }

        // Click listener for expanding details
        holder.moreInfoButton.setOnClickListener {
            val isVisible = holder.expandableLayout.visibility == View.VISIBLE
            holder.expandableLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
            Log.d("DogAdapter", "Toggling details view for ${dog.name}: ${!isVisible}")
        }
    }

    override fun getItemCount(): Int = dogs.size

    /**
     * Updates the list of dogs and refreshes the adapter
     */
    fun updateDogs(newDogs: List<DogData>) {
        Log.d("DogAdapter", "Updating dog list (New size: ${newDogs.size})")
        dogs.clear()
        dogs.addAll(newDogs)
        notifyDataSetChanged()
    }

    /**
     * Updates the favorite list in real-time
     */
    fun updateFavorites(newFavorites: List<String>) {
        Log.d("DogAdapter", "Updating favorites: ${newFavorites.joinToString()}")
        favoriteDogs = newFavorites.toList()
        notifyDataSetChanged()
    }

    /**
     * Updates the favorite button UI (adds or removes the heart icon)
     */
    private fun updateFavoriteUI(button: MaterialButton, isFavorite: Boolean) {
        Log.d("DogAdapter", "Updating UI for favorite button: ${if (isFavorite) "Filled Heart" else "Outlined Heart"}")
        button.setIconResource(if (isFavorite) R.drawable.heart else R.drawable.heart_outlined)
    }
}
