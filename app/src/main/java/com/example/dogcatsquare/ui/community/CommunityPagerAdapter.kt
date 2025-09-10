package com.example.dogcatsquare.ui.community

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Community 탭 ViewPager2 어댑터.
 * - 동적 탭 갱신 지원: updateFragments()
 * - 안정 갱신을 위해 stableId(getItemId/containsItem) 구현
 */
class CommunityPagerAdapter(
    fragment: Fragment,
    private var fragments: List<Fragment>,
    private var tabTitles: List<String>
) : FragmentStateAdapter(fragment) {

    init {
        require(fragments.size == tabTitles.size) {
            "fragments.size(${fragments.size}) must match tabTitles.size(${tabTitles.size})"
        }
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getTitle(position: Int): String = tabTitles[position]

    /**
     * 동적으로 프래그먼트/탭을 교체.
     * (TabLayoutMediator는 바깥에서 detach/attach 해줄 것)
     */
    fun updateFragments(newFragments: List<Fragment>, newTabTitles: List<String>) {
        require(newFragments.size == newTabTitles.size) {
            "newFragments.size(${newFragments.size}) must match newTabTitles.size(${newTabTitles.size})"
        }
        fragments = newFragments
        tabTitles = newTabTitles
        notifyDataSetChanged()
    }

    // ---- Stable IDs ----
    // 탭 제목을 기반으로 안정적인 ID 부여(제목이 유니크하다는 가정).
    // 만약 같은 제목이 존재할 수 있다면, 제목 + 인덱스 조합 등으로 확장해도 됨.
    override fun getItemId(position: Int): Long {
        return tabTitles.getOrNull(position)?.hashCode()?.toLong()
            ?: fragments[position]::class.java.name.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        // 현재 보유한 탭들 중 동일한 ID가 있으면 존재로 간주
        return tabTitles.any { it.hashCode().toLong() == itemId }
    }
}