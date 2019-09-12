package com.payex.mobilesdk.internal

import android.os.Parcel
import android.os.Parcelable

internal const val LOG_TAG = "PayEx"

internal inline fun <reified T> makeCreator(crossinline constructor: (Parcel) -> T): Parcelable.Creator<T> {
    return object : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel) = constructor(source)
        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }
}
