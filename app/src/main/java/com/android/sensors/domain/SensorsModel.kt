package com.android.sensors.domain

import android.os.Parcel
import android.os.Parcelable
import com.android.sensors.utils.Const.NEW_ID

data class SensorsModel(
    val id: String? = NEW_ID,
    val title: String? = "",
    val description: String? = "",
    val source: String? = "",
    val moreInfo: String? = "",
    val imageUrl: String? = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(source)
        parcel.writeString(moreInfo)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SensorsModel> {
        override fun createFromParcel(parcel: Parcel): SensorsModel {
            return SensorsModel(parcel)
        }

        override fun newArray(size: Int): Array<SensorsModel?> {
            return arrayOfNulls(size)
        }
    }
}