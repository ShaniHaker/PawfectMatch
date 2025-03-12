package com.example.pawfectmatchapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectmatchapp.adapters.ArticleAdapter
import com.example.pawfectmatchapp.databinding.FragmentNotificationsBinding
import com.example.pawfectmatchapp.models.ArticleData
import android.util.Log

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter
    private val articleList = mutableListOf<ArticleData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewArticles.layoutManager = LinearLayoutManager(requireContext())

        // empty list initialize
        articleAdapter = ArticleAdapter(articleList)
        binding.recyclerViewArticles.adapter = articleAdapter

        // listen to changes on the articles
        notificationsViewModel.articles.observe(viewLifecycleOwner) { articles ->
            Log.d("NotificationsFragment", "📌 received: ${articles.size} articles from-Firestore")
            articleList.clear()
            articleList.addAll(articles)
            articleAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
