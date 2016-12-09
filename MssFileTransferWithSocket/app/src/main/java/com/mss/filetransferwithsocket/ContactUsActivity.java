package com.mss.filetransferwithsocket;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by hp on 30/11/16.
 */

public class ContactUsActivity extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        toolbar = (Toolbar) findViewById(R.id.custum_bar);
        TextView txtGmail = (TextView) findViewById(R.id.txt_gmail);
        TextView txtWeb = (TextView) findViewById(R.id.txt_webview);
        final WebView webView = (WebView) findViewById(R.id.webview);
        TextView txtPhoneNumber = (TextView) findViewById(R.id.txt_phone_number);
        TextView toolbarTitle = (TextView) findViewById(R.id.txt_title);
        toolbarTitle.setText("Contact Us");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setType("plain/text");
                sendIntent.setData(Uri.parse("info@mastersoftwaresolutions.com"));
                sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                startActivity(sendIntent);
            }
        });
        txtPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSetup();
            }
        });
        txtWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   webView.loadUrl(" https://www.google.co.in/maps/place/Master+Software+Solutions/@30.660687,76.8584843,17z/data=!3m1!4b1!4m5!3m4!1s0x390f9492acc68683:0x58224e9f10467f52!8m2!3d30.660687!4d76.860673?hl=en");
                webView.getSettings().setJavaScriptEnabled(false);*/
                startActivity(new Intent(ContactUsActivity.this, MapsActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ContactUsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void dialogSetup() {
        final Dialog dialog = new Dialog(ContactUsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog);
        TextView dialpadFirst = (TextView) dialog.findViewById(R.id.dialpad_number_first);
        TextView dialpadSecond = (TextView) dialog.findViewById(R.id.dialpad_number_second);
        dialpadFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91-81959-00456"));
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialpadSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91-88720-13695"));
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
