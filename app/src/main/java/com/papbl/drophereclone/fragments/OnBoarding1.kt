package com.papbl.drophereclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.papbl.drophereclone.R

class OnBoarding1 : Fragment() {

    private lateinit var nextPage: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.on_boarding_1, container, false)

        nextPage = view.findViewById(R.id.btn_next)

        return view
    }

}