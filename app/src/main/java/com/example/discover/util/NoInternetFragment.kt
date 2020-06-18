package com.example.discover.util


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.discover.R

/**
 * A simple [Fragment] subclass.
 */
class NoInternetFragment : Fragment() {

    companion object {
        fun newInstance(content: String): NoInternetFragment {
            return NoInternetFragment().apply {
                arguments = Bundle().apply {
                    putString("content", content)
                }
            }
        }
    }

    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = arguments?.getString("content")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_no_internet, container, false)

        val contentTextView: TextView = rootView.findViewById(R.id.no_internet_text)

        content?.let {
            contentTextView.text = it
        }

        return rootView
    }


}
