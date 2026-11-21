package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.GameViewModel

private const val TAG = "ThreeJsGameView"
private const val GAME_URL = "file:///android_asset/game/index.html"

private class WildcraftJsBridge(
    private val viewModel: GameViewModel
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onEngineReady() {
        mainHandler.post {
            viewModel.onEngineReady()
        }
    }

    @JavascriptInterface
    fun syncState(json: String) {
        mainHandler.post {
            viewModel.syncFromEngine(json)
        }
    }

    @JavascriptInterface
    fun triggerHaptic(type: String) {
        mainHandler.post {
            viewModel.triggerHaptic(type)
        }
    }

    @JavascriptInterface
    fun onBiomeChanged(biomeId: String) {
        mainHandler.post {
            viewModel.onBiomeChanged(biomeId)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeJsGameView(
    viewModel: GameViewModel,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Keep default layer type so Chromium's hardware WebGL canvas composites directly in Compose
            setBackgroundColor(Color.parseColor("#5BB3E0"))
            overScrollMode = WebView.OVER_SCROLL_NEVER
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = false
                loadWithOverviewMode = false
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(
                            TAG,
                            "JS [${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})"
                        )
                    }
                    return super.onConsoleMessage(consoleMessage)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "WebView error on ${request?.url}: ${error?.description}")
                }
            }

            addJavascriptInterface(WildcraftJsBridge(viewModel), "AndroidBridge")
        }
    }

    LaunchedEffect(webView) {
        onWebViewReady(webView)
        if (webView.url.isNullOrBlank()) {
            if (webView.width > 0 && webView.height > 0) {
                webView.loadUrl(GAME_URL)
            } else {
                val listener = object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View?,
                        left: Int,
                        top: Int,
                        right: Int,
                        bottom: Int,
                        oldLeft: Int,
                        oldTop: Int,
                        oldRight: Int,
                        oldBottom: Int
                    ) {
                        if (right - left > 0 && bottom - top > 0) {
                            webView.removeOnLayoutChangeListener(this)
                            if (webView.url.isNullOrBlank()) {
                                webView.loadUrl(GAME_URL)
                            }
                        }
                    }
                }
                webView.addOnLayoutChangeListener(listener)
                webView.post {
                    if (webView.url.isNullOrBlank()) {
                        webView.removeOnLayoutChangeListener(listener)
                        webView.loadUrl(GAME_URL)
                    }
                }
            }
        }
        viewModel.jsCommands.collect { script ->
            webView.evaluateJavascript(script, null)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.removeJavascriptInterface("AndroidBridge")
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.testTag("threejs_game_canvas")
    )
}
