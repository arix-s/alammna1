package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
    )
    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        AlammnaPanelWebHost()
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AlammnaPanelWebHost() {
  val context = LocalContext.current
  val activity = context as? Activity
  val webViewRef = remember { mutableStateOf<WebView?>(null) }

  BackHandler {
    val view = webViewRef.value
    if (view != null) {
      view.evaluateJavascript(
        "(function(){ return !!(window.__alammnaHandleBack && window.__alammnaHandleBack()); })()"
      ) { result ->
        if (result != "true") {
          activity?.finish()
        }
      }
    } else {
      activity?.finish()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webViewRef.value?.apply {
        clearHistory()
        clearCache(true)
        destroy()
      }
      webViewRef.value = null
    }
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color(0xFF090D14))
        .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    AndroidView(
      modifier = Modifier.fillMaxSize().testTag("alammna_panel_webview"),
      factory = { ctx ->
        CookieManager.getInstance().setAcceptCookie(false)
        WebView(ctx).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )
          setBackgroundColor(AndroidColor.parseColor("#090D14"))
          settings.javaScriptEnabled = true
          settings.domStorageEnabled = false
          settings.databaseEnabled = false
          settings.allowFileAccess = true
          settings.cacheMode = WebSettings.LOAD_NO_CACHE
          settings.useWideViewPort = true
          settings.loadWithOverviewMode = true
          webViewClient = WebViewClient()
          webChromeClient = WebChromeClient()
          loadUrl("file:///android_asset/index.html")
          webViewRef.value = this
        }
      },
    )
  }
}
