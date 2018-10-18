package fr.xebia.appauthlc

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.openid.appauth.*

class MainActivity : AppCompatActivity() {

    private var authService: AuthorizationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authService = AuthorizationService(this)

        when (intent?.action) {
            Intent.ACTION_MAIN -> requestAuthCode()
            ACTION_AUTH_CODE_RESPONSE -> handleAuthCodeResponse()
            else -> throw IllegalStateException("Unknown intent action: ${intent?.action}")
        }
    }

    private fun requestAuthCode() {
        val authorizationEndpoint = Uri.parse(BuildConfig.GOOGLE_AUTH_ENDPOINT)
        val tokenEndpoint = Uri.parse(BuildConfig.GOOGLE_TOKEN_ENDPOINT)
        val clientId = BuildConfig.GOOGLE_CLIENT_ID
        val redirectUri = Uri.parse(BuildConfig.GOOGLE_REDIRECT_URI)

        val authConfig = AuthorizationServiceConfiguration(authorizationEndpoint, tokenEndpoint)

        val request = AuthorizationRequest.Builder(
                authConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        ).setScope("profile").build()

        val completedIntent = Intent(this, javaClass).setAction(ACTION_AUTH_CODE_RESPONSE)
        val pendingIntent = PendingIntent.getActivity(
                this,
                REQUEST_PERFORM_AUTH,
                completedIntent,
                0
        )
        authService?.performAuthorizationRequest(request, pendingIntent)
        finish()
    }

    private fun handleAuthCodeResponse() {
        intent?.apply {

            val state = AuthState(
                    AuthorizationResponse.fromIntent(intent),
                    AuthorizationException.fromIntent(intent)
            )

            state.lastAuthorizationResponse?.createTokenExchangeRequest()?.let { tokenRequest ->
                performTokenRequest(tokenRequest, state)
            }
        }
    }

    private fun performTokenRequest(tokenRequest: TokenRequest, state: AuthState) {
        authService?.performTokenRequest(tokenRequest) { response, ex ->
            state.update(response, ex)
            if (ex != null) {
                Toast.makeText(this@MainActivity, "Error: $ex.message", Toast.LENGTH_LONG)
                        .show()
            } else {
                performAction(state, authService)
            }
        }
    }

    private fun performAction(state: AuthState, authService: AuthorizationService?) {
        authService?.let {
            state.performActionWithFreshTokens(it) { accessToken, _, _ ->
                DisplayNameAsyncTask(application).execute(accessToken)
            }
        }
    }

    override fun onDestroy() {
        authService?.dispose()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_PERFORM_AUTH: Int = 42
        private const val ACTION_AUTH_CODE_RESPONSE = "ACTION_AUTH_CODE_RESPONSE"
    }
}
