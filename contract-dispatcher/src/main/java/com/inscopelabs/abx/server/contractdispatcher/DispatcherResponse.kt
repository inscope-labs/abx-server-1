package com.inscopelabs.abx.server.contractdispatcher

import android.os.Parcel
import android.os.Parcelable

data class DispatcherResponse(
    val success: Boolean,
    val resultData: String?,
    val errorCode: Int?,
    val errorMessage: String?,
    val protocolVersion: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        success = parcel.readByte() != 0.toByte(),
        resultData = parcel.readString(),
        errorCode = if (parcel.readByte() == 0.toByte()) null else parcel.readInt(),
        errorMessage = parcel.readString(),
        protocolVersion = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (success) 1 else 0)
        parcel.writeString(resultData)
        if (errorCode == null) {
            parcel.writeByte(0)
        } else {
            parcel.writeByte(1)
            parcel.writeInt(errorCode)
        }
        parcel.writeString(errorMessage)
        parcel.writeInt(protocolVersion)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DispatcherResponse> {
        override fun createFromParcel(parcel: Parcel): DispatcherResponse {
            return DispatcherResponse(parcel)
        }

        override fun newArray(size: Int): Array<DispatcherResponse?> {
            return arrayOfNulls(size)
        }
    }
}
