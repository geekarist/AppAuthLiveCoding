package fr.xebia.appauthlc

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*

class MainActivity : AppCompatActivity(), FetchNameAsyncTask.Listener {

    private var authService: AuthorizationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authService = AuthorizationService(this)

        val authState = restoreSavedAuthState()

        if (true == authState?.isAuthorized) {
            performAction(authState, authService)
        } else if (authState?.lastAuthorizationResponse?.authorizationCode != null) {
            performTokenRequest(authState)
        } else if (isFirstLaunch()) {
            requestAuthCode()
        } else if (isAuthCodeResponse()) {
            handleAuthCodeResponse()
        } else {
            throw IllegalStateException("Unknown intent action: ${intent?.action}")
        }
    }

    private fun restoreSavedAuthState(): AuthState? {
        val manager = PreferenceManager.getDefaultSharedPreferences(this)
        val strAuthState = manager.getString(PREF_KEY_AUTH_STATE, null)
        return strAuthState?.let { AuthState.jsonDeserialize(it) }
    }

    private fun persistAuthState(state: AuthState) {
        PersistAuthStateAsyncTask(application, PREF_KEY_AUTH_STATE).execute(state)
    }

    private fun isAuthCodeResponse(): Boolean = intent?.action == ACTION_AUTH_CODE_RESPONSE

    private fun isFirstLaunch(): Boolean = intent?.action == Intent.ACTION_MAIN

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

        val completedIntent = Intent(this, javaClass)
                .setAction(ACTION_AUTH_CODE_RESPONSE)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                this,
                REQUEST_PERFORM_AUTH,
                completedIntent,
                0
        )
        authService?.performAuthorizationRequest(request, pendingIntent)
    }

    private fun handleAuthCodeResponse() {
        intent?.apply {

            val state = AuthState(
                    AuthorizationResponse.fromIntent(intent),
                    AuthorizationException.fromIntent(intent)
            )

            persistAuthState(state)

            performTokenRequest(state)
        }
    }

    private fun performTokenRequest(state: AuthState) {
        state.lastAuthorizationResponse?.createTokenExchangeRequest()?.let { tokenRequest ->
            authService?.performTokenRequest(tokenRequest) { response, ex ->
                state.update(response, ex)
                persistAuthState(state)
                if (ex != null) {
                    Toast.makeText(this@MainActivity, "Error: $ex.message", Toast.LENGTH_LONG)
                            .show()
                } else {
                    performAction(state, authService)
                }
            }
            Unit
        }
    }

    private fun performAction(state: AuthState, authService: AuthorizationService?) {
        authService?.let {
            state.performActionWithFreshTokens(it) { accessToken, _, _ ->
                FetchNameAsyncTask(this).execute(accessToken)
            }
        }
    }

    override fun onDisplayName(displayName: String) {
        runOnUiThread {
            mainHello.text = getString(R.string.main_hello_user, displayName)
        }
    }

    override fun onDestroy() {
        authService?.dispose()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_PERFORM_AUTH: Int = 42
        private const val ACTION_AUTH_CODE_RESPONSE = "ACTION_AUTH_CODE_RESPONSE"
        private const val PREF_KEY_AUTH_STATE = "PREF_KEY_AUTH_STATE"
    }
}
