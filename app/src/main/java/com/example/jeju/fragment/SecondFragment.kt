package com.example.jeju.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jeju.R

class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        val titleTextView = view.findViewById<TextView>(R.id.fragment_title)
        val title = arguments?.getString("title")
        titleTextView.text = title

        val addressTextView = view.findViewById<TextView>(R.id.fragment_address)
        val address = arguments?.getString("address")
        addressTextView.text = address

        val imageUrl = arguments?.getString("imageUrl")
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        return view
    }

}