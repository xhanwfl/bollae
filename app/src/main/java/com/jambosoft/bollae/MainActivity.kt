package com.jambosoft.bollae

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.*
import android.webkit.*
import android.widget.ProgressBar

class MainActivity : AppCompatActivity() {
    private lateinit var webView : WebView
    private lateinit var mProgressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById<WebView>(R.id.webView)
        mProgressBar = findViewById(R.id.progress1)

        webView.apply{
            webViewClient = MyWebViewClient() // 클릭시 새창 안뜨게

            //팝업이나 파일 업로드 등 설정해주기 위해 webView.webChromeClient를 설정
            //웹뷰에서 크롬이 실행가능&&새창띄우기는 안됨
            //webChromeClient = WebChromeClient()

            //웹뷰에서 팝업창 호출하기 위해
            webChromeClient = object : WebChromeClient(){
                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                    val newWebView = WebView(this@MainActivity).apply{
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val dialog = Dialog(this@MainActivity).apply{
                        setContentView(newWebView)
                        window!!.attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
                        window!!.attributes.height = ViewGroup.LayoutParams.MATCH_PARENT
                        show()
                    }

                    newWebView.webChromeClient = object : WebChromeClient(){
                        override fun onCloseWindow(window: WebView?) {
                            dialog.dismiss()
                        }
                    }

                    (resultMsg?.obj as WebView.WebViewTransport).webView = newWebView
                    resultMsg.sendToTarget()
                    return true
                }
            }
            settings.apply {
                javaScriptEnabled = true
                setSupportMultipleWindows(true) //새창띄우기 허용
                javaScriptCanOpenWindowsAutomatically = true //자바스크립트 새창띄우기 (멀티뷰) 허용
                loadWithOverviewMode = true //메타태크 허용
                useWideViewPort = true //화면사이즈 맞추기 허용
                setSupportZoom(true) //화면 줌 허용
                builtInZoomControls = true //화면 확대 축소 허용

                setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK) //캐시 허용
                setAppCacheEnabled(true)

                //cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용안함
                domStorageEnabled = true //로컬저장소 허용
                displayZoomControls = true

                setRenderPriority(WebSettings.RenderPriority.HIGH)
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                setEnableSmoothTransition(true)


                safeBrowsingEnabled = true
                mediaPlaybackRequiresUserGesture = false
                allowUniversalAccessFromFileURLs = true
                allowFileAccess = true
                loadsImagesAutomatically = true
            }
            fitsSystemWindows = true
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        val url = "https://m.bollae.net/"
        webView.loadUrl(url)
    }


    override fun onBackPressed() {
        if(webView!!.canGoBack()){
            webView!!.goBack()
        }else{
            super.onBackPressed()
        }
    }

    inner class MyWebViewClient : WebViewClient(){
        override fun shouldOverrideUrlLoading( view: WebView, url : String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mProgressBar.visibility = ProgressBar.VISIBLE
            webView.visibility = View.INVISIBLE
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            mProgressBar.visibility = ProgressBar.GONE
            webView.visibility = View.VISIBLE
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            var builder : android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(this@MainActivity)
            var message = "SSL Certificate error."
            when(error?.primaryError){
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
            }
            message += " Do you wnat to continue anyway?"
            builder.setTitle("SSL Certificate Error")
            builder.setMessage(message)
            builder.setPositiveButton("continue",
                DialogInterface.OnClickListener{ _, _ -> handler?.proceed()})
            builder.setNegativeButton("cancel",
                DialogInterface.OnClickListener{ dialog, which -> handler?.cancel()})
            val dialog : android.app.AlertDialog? = builder.create()
            dialog?.show()
        }
    }


}