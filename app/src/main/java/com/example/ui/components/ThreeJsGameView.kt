package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
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
            setBackgroundColor(Color.parseColor("#0F141B"))
            overScrollMode = WebView.OVER_SCROLL_NEVER
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
            }
            webViewClient = WebViewClient()
            addJavascriptInterface(WildcraftJsBridge(viewModel), "AndroidBridge")
            loadUrl("file:///android_asset/game/index.html")
        }
    }

    LaunchedEffect(webView) {
        onWebViewReady(webView)
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
