package edu.temple.basicbrowser

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BrowserScreen()
            }
        }
    }
}

/**
 * Makes sure the URL has a protocol (scheme). If the user typed something like
 * "wikipedia.com", it becomes "https://wikipedia.com". URLs that already have a
 * scheme (http://, https://, file://, etc.) are returned unchanged.
 */
fun ensureScheme(input: String): String {
    val url = input.trim()
    if (url.isEmpty()) return url
    val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(url)
    return if (hasScheme) url else "https://$url"
}

@Composable
fun BrowserScreen() {
    // Text currently typed in the TextField
    var urlText by rememberSaveable { mutableStateOf("") }
    // URL the WebView should load (only changes when the user presses the button)
    var urlToLoad by rememberSaveable { mutableStateOf("") }

    val go = {
        val fixed = ensureScheme(urlText)
        if (fixed.isNotEmpty()) {
            urlText = fixed      // show the corrected URL in the TextField
            urlToLoad = fixed
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            // Top row: URL TextField + Go button (same layout as the XML version)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Enter URL") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { go() })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = go) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Go"
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            // WebView has no Compose equivalent, so wrap the legacy View in AndroidView
            BrowserWebView(
                url = urlToLoad,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BrowserWebView(url: String, modifier: Modifier = Modifier) {
    // Remember which URL we last loaded so recompositions don't reload the page
    val lastLoaded = remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Keep link clicks inside our WebView instead of opening the system browser
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            if (url.isNotEmpty() && url != lastLoaded.value) {
                lastLoaded.value = url
                webView.loadUrl(url)
            }
        }
    )
}
