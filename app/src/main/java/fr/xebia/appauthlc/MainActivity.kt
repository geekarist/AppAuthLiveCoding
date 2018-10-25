package fr.xebia.appauthlc

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity(), FetchNameAsyncTask.Listener {

    private val authorizationService by lazy { AuthorizationService(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (intent?.action) {
            Intent.ACTION_MAIN -> requestAuthCode()
            ACTION_RETURN_AUTH_CODE -> fetchName(intent)
        }
    }

    private fun requestAuthCode() {
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
        val intent = Intent(this, javaClass).setAction(ACTION_RETURN_AUTH_CODE)
        val completedIntent = PendingIntent.getActivity(this, 0, intent, 0)
        authorizationService.performAuthorizationRequest(request, completedIntent)
    }

    private fun fetchName(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        val authState = AuthState(response, error)

        response?.createTokenExchangeRequest()?.let { tokenRequest ->
            authorizationService.performTokenRequest(tokenRequest) { response, ex ->
                authState.update(response, ex)
                authState.performActionWithFreshTokens(authorizationService) { accessToken, _, _ ->
                    FetchNameAsyncTask(this).execute(accessToken)
                }
            }
        }
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

    companion object {
        private const val ACTION_RETURN_AUTH_CODE = "ACTION_RETURN_AUTH_CODE"
    }
}
