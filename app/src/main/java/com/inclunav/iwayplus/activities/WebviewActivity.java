package com.inclunav.iwayplus.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.inclunav.iwayplus.R;

public class WebviewActivity extends AppCompatActivity {

    Bundle extras;
    String loadurl;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        extras=getIntent().getExtras();
        if(extras!=null){
            loadurl=extras.getString("URL",getApplicationContext().getResources().getString(R.string.project_page));
        }

        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setInitialScale(1);


        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar progressBar =  findViewById(R.id.progressBar);
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);

                } else {
                    progressBar.setVisibility(View.VISIBLE);

                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
        });

        registerForContextMenu(webView);
        webView.loadUrl(loadurl);
    }

    public void goBack(View view) {
        onBackPressed();
    }
}


