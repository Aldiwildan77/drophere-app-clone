package com.papbl.drophereclone

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.papbl.drophereclone.fragments.OnBoarding1
import com.papbl.drophereclone.fragments.OnBoarding2
import com.papbl.drophereclone.utils.UserCredential

class OnBoardingActivity : FragmentActivity() {

    companion object {
        val pageOne: Fragment = OnBoarding1()
        val pageTwo: Fragment = OnBoarding2()
    }

    private val credential = UserCredential()
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        viewPager = findViewById(R.id.vp_onBoarding)

        val fragmentsList: List<Fragment> = listOf(pageOne, pageTwo)

        viewPager.adapter = createAdapter(fragmentsList)
    }

    override fun onStart() {
        super.onStart()
        if (credential.getOnBoardingViewed(this)) {
            startActivity(Intent(this, ManagePageActivity::class.java))
            finish()
        }

    }

    private fun createAdapter(list: List<Fragment>): ViewPagerAdapter {
        return ViewPagerAdapter(this, list)
    }

    private inner class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        val fragmentList: List<Fragment>
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}
