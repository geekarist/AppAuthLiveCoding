package fr.xebia.appauthlc

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import net.openid.appauth.AuthState
import java.lang.ref.WeakReference

class PersistAuthStateAsyncTask(context: Context, private val key: String) : AsyncTask<AuthState, Unit, Unit>() {

    private val contextRef = WeakReference(context)
    private val safeContext
        get() = contextRef.get()

    override fun doInBackground(vararg params: AuthState?) {
        params[0]?.let { state ->
            val strState = state.jsonSerializeString()
            safeContext?.let {
                val prefManager = PreferenceManager.getDefaultSharedPreferences(safeContext)
                prefManager.edit().putString(key, strState).apply()
            }
        }
    }
}
