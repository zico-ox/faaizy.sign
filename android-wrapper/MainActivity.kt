package com.faaizy.billex

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.OutputStream
import java.util.UUID

/**
 * Minimal Android Print Wrapper for EXEO EX58P & Web Billing Apps
 * Injects `window.AndroidPrinter` into the WebView for direct 1-tap Bluetooth SPP printing.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    // Standard Bluetooth Serial Port Profile (SPP) RFCOMM UUID:
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request Bluetooth permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                    101
                )
            }
        }

        webView = findViewById(R.id.webView)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true

        // Inject the JavaScript Interface: window.AndroidPrinter
        webView.addJavascriptInterface(PrinterBridge(), "AndroidPrinter")
        webView.webViewClient = WebViewClient()

        // Point this to your online billing URL, local server, or local asset:
        // Example online: webView.loadUrl("https://your-shop-billing.web.app/billex.html")
        // Example local:  webView.loadUrl("file:///android_asset/billex.html")
        webView.loadUrl("file:///android_asset/billex.html")
    }

    inner class PrinterBridge {
        /**
         * Called directly by JS: window.AndroidPrinter.print(base64EscPosData)
         */
        @JavascriptInterface
        fun print(base64Data: String) {
            Thread {
                try {
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val btAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (btAdapter == null) {
                        showToast("Bluetooth is not available on this device")
                        return@Thread
                    }

                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        // Find paired EXEO printer (look for EX58, MPT, POS, or choose first paired device)
                        val bondedDevices: Set<BluetoothDevice> = btAdapter.bondedDevices
                        val printer = bondedDevices.firstOrNull { 
                            val name = it.name?.uppercase() ?: ""
                            name.contains("EX58") || name.contains("MPT") || name.contains("POS") || name.contains("PRINTER")
                        } ?: bondedDevices.firstOrNull()

                        if (printer == null) {
                            showToast("No paired printer found! Please pair EXEO EX58P in Android Bluetooth Settings first.")
                            return@Thread
                        }

                        // Open direct RFCOMM socket (Bluetooth Classic SPP)
                        val socket: BluetoothSocket = printer.createRfcommSocketToServiceRecord(SPP_UUID)
                        socket.connect()
                        val out: OutputStream = socket.outputStream
                        out.write(bytes)
                        out.flush()
                        
                        // Small delay to ensure all bytes stream through before closing socket
                        Thread.sleep(200)
                        socket.close()

                        showToast("Bill printed to ${printer.name ?: "EXEO EX58P"}!")
                    }
                } catch (e: Exception) {
                    showToast("Print error: ${e.message}")
                }
            }.start()
        }

        private fun showToast(msg: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
