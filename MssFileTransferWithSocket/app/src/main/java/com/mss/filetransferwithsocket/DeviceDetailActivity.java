package com.mss.filetransferwithsocket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mss.filetransferwithsocket.interfaces.OnDisConnect;
import com.mss.filetransferwithsocket.utils.AppController;
import com.mss.filetransferwithsocket.utils.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceDetailActivity extends Activity implements ConnectionInfoListener, View.OnClickListener, OnDisConnect {
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private WifiP2pDevice mDevice;
    public static ProgressDialog progressDialog = null;
    private static ProgressDialog mProgressDialog, mProgressDialogSend;
    public static String WiFiServerIp = "";
    public static String WiFiClientIp = "";
    static Boolean ClientCheck = false;
    public static String GroupOwnerAddress = "";
    static long actualFilelength = 0;
    static int percentage = 0;
    public static String folderName = "P2P";
    public static String AVAILABLE = "Available";
    public static String UNAVAILABLE = "Unavailable";
    public static String CONNECTED = "Connected";
    public static String INVITED = "Invited";
    public static String FAILED = "failed";
    public static String UNKNOWN = "Unknown";


    private WifiP2pInfo mInfo;
    private TextView txtFriendDeviceName, txtFriendDeviceStatus, txtHeader;
    private Button btnConnect, btnDisconnect, btnSendImage, btnSendVideo;
    ConnectionInfoListener connectionInfoListener;
    private WiFiDirectBroadcastReceiver mWifiDirectBroadcastreceiver;
    private boolean mIsWifiP2pEnabled = false;
    private boolean mRetryChannel = false;
    ImageView imgStatus;
    ImageView ibtnBack;
    private static boolean mSendDialog = false;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.mIsWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        initUi();
    }

    private void initUi() {
        AppController.screenData = true;
        mDevice = (WifiP2pDevice) getIntent().getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        txtFriendDeviceName = (TextView) findViewById(R.id.txt_detail_device_name);
        txtFriendDeviceStatus = (TextView) findViewById(R.id.txt_detail_device_available);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        btnSendImage = (Button) findViewById(R.id.btn_send_image);
        btnSendVideo = (Button) findViewById(R.id.btn_send_video);
        imgStatus = (ImageView) findViewById(R.id.img_status);
        txtHeader = (TextView) findViewById(R.id.toolbar_title);
        ibtnBack = (ImageView) findViewById(R.id.ibtn_back);
        Session.setDeviceActionListenerDisconnect(this);
        populateUi();
    }

    private void populateUi() {
        if (AppController.deviceDetailActivity == null)
            AppController.deviceDetailActivity = DeviceDetailActivity.this;
        btnConnect.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnSendVideo.setOnClickListener(this);
        btnSendImage.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        ibtnBack.setOnClickListener(this);
        ibtnBack.setImageResource(R.mipmap.back_icon);
        txtFriendDeviceName.setText(mDevice.deviceName);
        txtFriendDeviceStatus.setText(getDeviceStatus(mDevice.status));
        txtHeader.setText(mDevice.deviceName);
        if (getDeviceStatus(mDevice.status).equals(AVAILABLE)) {
            imgStatus.setImageResource(R.mipmap.available);
        }
        if (getDeviceStatus(mDevice.status).equals(UNAVAILABLE)) {
            imgStatus.setImageResource(R.mipmap.unavailable);
        }
        if (getDeviceStatus(mDevice.status).equals(CONNECTED)) {
            imgStatus.setImageResource(R.mipmap.connect);
        }
        if (getDeviceStatus(mDevice.status).equals(INVITED)) {
            imgStatus.setImageResource(R.mipmap.invite);
        }
        if (getDeviceStatus(mDevice.status).equals(FAILED)) {
            imgStatus.setImageResource(R.mipmap.fail);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mWifiDirectBroadcastreceiver = new WiFiDirectBroadcastReceiver(AppController.manager, AppController.channel, this, this);
            registerReceiver(mWifiDirectBroadcastreceiver, AppController.intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mWifiDirectBroadcastreceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String selectedfilePath = null;
            try {
                selectedfilePath = CommonMethods.getPath(uri, DeviceDetailActivity.this);
                Log.e(getResources().getString(R.string.txt_file_path), selectedfilePath);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            String Extension = "";
            if (selectedfilePath != null) {
                File f = new File(selectedfilePath);
                System.out.println(getResources().getString(R.string.txt_file_name) + f.getName());
                Long FileLength = f.length();
                actualFilelength = FileLength;
                try {
                    Extension = f.getName();
                    Log.e(getResources().getString(R.string.txt_extension), "" + Extension);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                CommonMethods.e("", getResources().getString(R.string.txt_path_null));
                return;
            }

            try {
                Intent serviceIntent = new Intent(DeviceDetailActivity.this, FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                String Ip = SharedPreferencesHandler.getStringValues(getApplicationContext(), getResources().getString(R.string.txt_wifi_client_ip));
                String OwnerIp = SharedPreferencesHandler.getStringValues(getApplicationContext(), getResources().getString(R.string.txt_grp_owner));
                if (OwnerIp != null && OwnerIp.length() > 0) {
                    CommonMethods.e("", getResources().getString(R.string.txt_inside_chk));
                    String host = null;
                    int sub_port = -1;
                    String ServerBool = SharedPreferencesHandler.getStringValues(getApplicationContext(), getResources().getString(R.string.txt_server_boolean));
                    if (ServerBool != null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {

                        //-----------------------------
                        if (Ip != null && !Ip.equals("")) {
                            CommonMethods.e(
                                    getResources().getString(R.string.txt_in_if_cond),
                                    "Sending data to " + Ip);
                            // Get Client Ip Address and send data
                            host = Ip;
                            sub_port = FileTransferService.PORT;
                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, Ip);
                        }
                    } else {
                        FileTransferService.PORT = 8888;
                        host = OwnerIp;
                        sub_port = FileTransferService.PORT;
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, OwnerIp);
                    }
                    serviceIntent.putExtra(FileTransferService.Extension, Extension);
                    serviceIntent.putExtra(FileTransferService.Filelength, actualFilelength + "");
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
                    if (host != null && sub_port != -1) {
                        CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
                        showprogress("Sending...", DeviceDetailActivity.this);
                        startService(serviceIntent);
                    } else {
                        CommonMethods.DisplayToast(getApplicationContext(), "Host Address not found, Please Re-Connect");
                        DismissProgressDialog();
                    }
                } else {
                    DismissProgressDialog();
                    CommonMethods.DisplayToast(getApplicationContext(), "Host Address not found,Please Re-Connect");
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        } else {
            CommonMethods.DisplayToast(getApplicationContext(), "Cancelled Request");
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.mInfo = wifiP2pInfo;
        txtFriendDeviceStatus.setText("Connected");
        if (mInfo.groupOwnerAddress.getHostAddress() != null) {
        } else {
            CommonMethods.DisplayToast(getApplicationContext(), "Host Address not found");
        }

        try {
            String GroupOwner = mInfo.groupOwnerAddress.getHostAddress();
            if (GroupOwner != null && !GroupOwner.equals(""))
                SharedPreferencesHandler.setStringValues(getApplicationContext(), "GroupOwnerAddress", GroupOwner);
            if (mInfo.groupFormed && mInfo.isGroupOwner) {
                SharedPreferencesHandler.setStringValues(getApplicationContext(), "ServerBoolean", "true");
                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(DeviceDetailActivity.this, FileTransferService.PORT);
                if (FileServerobj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[]{null});
                    } else
                        FileServerobj.execute();
                }
            } else {
                if (!ClientCheck) {
                    firstConnectionMessage firstObj = new firstConnectionMessage(GroupOwnerAddress);
                    if (firstObj != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            firstObj.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR,
                                    new String[]{null});
                        } else
                            firstObj.execute();
                    }
                }

                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(DeviceDetailActivity.this, FileTransferService.PORT);
                if (FileServerobj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                    } else
                        FileServerobj.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static Handler handler;

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_connect:
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(DeviceDetailActivity.this, "Press <- to cancel", "Connecting:" + mDevice.deviceAddress, true, true);
                AppController.manager.connect(AppController.channel, config, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        txtFriendDeviceStatus.setText("Connected");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(DeviceDetailActivity.this, "Connect failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        finish();
                    }
                });

                break;
            case R.id.btn_disconnect:
                AppController.manager.removeGroup(AppController.channel, new ActionListener() {

                    @Override
                    public void onFailure(int reasonCode) {

                    }

                    @Override
                    public void onSuccess() {
                        txtFriendDeviceStatus.setText("Available");
                        finish();
                    }

                });
                break;
            case R.id.btn_send_video:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                break;
            case R.id.btn_send_image:
                Intent intent1 = new Intent(Intent.ACTION_PICK);
                intent1.setType("image/*");
                startActivityForResult(intent1, CHOOSE_FILE_RESULT_CODE);
                break;
            case R.id.ibtn_back:
                finish();
                break;
        }
    }

    @Override
    public void onDisconnectStatus() {
        txtFriendDeviceStatus.setText("Available");
        // finish();
    }

    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

        private Context mFilecontext;
        private String mExtension, mKey;
        private File mEncryptedFile;
        private long mReceivedFileLength;
        private int mPORT;
        private String mFile_ext;

        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;

            handler = new Handler();
            this.mPORT = port;

            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(mFilecontext,
                        ProgressDialog.THEME_HOLO_LIGHT);
        }


        @Override
        protected String doInBackground(String... params) {
            ServerSocket serverSocket;
            Socket client;
            try {
                CommonMethods.e("File Async task port", "File Async task port-> " + mPORT);
                // init handler for progressdialog
                try {
                    serverSocket = new ServerSocket(mPORT);
                    Log.d(CommonMethods.Tag, "Server: Socket opened");
                    client = serverSocket.accept();
                    Log.d("Client InetAddress:", "" + client.getInetAddress());

                    WiFiClientIp = client.getInetAddress().getHostAddress();

                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    WiFiTransferModal obj = null;

                    String InetAddress;
                    try {
                        obj = (WiFiTransferModal) ois.readObject();
                        InetAddress = obj.getmInetAddress();
                        if (InetAddress != null && InetAddress.equalsIgnoreCase(FileTransferService.inetaddress)) {
                            CommonMethods.e("File Async Group Client Ip", "port-> " + WiFiClientIp);
                            SharedPreferencesHandler.setStringValues(mFilecontext, "WiFiClientIp", WiFiClientIp);
                            CommonMethods.e("File Async Group Client Ip from SHAREDPrefrence", "port-> "
                                    + SharedPreferencesHandler.getStringValues(mFilecontext, "WiFiClientIp"));
                            SharedPreferencesHandler.setStringValues(mFilecontext, "ServerBoolean", "true");
                            ois.close(); // close the ObjectOutputStream object
                            serverSocket.close();
                            return "Demo";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ((DeviceDetailActivity) mFilecontext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.setMessage("Receiving:");
                            mProgressDialog.setIndeterminate(false);
                            mProgressDialog.setMax(100);
                            mProgressDialog.setProgress(0);
                            mProgressDialog.setProgressNumberFormat(null);
                            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            mProgressDialog.show();
                        }
                    });
                    Log.e("FileName Got:", obj.getmFileName());
                    final File f = new File(Environment.getExternalStorageDirectory() + "/" + folderName + "/" + obj.getmFileName());
                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();
                    this.mReceivedFileLength = obj.getmFileLength();
                    InputStream inputstream = client.getInputStream();
                    copyRecievedFile(inputstream, new FileOutputStream(f), mReceivedFileLength);
                    ois.close(); // close the ObjectOutputStream object after saving
                    // file to storage.
                    serverSocket.close();
                    this.mExtension = obj.getmFileName();
                    this.mEncryptedFile = f;
                    mFile_ext = obj.getmFileName();
                    Log.e("EXT:", mFile_ext);
                    return f.getAbsolutePath();
                } catch (ConnectException e) {
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            if (result != null) {
                if (!result.equalsIgnoreCase("Demo")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    if (mFile_ext.contains(".jpg") || mFile_ext.contains("png"))
                        intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                    else if (mFile_ext.contains(".mp3"))
                        intent.setDataAndType(Uri.parse("file://" + result), "audio/*");
                    else if (mFile_ext.contains(".mp4") || mFile_ext.contains(".mkv"))
                        intent.setDataAndType(Uri.parse("file://" + result), "video/*");
//                    else if(mFile_ext.contains(".pdf"))
//                        intent.setDataAndType(Uri.parse("file://" + result), "application/pdf");
//                    else if(mFile_ext.contains(".doc")||mFile_ext.contains(".docx"))
//                        intent.setDataAndType(Uri.parse("file://" + result), "application/msword");
//                    else if(mFile_ext.contains(".ppt")||mFile_ext.contains(".pptx"))
//                        intent.setDataAndType(Uri.parse("file://" + result), "application/vnd.ms-powerpoint");
//                    else if(mFile_ext.contains(".txt")||mFile_ext.contains(".xml"))
//                        intent.setDataAndType(Uri.parse("file://" + result), "plain/text");
//                    else if(mFile_ext.contains(".apk"))
//                        intent.setDataAndType(Uri.parse("file://" + result), "*");
                    mFilecontext.startActivity(intent);
                } else {
                    FileServerAsyncTask FileServerobj = new
                            FileServerAsyncTask(mFilecontext, FileTransferService.PORT);
                    if (FileServerobj != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                        } else FileServerobj.execute();
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mFilecontext);
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        long total = 0;
        long test = 0;
        byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        int progresspercentage1 = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
                    total += len;
                    if (actualFilelength > 0) {
                        progresspercentage1 = (int) ((total * 100) / actualFilelength);
                        percentage = progresspercentage1;
                        mProgressDialogSend.setProgress(progresspercentage1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    percentage = 0;
                    actualFilelength = 0;
                    if (mProgressDialogSend != null) {
                        if (mProgressDialogSend.isShowing()) {
                            mProgressDialogSend.dismiss();
                        }
                    }

                }
            }
            if (mProgressDialogSend != null) {
                if (mProgressDialogSend.isShowing()) {
                    mProgressDialogSend.dismiss();
                }
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream, OutputStream out, Long length) {

        byte buf[] = new byte[FileTransferService.ByteSize];
        byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
        String Decrypted;
        int len;
        long total = 0;
        int progresspercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    total += len;
                    if (length > 0) {
                        progresspercentage = (int) ((total * 100) / length);
                    }
                    mProgressDialog.setProgress(progresspercentage);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            out.close();
            inputStream.close();
        } catch (Exception e) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            Log.d(MainActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public void showprogress(final String task, final Context context) {
        if (mProgressDialogSend == null) {
            mProgressDialogSend = new ProgressDialog(context,
                    ProgressDialog.THEME_HOLO_LIGHT);
            mSendDialog = true;
        }

        ((DeviceDetailActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialogSend.setMessage(task);
                mProgressDialogSend.setIndeterminate(false);
                mProgressDialogSend.setMax(100);
                mProgressDialogSend.setProgress(0);
                mProgressDialogSend.setProgressNumberFormat(null);
                mProgressDialogSend.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialogSend.show();
            }
        });

    }

    public static void DismissProgressDialog() {
        try {
            if (mProgressDialogSend != null) {
                if (mProgressDialogSend.isShowing()) {
                    mProgressDialogSend.dismiss();
                    mSendDialog = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class firstConnectionMessage extends AsyncTask<String, Void, String> {
        String GroupOwnerAddress = "";

        public firstConnectionMessage(String owner) {
            // TODO Auto-generated constructor stub
            this.GroupOwnerAddress = owner;
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            CommonMethods.e("On first Connect", "On first Connect");
            Intent serviceIntent = new Intent(DeviceDetailActivity.this, WiFiClientIPTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

            if (mInfo.groupOwnerAddress.getHostAddress() != null) {
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, mInfo.groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.inetaddress, FileTransferService.inetaddress);
            }
            startService(serviceIntent);
            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result != null) {
                if (result.equalsIgnoreCase("success")) {
                    CommonMethods.e("On first Connect", "On first Connect sent to asynctask");
                    ClientCheck = true;
                }
            }
        }
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(MainActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return AVAILABLE;
            case WifiP2pDevice.INVITED:
                return UNAVAILABLE;
            case WifiP2pDevice.CONNECTED:
                return CONNECTED;
            case WifiP2pDevice.FAILED:
                return FAILED;
            case WifiP2pDevice.UNAVAILABLE:
                return UNAVAILABLE;
            default:
                return UNKNOWN;
        }
    }

    @Override
    protected void onDestroy() {
        AppController.deviceDetailActivity = null;
        super.onDestroy();
    }
}
