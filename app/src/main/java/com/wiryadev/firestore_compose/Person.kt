package com.wiryadev.firestore_compose

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = -1,
) : Parcelable
