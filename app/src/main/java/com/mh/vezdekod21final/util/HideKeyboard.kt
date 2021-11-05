package com.mh.vezdekod21final.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import com.give.vezdekodmh.utils.L


object HideKeyboard {
    fun hideSoftKeyBoard(context: Context, view: View) {
        try {
            L.d("hideSoftKeyBoard start")
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            L.d("hideSoftKeyBoard end")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showKeyBoard(context: Context){
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun scrollToDown(scrollView: ScrollView){
        scrollView.postDelayed(Runnable { scrollView.fullScroll(ScrollView.FOCUS_DOWN) },200)
    }
}