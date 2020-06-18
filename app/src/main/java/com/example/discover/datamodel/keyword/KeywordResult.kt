package com.example.discover.datamodel.keyword

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.discover.datamodel.media.Media
import com.google.gson.annotations.SerializedName

data class KeywordResult(

    @Embedded var media: Media,

    @SerializedName("keywords")
    @Relation(
        parentColumn = "id",
        entity = Keyword::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaKeywordCrossReference::class,
            parentColumn = "media_id",
            entityColumn = "keyword_id"
        )
    )
    val keywords: List<Keyword>
)