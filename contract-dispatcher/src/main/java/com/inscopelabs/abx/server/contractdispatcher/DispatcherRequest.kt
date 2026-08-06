package com.inscopelabs.abx.server.contractdispatcher

import android.os.Parcel
import android.os.Parcelable

data class DispatcherRequest(
    val prompt: String,
    val originComponent: String,
    val arguments: Map<String, String>,
    val sessionId: String,
    val protocolVersion: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        prompt = parcel.readString() ?: "",
        originComponent = parcel.readString() ?: "",
        arguments = mutableMapOf<String, String>().apply {
            val size = parcel.readInt()
            for (i in 0 until size) {
                val key = parcel.readString() ?: ""
                val value = parcel.readString() ?: ""
                put(key, value)
            }
        },
        sessionId = parcel.readString() ?: "",
        protocolVersion = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(prompt)
        parcel.writeString(originComponent)
        parcel.writeInt(arguments.size)
        for ((key, value) in arguments) {
            parcel.writeString(key)
            parcel.writeString(value)
        }
        parcel.writeString(sessionId)
        parcel.writeInt(protocolVersion)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DispatcherRequest> {
        override fun createFromParcel(parcel: Parcel): DispatcherRequest {
            return DispatcherRequest(parcel)
        }

        override fun newArray(size: Int): Array<DispatcherRequest?> {
            return arrayOfNulls(size)
        }
    }
}
