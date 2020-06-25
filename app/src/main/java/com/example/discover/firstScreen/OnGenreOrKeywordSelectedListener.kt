package com.example.discover.firstScreen

interface OnGenreOrKeywordSelectedListener {
    fun onGenreOrKeywordSelected(isMovie: Boolean, genreId: Int, isGenre: Boolean, name:String)
}