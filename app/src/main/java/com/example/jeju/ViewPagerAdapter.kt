package com.example.jeju

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    var fragmentList = listOf<FragmentData>()

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragmentData = fragmentList[position]
        val fragment = fragmentData.fragment
        val content = fragmentData.content
        val imageUrl = fragmentData.imageUrl

        val bundle = Bundle()
        bundle.putString("content", content)
        bundle.putString("imageUrl", imageUrl)
        fragment.arguments = bundle

        return fragment
    }
}
class FragmentData(val fragment: Fragment, val content: String, val imageUrl: String?)