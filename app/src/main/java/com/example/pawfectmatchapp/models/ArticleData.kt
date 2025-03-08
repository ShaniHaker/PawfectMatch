package com.example.pawfectmatchapp.models

data class ArticleData(
    var id: String,
    val title: String,
    val summary: String,
    val articleUrl: String,
    val imageUrl: String
) {
    // Builder Class for Creating an Article Object
    class Builder {
        private var id: String = ""
        private var title: String = ""
        private var summary: String = ""
        private var articleUrl: String = ""
        private var imageUrl: String = ""

        fun setId(id: String) = apply { this.id = id }
        fun setTitle(title: String) = apply { this.title = title }
        fun setSummary(summary: String) = apply { this.summary = summary }
        fun setArticleUrl(articleUrl: String) = apply { this.articleUrl = articleUrl }
        fun setImageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }

        fun build() = ArticleData(id, title, summary, articleUrl, imageUrl)
    }
}
