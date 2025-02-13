package com.example.dogcatsquare.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentSearchBinding
import com.example.dogcatsquare.ui.map.location.MapFragment

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchEditText()
        setupBackButton()
        // 배경을 불투명하게 설정
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun setupBackButton() {
        binding.backBtn.setOnClickListener {
            // 이전 Fragment를 보이게 하고 현재 Fragment를 제거
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupSearchEditText() {
        binding.editText2.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val searchQuery = textView.text.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    navigateToSearchResult(searchQuery)
                }
                true
            } else {
                false
            }
        }
    }

    private fun navigateToSearchResult(query: String) {
        // SearchResultFragment 인스턴스 생성 및 검색어 전달
        val searchResultFragment = SearchResultFragment().apply {
            arguments = Bundle().apply {
                putString("searchQuery", query)
                // 전달받은 위치 정보를 다시 전달
                putDouble("latitude", arguments?.getDouble("latitude") ?: 37.5665)
                putDouble("longitude", arguments?.getDouble("longitude") ?: 126.9780)
            }
        }

        // Fragment 전환
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
        // 이전 Fragment를 다시 보이게 함
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapFragment>()
            .firstOrNull()?.let { mapFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit()
            }
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 검색어나 다른 상태를 저장
        // outState.putString("searchQuery", binding.searchEditText.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // 저장된 상태 복원
        savedInstanceState?.let { bundle ->
            // binding.searchEditText.setText(bundle.getString("searchQuery", ""))
        }
    }
}