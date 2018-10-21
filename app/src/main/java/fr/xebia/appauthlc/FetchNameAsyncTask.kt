package fr.xebia.appauthlc

import android.os.AsyncTask
import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.URL

class FetchNameAsyncTask(l: Listener) : AsyncTask<String, Unit, Unit>() {

    private val listenerRef = WeakReference(l)
    private val listener
        get() = listenerRef.get()

    override fun doInBackground(vararg params: String?) {
        val accessToken = params[0]
        val strUrl = "https://people.googleapis.com/v1/people/me?personFields=names&key=${BuildConfig.GOOGLE_API_KEY}"
        val connection = URL(strUrl).openConnection()
        connection.addRequestProperty("Authorization", "Bearer $accessToken")
        val reader = InputStreamReader(connection.getInputStream())
        val strResponse = reader.readText()
        val jsonObject = JSONObject(strResponse)
        val names = jsonObject.getJSONArray("names")
        val nameObj = names.get(0) as JSONObject
        val displayName = nameObj.getString("displayName")
        listener?.onDisplayName(displayName)
    }

    interface Listener {
        fun onDisplayName(displayName: String)
    }
}
