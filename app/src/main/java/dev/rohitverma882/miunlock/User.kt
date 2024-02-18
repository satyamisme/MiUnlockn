package dev.rohitverma882.miunlock

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@Parcelize
data class User(val passToken: String, val userId: String, val deviceId: String) : Parcelable
