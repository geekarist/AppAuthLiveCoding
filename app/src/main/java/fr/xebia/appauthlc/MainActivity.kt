package fr.xebia.appauthlc

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity(), FetchNameAsyncTask.Listener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FetchNameAsyncTask(this).execute("TODO")
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
}
