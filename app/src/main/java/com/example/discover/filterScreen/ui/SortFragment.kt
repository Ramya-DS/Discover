package com.example.discover.filterScreen.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.discover.R
import com.example.discover.filterScreen.OnSortOptionSelectedListener

class SortFragment() : DialogFragment() {

    companion object {
        fun newInstance(isMovie: Boolean, options: String?): SortFragment {
            return SortFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isMovie", isMovie)
                    putString("option", options)
                }
            }
        }

        const val POPULARITY_DESC = "popularity.desc"
        const val POPULARITY_ASC = "popularity.asc"
        const val RELEASE_DATE_DESC = "release_date.desc"
        const val RELEASE_DATE_ASC = "release_date.asc"
        const val FIRST_AIR_DESC = "first_air_date.desc"
        const val FIRST_AIR_ASC = "first_air_date.asc"
        const val VOTE_AVERAGE_DESC = " vote_average.desc"
        const val VOTE_AVERAGE_ASC = " vote_average.asc"
    }

    private var isMovie: Boolean = true
    private var option =
        POPULARITY_DESC
    var onSortOptionSelectedListener: OnSortOptionSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isMovie = getBoolean("isMovie")
            option = getString("option", POPULARITY_DESC)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sort, container, false)

        val sortGroup: RadioGroup = rootView.findViewById(R.id.sort_group)

        when (option) {
            POPULARITY_ASC -> rootView.findViewById<RadioButton>(R.id.sort_popularity_asc)
                .isChecked = true
            RELEASE_DATE_DESC, FIRST_AIR_DESC -> rootView.findViewById<RadioButton>(R.id.sort_release_date_desc)
                .isChecked = true
            RELEASE_DATE_ASC, FIRST_AIR_ASC -> rootView.findViewById<RadioButton>(R.id.sort_release_date_asc)
                .isChecked = true
            VOTE_AVERAGE_DESC -> rootView.findViewById<RadioButton>(R.id.sort_vote_average_desc)
                .isChecked = true
            VOTE_AVERAGE_ASC -> rootView.findViewById<RadioButton>(R.id.sort_vote_average_asc)
                .isChecked = true
        }

        sortGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.sort_popularity_desc -> onSortOptionSelectedListener?.onSortSelected(
                    POPULARITY_DESC
                )
                R.id.sort_popularity_asc -> onSortOptionSelectedListener?.onSortSelected(
                    POPULARITY_ASC
                )
                R.id.sort_release_date_desc -> {
                    if (isMovie)
                        onSortOptionSelectedListener?.onSortSelected(RELEASE_DATE_DESC)
                    else
                        onSortOptionSelectedListener?.onSortSelected(FIRST_AIR_DESC)
                }
                R.id.sort_release_date_asc -> {
                    if (isMovie)
                        onSortOptionSelectedListener?.onSortSelected(RELEASE_DATE_ASC)
                    else
                        onSortOptionSelectedListener?.onSortSelected(FIRST_AIR_ASC)
                }
                R.id.sort_vote_average_desc -> onSortOptionSelectedListener?.onSortSelected(
                    VOTE_AVERAGE_DESC
                )
                R.id.sort_vote_average_asc -> onSortOptionSelectedListener?.onSortSelected(
                    VOTE_AVERAGE_ASC
                )
            }
            this.dismiss()
        }

        return rootView
    }
}
