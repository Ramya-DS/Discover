package com.example.discover.showsScreen

import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.crew.Crew

interface OnCreditSelectedListener {
    fun onCrewSelected(crew: List<Crew>)
    fun onCastSelected(cast: List<Cast>)
}