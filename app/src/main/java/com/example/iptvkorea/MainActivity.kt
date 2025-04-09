/* MainActivity.kt */
package com.example.iptvkorea

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelList = parseM3U()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChannelListScreen(channelList)
                }
            }
        }
    }

    private fun parseM3U(): List<Pair<String, String>> {
        val channels = mutableListOf<Pair<String, String>>()
        try {
            val inputStream = assets.open("ai티비.m3u")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var name: String? = null
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    if (it.startsWith("#EXTINF")) {
                        name = it.substringAfter(",")
                    } else if (name != null && it.startsWith("http")) {
                        channels.add(Pair(name!!, it))
                        name = null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }
}

@Composable
fun ChannelListScreen(channels: List<Pair<String, String>>) {
    var selectedUrl by remember { mutableStateOf<String?>(null) }

    if (selectedUrl != null) {
        PlayerScreen(url = selectedUrl!!) {
            selectedUrl = null
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(channels) { channel ->
                Text(
                    text = channel.first,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUrl = channel.second }
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun PlayerScreen(url: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            PlayerView(it).apply {
                this.player = player
            }
        }, modifier = Modifier.weight(1f))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("채널 목록으로 돌아가기")
        }
    }
}
