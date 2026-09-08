package com.prashant.instasv

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var urlInput: EditText
    private lateinit var downloadBtn: Button
    private lateinit var pasteBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var previewImage: ImageView
    private var downloadId: Long = -1L
    private var mediaUrl: String = ""
    private var mediaType: String = "" // "image" or "video"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlInput = findViewById(R.id.urlInput)
        downloadBtn = findViewById(R.id.downloadBtn)
        pasteBtn = findViewById(R.id.pasteBtn)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        previewImage = findViewById(R.id.previewImage)

        // Handle intent if app opened from share
        handleIntent(intent)

        pasteBtn.setOnClickListener {
            pasteFromClipboard()
        }

        downloadBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                if (isValidInstagramUrl(url)) {
                    downloadMedia(url)
                } else {
                    Toast.makeText(this, "Invalid Instagram URL", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter URL or paste", Toast.LENGTH_SHORT).show()
            }
        }

        registerDownloadReceiver()
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null && isValidInstagramUrl(sharedText)) {
                urlInput.setText(sharedText)
                downloadMedia(sharedText)
            }
        }
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text.toString()
            if (isValidInstagramUrl(text)) {
                urlInput.setText(text)
                Toast.makeText(this, "URL pasted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Instagram URL in clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidInstagramUrl(url: String): Boolean {
        return url.contains("instagram.com") || url.contains("instagr.am")
    }

    private fun downloadMedia(url: String) {
        progressBar.visibility = ProgressBar.VISIBLE
        statusText.text = "Fetching media..."
        downloadBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                val mediaInfo = fetchMediaUrl(url)
                if (mediaInfo != null) {
                    mediaUrl = mediaInfo.first
                    mediaType = mediaInfo.second

                    // Show preview if image
                    if (mediaType == "image") {
                        withContext(Dispatchers.Main) {
                            Glide.with(this@MainActivity)
                                .load(mediaUrl)
                                .into(previewImage)
                        }
                    }

                    startDownload(mediaUrl, mediaType)
                } else {
                    withContext(Dispatchers.Main) {
                        statusText.text = "Failed to fetch media"
                        progressBar.visibility = ProgressBar.GONE
                        downloadBtn.isEnabled = true
                        Toast.makeText(this@MainActivity, "Could not extract media URL", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Error: ${e.message}"
                    progressBar.visibility = ProgressBar.GONE
                    downloadBtn.isEnabled = true
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchMediaUrl(url: String): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                val html = response.body?.string() ?: return@withContext null

                // Extract media URL from HTML
                val imagePattern = "\"og:image\"\\s*content=\"([^\"]+)\"".toRegex()
                val videoPattern = "\"og:video\"\\s*content=\"([^\"]+)\"".toRegex()

                val videoMatch = videoPattern.find(html)
                if (videoMatch != null) {
                    return@withContext Pair(videoMatch.groupValues[1], "video")
                }

                val imageMatch = imagePattern.find(html)
                if (imageMatch != null) {
                    return@withContext Pair(imageMatch.groupValues[1], "image")
                }

                null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun startDownload(url: String, type: String) {
        try {
            val fileName = if (type == "image") {
                "InstaSV_${System.currentTimeMillis()}.jpg"
            } else {
                "InstaSV_${System.currentTimeMillis()}.mp4"
            }

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
                .setTitle("Downloading from Instagram")
                .setDescription("Saving $type...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)

            downloadId = downloadManager.enqueue(request)

            runOnUiThread {
                statusText.text = "Downloading $fileName..."
            }
        } catch (e: Exception) {
            runOnUiThread {
                statusText.text = "Download error: ${e.message}"
                progressBar.visibility = ProgressBar.GONE
                downloadBtn.isEnabled = true
                Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerDownloadReceiver() {
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id == downloadId) {
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor? = dm.query(DownloadManager.Query().setFilterById(downloadId))
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndex >= 0) {
                            val status = cursor.getInt(columnIndex)
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    statusText.text = "✓ Download completed!"
                                    progressBar.visibility = ProgressBar.GONE
                                    downloadBtn.isEnabled = true
                                    Toast.makeText(context, "Downloaded successfully!", Toast.LENGTH_LONG).show()
                                    urlInput.setText("")
                                    previewImage.setImageResource(android.R.color.transparent)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    statusText.text = "✗ Download failed"
                                    progressBar.visibility = ProgressBar.GONE
                                    downloadBtn.isEnabled = true
                                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        cursor.close()
                    }
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, filter)
        }
    }
}
