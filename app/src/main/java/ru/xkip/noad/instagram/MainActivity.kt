package ru.xkip.noad.instagram

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setContentView(R.layout.activity_main)

		var pInfo = ""
		try {
			pInfo = packageManager.getPackageInfo(packageName, 0).versionName
		} catch (e: Throwable) {
			e.printStackTrace()
		}

		val res = resources
		val status = (res.getString(R.string.app_name)
				+ " v"
				+ pInfo
				+ " "
				+ if (XChecker.isEnabled())
			res.getString(R.string.module_active)
		else
			res
				.getString(R.string.module_inactive))
		val tvStatus = findViewById(R.id.moduleStatus) as TextView
		tvStatus.text = status
		tvStatus.setTextColor(if (XChecker.isEnabled()) Color.GREEN else Color.RED)

		Log.i("NOAD", "setOnClickListener2")

		findViewById<View>(R.id.btnOK).setOnClickListener(View.OnClickListener { finish() })

	}

}
