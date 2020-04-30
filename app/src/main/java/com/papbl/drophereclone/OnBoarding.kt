package com.papbl.drophereclone

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.papbl.drophereclone.fragments.OnBoarding1
import com.papbl.drophereclone.fragments.OnBoarding2

class OnBoarding : FragmentActivity() {

    companion object {
        val pageOne: Fragment = OnBoarding1()
        val pageTwo: Fragment = OnBoarding2()
    }

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        viewPager = findViewById(R.id.vp_onBoarding)

        val fragmentsList: List<Fragment> = listOf(pageOne, pageTwo)
        Log.d("on-boarding: ", "" + fragmentsList.size)

        viewPager.adapter = createAdapter(fragmentsList)
    }

    private fun createAdapter(list: List<Fragment>) : ViewPagerAdapter {
        val adapter = ViewPagerAdapter(this, list)
        return adapter
    }

    private inner class ViewPagerAdapter(
       fragmentActivity: FragmentActivity,
       val fragmentList: List<Fragment>
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList.get(position)
        }
    }
}
