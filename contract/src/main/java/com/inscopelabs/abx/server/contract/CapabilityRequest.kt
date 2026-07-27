package com.inscopelabs.abx.server.contract

import android.os.Parcel
import android.os.Parcelable

data class CapabilityRequest(
    val operation: String,
    val authorizedPath: String,
    val arguments: Map<String, String>,
    val sessionId: String,
    val protocolVersion: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        operation = parcel.readString() ?: "",
        authorizedPath = parcel.readString() ?: "",
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
        parcel.writeString(operation)
        parcel.writeString(authorizedPath)
        parcel.writeInt(arguments.size)
        for ((key, value) in arguments) {
            parcel.writeString(key)
            parcel.writeString(value)
        }
        parcel.writeString(sessionId)
        parcel.writeInt(protocolVersion)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CapabilityRequest> {
        override fun createFromParcel(parcel: Parcel): CapabilityRequest {
            return CapabilityRequest(parcel)
        }

        override fun newArray(size: Int): Array<CapabilityRequest?> {
            return arrayOfNulls(size)
        }
    }
}
