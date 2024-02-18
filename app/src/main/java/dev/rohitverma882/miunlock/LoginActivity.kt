package dev.rohitverma882.miunlock

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.webkit.CookieManagerCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature

import com.google.android.material.progressindicator.LinearProgressIndicator

import dev.rohitverma882.miunlock.databinding.ActivityLoginBinding

import java.util.Collections
import java.util.UUID

@SuppressLint("SetJavaScriptEnabled")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val webView: WebView get() = binding.webView
    private val progressBar: LinearProgressIndicator get() = binding.progress

    private val cookieManager = CookieManager.getInstance()

    private var isLogout: Boolean = false
    private var passToken: String? = null
    private var userId: String? = null
    private var deviceId: String = "wb_${UUID.randomUUID()}"

    private var onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLogout = intent.getBooleanExtra("isLogout", false)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        if (isLogout) clearWebView()

        setupWebView()
    }

    private fun clearWebView() {
        ApplicationLoader.prefs.edit()
            .putString("user", "")
            .apply()

        cookieManager.removeSessionCookies(null)
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        webView.clearCache(false)
        webView.clearHistory()
        webView.clearFormData()
    }

    private fun setupWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        webView.setInitialScale(1)
        webView.loadUrl(LOGIN_URL)
        webView.webViewClient = object : WebViewClientCompat() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false

                scanCookies(url)
                checkLogin()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                if (request.isRedirect) {
                    view.loadUrl(request.url.toString())
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    private fun scanCookies(url: String?) {
        var cookies =
            cookieManager.getCookie(url ?: LOGIN_URL)?.split(";") ?: Collections.emptyList()

        if (cookies.isEmpty() && WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO)) {
            cookies = CookieManagerCompat.getCookieInfo(cookieManager, url ?: LOGIN_URL)
        }

        for (cookie in cookies) {
            if (cookie.contains(";")) {
                cookies = cookie.split(";")
                for (cookie2 in cookies) {
                    if (!extractCookies(cookie2)) continue
                }
            } else {
                if (!extractCookies(cookie)) continue
            }
        }
    }

    private fun extractCookies(cookie: String): Boolean {
        val p = cookie.split("=", ignoreCase = true, limit = 2)
        if (p.size < 2) return false
        val name = p[0].trim()
        val value = p[1].trim()
        when (name) {
            "passToken" -> {
                passToken = value
            }

            "userId" -> {
                userId = value
            }

            "deviceId" -> {
                deviceId = value
            }
        }
        return true
    }

    private fun checkLogin() {
        if (passToken.isNullOrEmpty() || userId.isNullOrEmpty() || isFinishing) return

        Intent(this, TerminalActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("user", User(passToken!!, userId!!, deviceId))
        }.also {
            startActivity(it)
            finish()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        super.onDestroy()
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val SERVICE_NAME = "unlockApi"
        private const val LOGIN_URL =
            "https://account.xiaomi.com/pass/serviceLogin?sid=${SERVICE_NAME}&json=false&passive=true&hidden=false&checkSafePhone=true&_locale=en"
    }
}
