package appauthlc.xebia.fr.appauthlivecoding

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userName = "Unknown"
        mainHello.text = getString(R.string.main_hello_user, userName)
    }
}
