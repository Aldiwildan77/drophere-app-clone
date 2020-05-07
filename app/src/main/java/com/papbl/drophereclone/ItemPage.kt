package com.papbl.drophereclone

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

//@Parcelize
//data class ItemPage(
//    var deadline : Timestamp?,
//    var title: String?,
//    var description: String?,
//    var isDeleted: Boolean?,
//    var ownerId: String?,
//    var password: String?,
//    var uniqueCode : String?
//) : Parcelable

@Parcelize
data class ItemPage(
    var deadline: Timestamp?,
    var deleted: Boolean,
    var description: String?,
    var ownerId: String?,
    var password: String?,
    var title: String?,
    var unique_code: String?
) : Parcelable
