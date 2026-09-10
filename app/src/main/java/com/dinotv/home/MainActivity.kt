package com.dinotv.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/** A self-hosted console client. No JavaScript/native bridge or privileged remote permissions. */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var status: TextView
    private var home = ""
    private var failed = false
    private var chooser: ValueCallback<Array<Uri>>? = null
    private val prefs by lazy { getSharedPreferences("dino-home", MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        home = prefs.getString("home", "").orEmpty()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(23, 22, 20))
            setOnApplyWindowInsetsListener { view, insets ->
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                insets
            }
        }
        layout.addView(Button(this).apply { text = "Dino Home · Сервер"; setOnClickListener { configure() } })
        status = TextView(this).apply {
            setTextColor(Color.rgb(239, 230, 215))
            setPadding(24, 12, 24, 12)
            text = "Введите адрес своего сервера"
            setOnClickListener { if (home.isNotEmpty()) web.loadUrl(home) }
        }
        layout.addView(status)
        web = WebView(this).apply {
            setBackgroundColor(Color.rgb(23, 22, 20))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.mediaPlaybackRequiresUserGesture = true
            settings.userAgentString += " DinoHome/Android"
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val target = request.url.toString()
                if (HomeAddress.sameOrigin(home, target)) return false
                if (request.isForMainFrame && request.url.scheme == "https") {
                    try { startActivity(Intent(Intent.ACTION_VIEW, request.url).addCategory(Intent.CATEGORY_BROWSABLE)) }
                    catch (_: Exception) { status.text = "Не удалось открыть браузер" }
                }
                return true
            }
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                failed = false
                status.text = "Подключаюсь…"
            }
            override fun onPageFinished(view: WebView, url: String) { if (!failed) status.text = "" }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    failed = true
                    status.text = "Нет соединения. Нажмите здесь, чтобы повторить."
                }
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                chooser?.onReceiveValue(null)
                chooser = callback
                try {
                    @Suppress("DEPRECATION")
                    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE); type = "image/*"
                    }, 10)
                } catch (_: Exception) { chooser?.onReceiveValue(null); chooser = null }
                return true
            }
        }
        layout.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(layout)
        if (HomeAddress.normalize(home) != null) web.loadUrl(home) else configure()
    }

    private fun configure() {
        val field = EditText(this).apply {
            setSingleLine(); hint = "https://home.example.com"; setText(home)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        val dialog = AlertDialog.Builder(this).setTitle("Ваш сервер Dino TV")
            .setMessage("Укажите доверенный HTTPS-сервер. Для входа нужен одноразовый код из авторизованной консоли.")
            .setView(field).setNegativeButton("Отмена", null).setPositiveButton("Открыть", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val next = HomeAddress.normalize(field.text.toString())
                if (next == null) { field.error = "Нужен HTTPS-адрес без паролей и параметров"; return@setOnClickListener }
                if (next != home) {
                    web.stopLoading()
                    WebStorage.getInstance().deleteAllData()
                    CookieManager.getInstance().removeAllCookies(null)
                    web.clearHistory(); web.clearCache(true)
                }
                home = next
                prefs.edit().putString("home", home).apply()
                status.text = "Подключаюсь…"
                web.loadUrl(home)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @Deprecated("Legacy back callback is supported on API 26+")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else configure() }

    @Deprecated("Activity result compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            chooser?.onReceiveValue(if (resultCode == RESULT_OK) data?.data?.let { arrayOf(it) } else null)
            chooser = null
        }
    }

    override fun onDestroy() {
        chooser?.onReceiveValue(null); chooser = null
        web.stopLoading()
        (web.parent as? android.view.ViewGroup)?.removeView(web)
        web.webChromeClient = null
        web.removeAllViews()
        web.destroy()
        super.onDestroy()
    }
}
