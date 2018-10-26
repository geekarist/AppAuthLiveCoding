package fr.xebia.appauthlc

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import net.openid.appauth.AuthState
import java.lang.ref.WeakReference

class RestoreAuthStateAsyncTask(
        context: Context,
        listener: Listener,
        private val key: String
) : AsyncTask<Unit, Unit, AuthState?>() {

    private val listenerRef = WeakReference(listener)
    private val safeListener
        get() = listenerRef.get()

    private val contextRef = WeakReference(context)
    private val safeContext
        get() = contextRef.get()

    override fun doInBackground(vararg params: Unit?): AuthState? {
        return safeContext?.let { context ->
            val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
            val strAuthState = prefManager.getString(key, null)
            strAuthState?.let { AuthState.jsonDeserialize(it) }
        }
    }

    override fun onPostExecute(result: AuthState?) {
        safeListener?.onAuthStateRestored(result)
    }

    interface Listener {
        fun onAuthStateRestored(result: AuthState?)
    }
}
