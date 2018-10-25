package fr.xebia.appauthlc

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity(), FetchNameAsyncTask.Listener {

    private val authorizationService by lazy { AuthorizationService(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = AuthorizationServiceConfiguration(
                Uri.parse(BuildConfig.GOOGLE_AUTH_ENDPOINT),
                Uri.parse(BuildConfig.GOOGLE_TOKEN_ENDPOINT)
        )
        val request = AuthorizationRequest.Builder(
                config,
                BuildConfig.GOOGLE_CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(BuildConfig.GOOGLE_REDIRECT_URI)
        ).setScope("profile").build()
        val completedIntent = PendingIntent.getActivity(this, 0, Intent(), 0)
        authorizationService.performAuthorizationRequest(request, completedIntent)

//        val authToken = "TODO"
//        FetchNameAsyncTask(this).execute(authToken)
    }

    override fun onFetchNameSuccess(displayName: String) {
        mainHello.text = getString(R.string.main_hello_user, displayName)
    }

    override fun onFetchNameError(e: Exception) {
        mainHello.text = getString(R.string.main_hello_user, "unknown user")
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        Log.e(javaClass.simpleName, "Error fetching name: $writer")
    }

    override fun onDestroy() {
        authorizationService.dispose()
        super.onDestroy()
    }
}
