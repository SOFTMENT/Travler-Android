package in.softment.travler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class OpenPdfActivity extends AppCompatActivity {

    private WebView myWebView;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pdf);

        String pdfLink = getIntent().getStringExtra("pdfLink");
        String title = getIntent().getStringExtra("title");
        TextView headtitle = findViewById(R.id.title);
        headtitle.setText(title);
        myWebView = (WebView) findViewById(R.id.webpage);
        myWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                webView.loadUrl(url);
                frameLayout.setVisibility(View.VISIBLE);

                return false;
            }

        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                setTitle("Loading...");
                if (progress == 100) {
                    frameLayout.setVisibility(View.GONE);
                    setTitle(webView.getTitle());

                    //   lottieAnimationView.setVisibility(View.GONE);


                }
                super.onProgressChanged(webView, progress);
            }
        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setAllowContentAccess(true);
        myWebView.setScrollbarFadingEnabled(false);
        myWebView.setScrollbarFadingEnabled(false);
        frameLayout = findViewById(R.id.frame);
        progressBar = findViewById(R.id.progressbar);

        try {
            String url= URLEncoder.encode(pdfLink,"UTF-8");
            myWebView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
            progressBar.setMax(100);
            progressBar.setProgress(0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}