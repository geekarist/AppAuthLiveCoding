package fr.xebia.appauthlc

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.openid.appauth.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (intent?.action) {
            Intent.ACTION_MAIN -> requestAuth()
            ACTION_AUTH_CODE_RESPONSE -> {
                Toast.makeText(this, getAuthCode(intent), Toast.LENGTH_LONG).show()
            }
            else -> throw IllegalStateException("Unknown intent action: ${intent?.action}")
        }
    }

    private fun getAuthCode(intent: Intent?): String? {
        return intent?.let {
            AuthorizationException.fromIntent(it)?.let { e -> throw e }
            AuthorizationResponse.fromIntent(it)?.authorizationCode
        }
    }

    private fun requestAuth() {
        val authorizationEndpoint = Uri.parse(BuildConfig.GOOGLE_AUTH_ENDPOINT)
        val tokenEndpoint = Uri.parse(BuildConfig.GOOGLE_TOKEN_ENDPOINT)
        val clientId = BuildConfig.GOOGLE_CLIENT_ID
        val redirectUri = Uri.parse(BuildConfig.GOOGLE_REDIRECT_URI)

        val authConfig = AuthorizationServiceConfiguration(authorizationEndpoint, tokenEndpoint)
        val authorizationService = AuthorizationService(this)

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
        authorizationService.performAuthorizationRequest(request, pendingIntent)
        finish()
    }

    companion object {
        private const val REQUEST_PERFORM_AUTH: Int = 42
        private const val ACTION_AUTH_CODE_RESPONSE = "ACTION_AUTH_CODE_RESPONSE"
    }
}
