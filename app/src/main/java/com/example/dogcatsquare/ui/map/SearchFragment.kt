package com.example.dogcatsquare.ui.map

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentSearchBinding
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.map.location.SearchItem
import com.example.dogcatsquare.ui.map.location.SearchWordAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchFragment : Fragment(), SearchWordAdapter.OnSearchTermClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchWordAdapter
    private val recentSearches = mutableListOf<SearchItem>()

    companion object {
        private const val PREFS_NAME = "SearchPrefs"
        private const val KEY_SEARCHES = "recent_searches"
        private const val MAX_RECENT_SEARCHES = 5
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadRecentSearches()

        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchWordAdapter(recentSearches, this)
        binding.searchResultRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = searchAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.right = resources.getDimensionPixelSize(R.dimen.spacing_8)
                }
            })
        }
    }

    private fun loadRecentSearches() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val searchesJson = sharedPreferences.getString(KEY_SEARCHES, null)
        if (searchesJson != null) {
            try {
                val type = object : TypeToken<List<SearchItem>>() {}.type
                val loadedSearches = Gson().fromJson<List<SearchItem>>(searchesJson, type)
                recentSearches.clear()
                recentSearches.addAll(loadedSearches)
                searchAdapter.updateSearches(recentSearches.toList())
            } catch (e: Exception) {
                recentSearches.clear()
                saveRecentSearches()
            }
        }
    }

    private fun saveRecentSearches() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val searchesJson = Gson().toJson(recentSearches)
        sharedPreferences.edit()
            .putString(KEY_SEARCHES, searchesJson)
            .apply()
    }

    private fun addRecentSearch(query: String) {
        // 중복 검색어 제거
        recentSearches.removeIf { it.query == query }

        // 최대 개수 체크
        if (recentSearches.size >= MAX_RECENT_SEARCHES) {
            recentSearches.removeAt(0)
        }

        // 새 검색어 추가
        recentSearches.add(SearchItem(query))
        searchAdapter.notifyDataSetChanged()
        saveRecentSearches()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchEditText()
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun setupSearchEditText() {
        binding.editText2.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val searchQuery = textView.text.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    addRecentSearch(searchQuery)
                    navigateToSearchResult(searchQuery)
                }
                true
            } else {
                false
            }
        }
    }

    override fun onSearchTermClicked(query: String) {
        binding.editText2.setText(query)
        navigateToSearchResult(query)
    }

    override fun onSearchTermRemoved(query: String) {
        // 1. 먼저 현재 리스트에서 해당 아이템 제거
        val index = recentSearches.indexOfFirst { it.query == query }
        if (index != -1) {
            recentSearches.removeAt(index)

            // 2. SharedPreferences에 변경사항 저장
            saveRecentSearches()

            // 3. 어댑터에 변경 알림
            searchAdapter.updateSearches(recentSearches.toList())
        }
    }

    private fun navigateToSearchResult(query: String) {
        val searchResultFragment = SearchResultFragment().apply {
            arguments = Bundle().apply {
                putString("searchQuery", query)
                putDouble("latitude", arguments?.getDouble("latitude") ?: 37.5665)
                putDouble("longitude", arguments?.getDouble("longitude") ?: 126.9780)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .hide(this)
            .add(R.id.main_frm, searchResultFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapFragment>()
            .firstOrNull()?.let { mapFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit()
            }
        _binding = null
    }
}