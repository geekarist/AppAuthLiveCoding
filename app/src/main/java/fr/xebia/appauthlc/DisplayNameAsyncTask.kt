package fr.xebia.appauthlc

import android.app.Application
import android.os.AsyncTask
import android.widget.Toast
import java.io.InputStreamReader
import java.net.URL

class DisplayNameAsyncTask(private val app: Application) : AsyncTask<String, Unit, Unit>() {

    override fun doInBackground(vararg params: String?) {
        val accessToken = params[0]
        val strUrl = "https://people.googleapis.com/v1/people/me?personFields=names&key=${BuildConfig.GOOGLE_API_KEY}"
        val connection = URL(strUrl).openConnection()
        connection.addRequestProperty("Authorization", "Bearer $accessToken")
        val reader = InputStreamReader(connection.getInputStream())
        val strResponse = reader.readText()
        Toast.makeText(app, "Response: $strResponse", Toast.LENGTH_LONG).show()
    }
}
