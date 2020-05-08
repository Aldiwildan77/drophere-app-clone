package com.papbl.drophereclone.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.papbl.drophereclone.ManagePageActivity
import com.papbl.drophereclone.R

class OnBoarding2 : Fragment(), View.OnClickListener {

    private lateinit var nextPage: MaterialButton
    private lateinit var previousPage: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.on_boarding_2, container, false)

        nextPage = view.findViewById(R.id.btn_next)
        previousPage = view.findViewById(R.id.btn_previous)

        bindEvent()

        return view
    }

    private fun bindEvent() {
        nextPage.setOnClickListener(this)
        previousPage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == nextPage.id) {
            startActivity(Intent(activity, ManagePageActivity::class.java))
            activity?.finish()
        } else if (v?.id == previousPage.id) {

        }
    }
}