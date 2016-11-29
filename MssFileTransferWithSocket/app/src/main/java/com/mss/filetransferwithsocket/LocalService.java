package com.mss.filetransferwithsocket;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by ravishankar on 21/11/16.
 */

public class LocalService extends Service {


    // private final IBinder mBinder = new LocalBinder();
    WelcomeSocketThread mWelcomeSocketThread = null;  //the welcome socket thread
    Handler mSocketSendResultHandler = null;          //used to receive the result of message sending operations
    Handler mRefreshHandler = null;
    ArrayList<User> mDiscoveredUsers = null;
    private boolean mIsWifiPeerValid = false;
    public static NotificationManager mNotificationManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        InitClassMembers();
        //create a message handler that'll work with the "SendSingleStringViaSocketThread" threads
        if (mSocketSendResultHandler == null)
            mSocketSendResultHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle data = msg.getData();
                    String result = data.getString(Constants.SINGLE_SEND_THREAD_KEY_RESULT);
                    String RoomID = data.getString(Constants.SINGLE_SEND_THREAD_KEY_UNIQUE_ROOM_ID);
                    HandleSendAttemptAndBroadcastResult(result, RoomID);
                }
            };

        //create a handler to send delayed messages to itself and perform a peer refresh operation.
        //also, it checks if our current wifi p2p peer is active (has our app and runs properly)
        /*if (mRefreshHandler==null)
        {
            mRefreshHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    //if this is a wifi peer validation TO
                    if (msg.what==Handler_WHAT_valueForActivePeerTO)
                    {
                        if (!mIsWifiPeerValid)  //if we haven't received any valid communication from this peer we're connected to
                        {
                            ChatSearchScreenFrag.mIsConnectedToGroup=false; //mark that this connection is invalid
                            ChatSearchScreenFrag.mManager.removeGroup(ChatSearchScreenFrag.mChannel, null); //leave the current group
                        }
                    }
                    else //it's a TO telling us to send discovery messages to all peers
                    {
                        if (ChatSearchScreenFrag.mIsWifiDirectEnabled)
                            LocalService.this.OnRefreshButtonclicked();  //perform a peer refresh
                        DeleteTimedOutRooms();  //delete TO'd rooms if necessary
                        //send a delayed empty message to ourselves
                        sendEmptyMessageDelayed(0, MainScreenActivity.RefreshPeriodInMs);
                    }
                }
            };
            //send the 1st message to trigger the logical peer-discovery refresh procedure
            mRefreshHandler.sendEmptyMessageDelayed(0, 500);
        }//if*/

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void UpdateDiscoveredUsersList(String peerIP, String unique, String name) {
        mIsWifiPeerValid = true;  //note that we've received a valid discovery message. WE'RE LIVE AND RUNNING!
        boolean isFound = false;
        if (unique == null) //if this func is called by the b-cast receiver (the unique is unknown at this state)
        {
            synchronized (mDiscoveredUsers) {
                for (User user : mDiscoveredUsers) //for each existing user
                {
                    if (user.mIPAddr.equalsIgnoreCase(peerIP)) //if this IP exists
                    {
                        isFound = true;
                        break;
                    }
                }//for

                if (!isFound) {
                    User peer = new User(null, peerIP, null); //create a new peer
                    mDiscoveredUsers.add(peer);
                }
            }//synch
        }//if
        else //the user's unique is given
        {
            synchronized (mDiscoveredUsers) {
                for (User user : mDiscoveredUsers) //for each existing user
                {
                    //if we found the user
                    if ((user.mUniqueID != null && user.mUniqueID.equalsIgnoreCase(unique)) || user.mIPAddr.equalsIgnoreCase(peerIP)) {
                        user.mIPAddr = peerIP;  //update the IP address
                        user.name = name;     //update the name
                        user.mUniqueID = unique; //update the unique ID
                        isFound = true;
                        break;
                    }

                }//for

                if (!isFound) //if this peer doesn't exist in our registry at all
                {
                    User peer = new User(unique, peerIP, name); //create a new peer
                    mDiscoveredUsers.add(peer);
                }

            }//synch
        }//else
    }

    private void InitClassMembers() {

        if (mDiscoveredUsers == null) {
            mDiscoveredUsers = new ArrayList<User>();
        }


        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void HandleSendAttemptAndBroadcastResult(String result, String RoomID) {
        Intent intent = CreateBroadcastIntent();
        intent.putExtra(Constants.SERVICE_BROADCAST_OPCODE_KEY, Constants.SERVICE_BROADCAST_OPCODE_JOIN_SENDING_RESULT); //the opcode is connection result event
        intent.putExtra(Constants.SINGLE_SEND_THREAD_KEY_UNIQUE_ROOM_ID, RoomID); //set the room's ID

        if (result.equalsIgnoreCase(Constants.SINGLE_SEND_THREAD_ACTION_RESULT_SUCCESS)) //if physical connection was established and a request was sent
            intent.putExtra(Constants.SINGLE_SEND_THREAD_KEY_RESULT, Constants.SINGLE_SEND_THREAD_ACTION_RESULT_SUCCESS); //set a 'success' result
        else    //connection failed (for physical reasons)
            intent.putExtra(Constants.SINGLE_SEND_THREAD_KEY_RESULT, Constants.SINGLE_SEND_THREAD_ACTION_RESULT_FAILED); //set a 'failed' result value

        sendBroadcast(intent); //b-cast
    }

    public Intent CreateBroadcastIntent() {
        return new Intent(Constants.SERVICE_BROADCAST);
    }

    public void OnWelcomeSocketCreateError() {
        Intent intent = CreateBroadcastIntent();
        intent.putExtra(Constants.SERVICE_BROADCAST_OPCODE_KEY, Constants.SERVICE_BROADCAST_WELCOME_SOCKET_CREATE_FAIL);
    }
}
