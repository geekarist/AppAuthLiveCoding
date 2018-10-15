package appauthlc.xebia.fr.appauthlivecoding

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class MainActivity : AppCompatActivity() {

    private val ACTION_AUTH_CODE_RESPONSE = "ACTION_AUTH_CODE_RESPONSE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        when (intent?.action) {
            null -> requestAuth()
            ACTION_AUTH_CODE_RESPONSE -> {
                Toast.makeText(this, getAuthCode(intent), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAuthCode(intent: Intent?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun requestAuth() {
        val authorizationEndpoint = Uri.parse("TODO")
        val tokenEndpoint = Uri.parse("TODO")
        val clientId = "TODO"
        val redirectUri = Uri.parse("TODO")

        val authConfig = AuthorizationServiceConfiguration(authorizationEndpoint, tokenEndpoint)
        val authorizationService = AuthorizationService(this)

        val request = AuthorizationRequest.Builder(
                authConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        ).build()

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
    }
}
