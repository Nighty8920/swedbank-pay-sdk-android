package com.swedbankpay.mobilesdk

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.swedbankpay.mobilesdk.internal.makeCreator

/**
 * Describes a terminal error condition signaled by an onError callback from Swedbank Pay.
 *
 * See [https://developer.swedbankpay.com/checkout/other-features#onerror]
 */
@Suppress("unused")
class TerminalFailure internal constructor() : Parcelable {
    @SerializedName("origin") private var _origin: String? = null
    @SerializedName("messageId") private var _messageId: String? = null
    @SerializedName("details") private var _details: String? = null

    /**
     * `"consumer"`, `"paymentmenu"`, `"creditcard"`, identifies the system that originated the error.
     */
    val origin get() = _origin
    /**
     * A unique identifier for the message.
     */
    val messageId get() = _messageId
    /**
     * A human readable and descriptive text of the error.
     */
    val details get() = _details

    private constructor(parcel: Parcel) : this() {
        _origin = parcel.readString()
        _messageId = parcel.readString()
        _details = parcel.readString()
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_origin)
        parcel.writeString(_messageId)
        parcel.writeString(_details)
    }
    override fun describeContents() = 0
    companion object {
        @Suppress("unused")
        @JvmField val CREATOR = makeCreator(::TerminalFailure)
    }
}