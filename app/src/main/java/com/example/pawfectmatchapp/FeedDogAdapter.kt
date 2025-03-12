package com.example.pawfectmatchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawfectmatchapp.models.DogData
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FeedDogAdapter(
    private val dogs: List<DogData>
) : RecyclerView.Adapter<FeedDogAdapter.FeedDogViewHolder>() {

    class FeedDogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dogImage: AppCompatImageView = view.findViewById(R.id.dogImage)
        val dogName: MaterialTextView = view.findViewById(R.id.dogName)
        val dogAge: MaterialTextView = view.findViewById(R.id.dogAge)
        val dogBreed: MaterialTextView = view.findViewById(R.id.dogBreed)
        val dogGender: MaterialTextView = view.findViewById(R.id.dogGender)
        val dogDescription: MaterialTextView = view.findViewById(R.id.dogDescription)
        val expandableLayout: View = view.findViewById(R.id.expandableLayout)
        val expandButton: ExtendedFloatingActionButton = view.findViewById(R.id.main_BTN)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedDogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_dog, parent, false)
        return FeedDogViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedDogViewHolder, position: Int) {
        val dog = dogs[position]

        // loading image with glide
        Glide.with(holder.itemView.context)
            .load(dog.imageUrl)
            .placeholder(R.drawable.account)
            .into(holder.dogImage)

        // updating info
        holder.dogName.text = dog.name
        holder.dogAge.text = "Age: ${dog.age}"
        holder.dogBreed.text = "Breed: ${dog.breed}"
        holder.dogGender.text = "Gender: ${dog.gender}"
        holder.dogDescription.text = dog.description

        // manage toggle of expended info
        holder.expandButton.setOnClickListener {
            val isExpanded = holder.expandableLayout.visibility == View.VISIBLE
            holder.expandableLayout.visibility = if (isExpanded) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = dogs.size
}
