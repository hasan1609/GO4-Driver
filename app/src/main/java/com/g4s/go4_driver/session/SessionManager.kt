package com.g4s.go4_driver.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {
    val privateMode = 0
    val privateName ="login"
    var Pref : SharedPreferences?= context.getSharedPreferences(privateName,privateMode)
    var editor : SharedPreferences.Editor?=Pref?.edit()

    private val islogin = "login"
    fun setLogin(check: Boolean){
        editor?.putBoolean(islogin,check)
        editor?.commit()
    }

    fun getLogin():Boolean?
    {
        return Pref?.getBoolean(islogin,false)
    }


    private val isToken = "isToken"
    fun setToken(check: String){
        editor?.putString(isToken,check)
        editor?.commit()
    }

    fun getToken():String?
    {
        return Pref?.getString(isToken,"")
    }

    private val isId = "isId"
    fun setId(check: String){
        editor?.putString(isId,check)
        editor?.commit()
    }

    fun getId():String?
    {
        return Pref?.getString(isId,"")
    }

    private val isNamaDriver = "isNamaDriver"
    fun setNamaDriver(check: String){
        editor?.putString(isNamaDriver,check)
        editor?.commit()
    }

    fun getNamaDriver():String?
    {
        return Pref?.getString(isNamaDriver,"")
    }

    private val isFcm = "isFcm"
    fun setFcm(check: String){
        editor?.putString(isFcm,check)
        editor?.commit()
    }

    fun getFcm():String?
    {
        return Pref?.getString(isFcm,"")
    }

    private val isType = "isType"
    fun setType(check: String){
        editor?.putString(isType,check)
        editor?.commit()
    }

    fun getType():String?
    {
        return Pref?.getString(isType,"")
    }

    private val isFoto = "isFoto"
    fun setFoto(check: String){
        editor?.putString(isFoto,check)
        editor?.commit()
    }

    fun getFoto():String?
    {
        return Pref?.getString(isFoto,"")
    }

    fun clearSession() {
        val editor = Pref?.edit()
        editor?.clear()
        editor?.apply()
    }

}