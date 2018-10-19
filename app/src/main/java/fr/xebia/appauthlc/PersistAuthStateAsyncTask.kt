package fr.xebia.appauthlc

import android.app.Application
import android.os.AsyncTask
import android.preference.PreferenceManager
import net.openid.appauth.AuthState

class PersistAuthStateAsyncTask(
        private val app: Application,
        private val s: String
) : AsyncTask<AuthState, Unit, Unit>() {

    override fun doInBackground(vararg params: AuthState?) {
        val manager = PreferenceManager.getDefaultSharedPreferences(app)
        val strAuthState = params[0]?.jsonSerializeString()
        manager.edit().putString(s, strAuthState).apply()
    }
}
