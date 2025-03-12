package com.example.pawfectmatchapp.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawfectmatchapp.models.ArticleData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificationsViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _articles = MutableLiveData<List<ArticleData>>()
    val articles: LiveData<List<ArticleData>> = _articles

    init {
        fetchArticlesFromFirestore()
    }

    private fun fetchArticlesFromFirestore() {
        db.collection("Articles") //capital letter
            .get()
            .addOnSuccessListener { documents ->
                val articleList = mutableListOf<ArticleData>()
                for (document in documents) {
                    val article = ArticleData.Builder()
                        .setId(document.id)
                        .setTitle(document.getString("title") ?: "No Title")
                        .setSummary(document.getString("summary") ?: "No Summary")
                        .setArticleUrl(document.getString("articleUrl") ?: "")
                        .setImageUrl(document.getString("imageUrl") ?: "")
                        .build()
                    articleList.add(article)
                }
                _articles.value = articleList
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "error in receiving articles: ${e.message}")
            }
    }

}
