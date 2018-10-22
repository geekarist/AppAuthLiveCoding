package fr.xebia.appauthlc

import android.os.AsyncTask
import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.URL

class FetchNameAsyncTask(l: Listener) : AsyncTask<String, Unit, FetchNameAsyncTask.Response>() {

    private val listenerRef = WeakReference(l)
    private val listener
        get() = listenerRef.get()

    override fun doInBackground(vararg params: String?): Response {
        return try {
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
            Response.Success(displayName)
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    sealed class Response {
        data class Success(val displayName: String) : Response()
        data class Error(val error: Exception) : Response()
    }

    override fun onPostExecute(result: Response?) {
        when (result) {
            is Response.Success -> listener?.onFetchNameSuccess(result.displayName)
            is Response.Error -> listener?.onFetchNameError(result.error)
        }
    }

    interface Listener {
        fun onFetchNameSuccess(displayName: String)
        fun onFetchNameError(e: Exception)
    }
}
