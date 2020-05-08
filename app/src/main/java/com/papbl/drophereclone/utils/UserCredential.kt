package com.papbl.drophereclone.utils

import android.content.Context
import android.content.SharedPreferences
import com.papbl.drophereclone.models.Credential

class UserCredential {

    companion object {
        const val KEY_UID_CREDENTIAL = "uid"
        const val KEY_EMAIL_CREDENTIAL = "email"
        const val KEY_ON_BOARDING_VIEWED = "onboarding"
    }

    private fun getSharedPreference(context: Context): SharedPreferences? {
        return context.getSharedPreferences(
            context.applicationInfo.toString(),
            Context.MODE_PRIVATE
        )
    }

    fun setOnBoardingViewed(context: Context) {
        val editor: SharedPreferences.Editor = getSharedPreference(context)!!.edit()
        editor.putBoolean(KEY_ON_BOARDING_VIEWED, true)
        editor.apply()
    }

    fun getOnBoardingViewed(context: Context): Boolean {
        return getSharedPreference(context)!!.getBoolean(KEY_ON_BOARDING_VIEWED, false)
    }

    fun setLoggedUser(context: Context, userCredential: Credential) {
        val editor: SharedPreferences.Editor = getSharedPreference(context)!!.edit()
        editor.putString(KEY_EMAIL_CREDENTIAL, userCredential.email)
        editor.putString(KEY_UID_CREDENTIAL, userCredential.uid)
        editor.apply()
    }

    fun getLoggedUser(context: Context): Credential {
        val email = getSharedPreference(context)!!.getString(KEY_EMAIL_CREDENTIAL, "")
        val uid = getSharedPreference(context)!!.getString(KEY_UID_CREDENTIAL, "")
        return Credential(email!!, uid!!)
    }

    fun clearLoggedInUser(context: Context) {
        val editor: SharedPreferences.Editor = getSharedPreference(context)!!.edit()
        editor.remove(KEY_UID_CREDENTIAL)
        editor.remove(KEY_EMAIL_CREDENTIAL)
        editor.apply()
    }

}