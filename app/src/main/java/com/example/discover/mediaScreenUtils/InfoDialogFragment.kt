package com.example.discover.mediaScreenUtils


import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.movieScreen.MovieActivity
import com.google.android.material.appbar.MaterialToolbar
import java.lang.ref.WeakReference
import java.util.*

class InfoDialogFragment : DialogFragment() {

    companion object {
        fun newInfoInstance(list: List<InfoClass>, title: String): InfoDialogFragment {
            val fragment =
                InfoDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList("list", list as ArrayList<out Parcelable>)
                putString("title", title)
            }
            return fragment
        }

        fun newCrewInstance(list: List<Crew>, title: String): InfoDialogFragment {
            val fragment =
                InfoDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList("crew", list as ArrayList<out Parcelable>)
                putString("title", title)
            }
            return fragment
        }

        fun newCastInstance(list: List<Cast>, title: String): InfoDialogFragment {
            val fragment =
                InfoDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList("cast", list as ArrayList<out Parcelable>)
                putString("title", title)
            }
            return fragment
        }
    }

    private var info: ArrayList<InfoClass>? = null
    private var crew: ArrayList<Crew>? = null
    private var cast: ArrayList<Cast>? = null
    private lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            info = getParcelableArrayList<InfoClass>("list")
            crew = getParcelableArrayList<Crew>("crew")
            cast = getParcelableArrayList<Cast>("cast")
            title = getString("title")!!
            Log.d("arguments", "$info $crew $cast")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_info_dialog, container, false)

        val toolbar: MaterialToolbar = rootView.findViewById(R.id.fragment_info_toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.title = title

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.fragment_info_tlist)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        info?.let {
            if (activity is MovieActivity)
                recyclerView.adapter =
                    InfoAdapter(
                        it,
                        activity as MovieActivity
                    )
//            else
//                recyclerView.adapter =
//                    InfoAdapter(
//                        it,
//                        activity as ShowActivity
//                    )
        }

        cast?.let {
            Log.d("cast", it.toString())
            recyclerView.adapter = CreditAdapter(
                false,
                WeakReference(activity as Activity)
            ).apply {
                this.castList = it
            }
            recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            })
        }

        crew?.let {
            Log.d("crew", it.toString())
            recyclerView.adapter = CreditAdapter(
                true,
                WeakReference(activity as Activity)
            ).apply {
                this.crewList = it
            }
            recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            })
        }

        return rootView
    }


}
