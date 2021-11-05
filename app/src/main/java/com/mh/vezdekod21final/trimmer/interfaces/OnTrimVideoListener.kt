package com.mh.vezdekod21final.trimmer.interfaces

import android.net.Uri

interface OnTrimVideoListener {
    fun onTrimStarted()
    fun getResult(startTimeString:String, endTimeString:String)
    fun cancelAction()
    fun onError(message: String)
}
