package com.example.pawfectmatchapp.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawfectmatchapp.R
import com.example.pawfectmatchapp.models.ArticleData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.widget.AppCompatImageView

class ArticleAdapter(
    private val articles: MutableList<ArticleData>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val articleImage: AppCompatImageView = view.findViewById(R.id.articleImage)
        val articleTitle: MaterialTextView = view.findViewById(R.id.articleTitle)
        val articleSummary: MaterialTextView = view.findViewById(R.id.articleSummary)
        val btnReadMore: MaterialButton = view.findViewById(R.id.btnReadMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]

        holder.articleTitle.text = article.title
        holder.articleSummary.text = article.summary

        // 📌 הדפסת הנתונים ללוג לבדיקה
        Log.d("ArticleAdapter", "📌 מאמר מוצג -> Title: ${article.title}, ImageURL: ${article.imageUrl}")
        // 📸 טעינת תמונה מה-Storage עם Glide (ללא placeholder)
        Glide.with(holder.itemView.context)
            .load(article.imageUrl)
            .into(holder.articleImage)

        // 🖱️ לחיצה על כפתור "קרא עוד" לפתיחת הקישור
        holder.btnReadMore.setOnClickListener {
            Log.d("ArticleAdapter", "🌍 פתיחת קישור: ${article.articleUrl}")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.articleUrl))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = articles.size

    /**
     * ✅ עדכון רשימת המאמרים והצגת השינויים
     */
    fun updateArticles(newArticles: List<ArticleData>) {
        Log.d("ArticleAdapter", "🔄 עדכון רשימת המאמרים (גודל חדש: ${newArticles.size})")
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }
}
