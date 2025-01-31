package com.example.dogcatsquare.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // 최근검색어 로직인데 xml에서 searchview로 안하고 edittext로 구현해놔서 알맞게 수정하시면 될 거 같습니다
        // 최신 검색어
//        val searchView: androidx.appcompat.widget.SearchView = binding.searchView
//        binding.searchRecipeRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        val adapter = SearchWordAdapter(requireContext(), recentSearches, this)
//        binding.searchRecipeRV.adapter = adapter

        // SharedPreferences에서 최근 검색어 불러오기
//        loadRecentSearches()

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
//            androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                if (!query.isNullOrEmpty()) {
//                    addRecentSearch(query)
//                    adapter.notifyDataSetChanged()
//                    navigateToSearchResult(query)
//                }
//
//                return false // 키보드 검색 아이콘 클릭 시 키보드 내림
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return true
//            }
//        })

        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    // 최근검색어 클릭
//    override fun onSearchTermClicked(query: String) {
//        val bundle = Bundle().apply {
//            putString("query", query)
//        }
//        val searchResultFragment = SearchResultFragment().apply {
//            arguments = bundle
//        }
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(R.id.main_frm, searchResultFragment)
//            .addToBackStack("SearchResultFragment") // 백 스택 추가
//            .commitAllowingStateLoss()
//    }

    // 최근검색어 추가
//    private fun addRecentSearch(search: String) {
//        if (recentSearches.size >= 5) { // 5개 넘어가면 맨 처음꺼 삭제
//            recentSearches.removeAt(0)
//        }
//        recentSearches.add(search)
//        saveRecentSearches()
//    }

    // 최근 검색어 SP에 저장
//    private fun saveRecentSearches() {
//        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        val searchSet = recentSearches.toSet()
//        editor.putStringSet(KEY_SEARCHES, searchSet)
//        editor.apply()
//    }
}