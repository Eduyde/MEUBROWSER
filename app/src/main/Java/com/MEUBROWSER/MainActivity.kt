package com.meubrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private val home = "https://www.google.com"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun btn(icon: Int, action: () -> Unit) = ImageButton(this).apply {
            setImageResource(icon)
            setBackgroundColor(0)
            setOnClickListener { action() }
        }

        urlBar = EditText(this).apply {
            hint = "Pesquisar ou digitar endereço"
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_GO
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            setOnEditorActionListener { _, id, ev ->
                if (id == EditorInfo.IME_ACTION_GO || ev?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    load(text.toString()); hideKeyboard(); true
                } else false
            }
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8)
            addView(btn(android.R.drawable.ic_media_previous) { if (web.canGoBack()) web.goBack() })
            addView(btn(android.R.drawable.ic_media_next) { if (web.canGoForward()) web.goForward() })
            addView(urlBar, LinearLayout.LayoutParams(0, -2, 1f))
            addView(btn(android.R.drawable.ic_popup_sync) { web.reload() })
            addView(btn(android.R.drawable.ic_menu_myplaces) { web.loadUrl(home) })
        }

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100; visibility = View.GONE
        }

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(v: WebView, url: String, f: Bitmap?) {
                    urlBar.setText(url); progress.visibility = View.VISIBLE
                }
                override fun onPageFinished(v: WebView, url: String) {
                    progress.visibility = View.GONE
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(v: WebView, p: Int) { progress.progress = p }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            addView(bar)
            addView(progress, LinearLayout.LayoutParams(-1, 8))
            addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        }
        setContentView(root)

        load(intent?.data?.toString() ?: home)
    }

    private fun load(input: String) {
        val t = input.trim()
        val url = when {
            t.isEmpty() -> home
            t.startsWith("http://") || t.startsWith("https://") -> t
            t.contains(".") && !t.contains(" ") -> "https://$t"
            else -> "https://www.google.com/search?q=" + Uri.encode(t)
        }
        web.loadUrl(url)
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
