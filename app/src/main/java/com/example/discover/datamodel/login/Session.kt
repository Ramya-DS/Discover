package com.example.discover.datamodel.login

import com.google.gson.annotations.SerializedName

data class Session(
    @SerializedName("success") val success: Boolean,
    @SerializedName("session_id") val sessionID: String
)