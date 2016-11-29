package com.mss.filetransferwithsocket;

import java.io.Serializable;

public class WiFiTransferModal implements Serializable {

    private String mFileName;
    private Long mFileLength;
    private String mInetAddress;


    public WiFiTransferModal() {

    }

    public WiFiTransferModal(String inetAddress) {
        this.mInetAddress = inetAddress;
    }

    public WiFiTransferModal(String name, Long filelength) {
        this.mFileName = name;
        this.mFileLength = filelength;
//		this.FileData = in;
    }

    public String getmInetAddress() {
        return mInetAddress;
    }

    public void setmInetAddress(String mInetAddress) {
        this.mInetAddress = mInetAddress;
    }


    public Long getmFileLength() {
        return mFileLength;
    }

    public void setmFileLength(Long mFileLength) {
        this.mFileLength = mFileLength;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }
}
