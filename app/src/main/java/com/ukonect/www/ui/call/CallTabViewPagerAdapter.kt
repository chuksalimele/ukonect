package com.ukonect.www.ui.call


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CallTabViewPagerAdapter(fragment: Fragment,  tab_titles: Array<String>)  : FragmentStateAdapter( fragment) {
    private val titles = tab_titles;
    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> CallDialledTabFragment.newInstance()
            1 -> CallReceivedTabFragment.newInstance()
            else -> CallMissedTabFragment.newInstance()
        }
    }
}