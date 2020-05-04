package com.papbl.drophereclone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemPage(
    var deadline : String,
    var title: String,
    var uniqueCode : String
) : Parcelable