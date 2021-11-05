package com.mh.vezdekod21final.util

import android.content.Context
import androidx.preference.PreferenceManager

object MySP {

    fun setStringValue(c: Context, text: String, constName : String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        with(sp.edit()) {
            putString(constName, text)
                .apply()
        }
    }

    fun getStringValue(c: Context, constName: String): String {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return sp.getString(constName, "").toString()
    }

    fun setIntValue(c: Context, int: Int, constName : String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        with(sp.edit()) {
            putInt(constName, int)
                .apply()
        }
    }

    fun getIntValue(c: Context, constName: String): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return sp.getInt(constName, 0)
    }
    fun setLongValue(c: Context, long: Long, constName : String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        with(sp.edit()) {
            putLong(constName, long)
                .apply()
        }
    }

    fun getLongValue(c: Context, constName: String): Long {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return sp.getLong(constName, 0)
    }



    fun getBooleanValue(c: Context, const: String): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        return sp.getBoolean(const, false)
    }

    fun setBooleanValue(c: Context, const: String, value: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(c)
        with(sp.edit()){
            putBoolean(const, value)
                .apply()
        }
    }



}