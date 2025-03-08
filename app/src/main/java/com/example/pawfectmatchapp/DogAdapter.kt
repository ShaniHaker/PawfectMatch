package com.example.pawfectmatchapp.adapters

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
        val moreInfoButton: MaterialButton = view.findViewById(R.id.main_BTN) // 🔹 כפתור להרחבת המידע
        val expandableLayout: View = view.findViewById(R.id.expandableLayout) // 🔹 תצוגת המידע המורחב
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]

        // 🎨 עדכון UI לכל כלב
        holder.dogName.text = dog.name
        holder.dogAge.text = "Age: ${dog.age}"
        holder.dogBreed.text = "Breed: ${dog.breed}"
        holder.dogGender.text = "Gender: ${dog.gender}"
        holder.dogDescription.text = "Description: ${dog.description}"

        // 🔹 בדיקת נתונים
        Log.d("DogAdapter", "🎨 עדכון תצוגה לכלב: ${dog.name} | גזע: ${dog.breed} | מין: ${dog.gender} | תיאור: ${dog.description}")

        // 📸 טעינת תמונת הכלב
        Glide.with(holder.itemView.context)
            .load(dog.imageUrl)
            .fitCenter()
            .into(holder.dogImage)

        // ✅ עדכון מצב המועדפים
        val isFavorite = favoriteDogs.contains(dog.id)
        Log.d("DogAdapter", "🐶 כלב: ${dog.name} | ID: ${dog.id} | במועדפים: $isFavorite | רשימת מועדפים: ${favoriteDogs.joinToString()}")
        updateFavoriteUI(holder.favoriteButton, isFavorite)

        // 🖱️ לחיצה על כפתור המועדפים (לב)
        holder.favoriteButton.setOnClickListener {
            val newFavoriteStatus = !isFavorite
            Log.d("DogAdapter", "🔄 שינוי סטטוס מועדפים לכלב ${dog.name}: $newFavoriteStatus")
            updateFavoriteUI(holder.favoriteButton, newFavoriteStatus)
            onFavoriteClicked(dog, newFavoriteStatus)
        }

        // 🖱️ לחיצה על הכפתור להרחבת המידע
        holder.moreInfoButton.setOnClickListener {
            val isVisible = holder.expandableLayout.visibility == View.VISIBLE
            holder.expandableLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
            Log.d("DogAdapter", "🔄 שינוי תצוגת הרחבה לכלב ${dog.name}: ${!isVisible}")
        }
    }

    override fun getItemCount(): Int = dogs.size

    /**
     * ✅ עדכון רשימת הכלבים והצגת השינויים
     */
    fun updateDogs(newDogs: List<DogData>) {
        Log.d("DogAdapter", "🔄 עדכון רשימת הכלבים (גודל חדש: ${newDogs.size})")
        dogs.clear()
        dogs.addAll(newDogs)
        notifyDataSetChanged()
    }

    /**
     * ✅ עדכון רשימת המועדפים בזמן אמת
     */
    fun updateFavorites(newFavorites: List<String>) {
        Log.d("DogAdapter", "💖 עדכון מועדפים: ${newFavorites.joinToString()}") // ✅ בדיקה אם יש שם IDים נכונים
        favoriteDogs = newFavorites.toList()
        notifyDataSetChanged()
    }

    /**
     * ✅ פונקציה שמעדכנת את כפתור המועדפים (מוסיפה או מסירה לב)
     */
    private fun updateFavoriteUI(button: MaterialButton, isFavorite: Boolean) {
        Log.d("DogAdapter", "🎨 עדכון UI של כפתור: ${if (isFavorite) "❤️ מלא" else "🤍 ריק"}")
        button.setIconResource(if (isFavorite) R.drawable.heart else R.drawable.heart_outlined)
    }
}
