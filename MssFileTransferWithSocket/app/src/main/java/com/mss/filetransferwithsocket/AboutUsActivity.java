package com.mss.filetransferwithsocket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * Created by hp on 30/11/16.
 */
public class AboutUsActivity extends AppCompatActivity {
    Toolbar toolbar;
    public ProgressDialog pd;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        webView=(WebView)findViewById(R.id.web_about);
        toolbar = (Toolbar) findViewById(R.id.custum_bar);
        TextView toolbarTitle = (TextView) findViewById(R.id.txt_title);
        toolbarTitle.setText("About Us");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pd = new ProgressDialog(AboutUsActivity.this);
        pd.setMessage("loading...");
        webView.setWebViewClient(new myWebClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("http://www.mastersoftwaresolutions.com/");
        webView.getSettings().setBuiltInZoomControls(true);
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            pd.show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            pd.dismiss();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(AboutUsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
