package com.mss.filetransferwithsocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Opens a socket connection to a given IP and perform the desired action.
 * If this thread is defined as passive, it tries to receive a request from the socket and replies if necessary.
 * If this thread is defined as active, it tries to send a discovery request via socket and then waits for a reply.
 */
public class NewConnectionWorkerThread extends Thread {
    Socket mSocket;
    LocalService mService;
    //A passive socket thread will wait on data coming from the socket, reply accordingly and will shut down.
    //An active socket will initiate the communication with the target socket.
    boolean mIsPassive;
    User mPeer;        //the peer we're connecting to
    private int mQueryCode;  //will be used to differentiate between 3 possible operation: discover peer / start private chat / join public chat room

    PrintWriter mOut = null;
    BufferedReader mIn = null;


    /**
     * Constructor for a passive socket
     *
     * @param srv  - reference to the LocalService
     * @param sock - an open socket
     */
    public NewConnectionWorkerThread(LocalService srv, Socket sock) {
        mSocket = sock;
        mIsPassive = true;
        mService = srv;
    }

    /**
     * Constructor for an active socket
     *
     * @param srv   - reference to the LocalService
     * @param peer  - a peer to connect to
     * @param Qcode - the code of the operation to be performed. Taken from Constants.class
     */
    public NewConnectionWorkerThread(LocalService srv, User peer, int Qcode) {
        mService = srv;
        mIsPassive = false;
        mPeer = peer;
        mQueryCode = Qcode;

    }

    @Override
    public void run() {
        if (mIsPassive)
            InitiatePassiveTransaction();
        else
            InitiateRelevantActiveTransaction();

        CloseInputAndOutputStreams();
    }

    /**
     * Closes mIn and mOut
     */
    private void CloseInputAndOutputStreams() {
        if (mOut != null)
            mOut.close();

        if (mIn != null)
            try {
                mIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }//end of CloseInputAndOutputStreams()

    /**
     * Case handler. Switches between different incoming requests.
     * Called by ReceiveDiscoveryMessage() if the thread is passive
     *
     * @param input - A String array, as returned by BreakDiscoveryMessageToStrings(String)
     */
    private void PassiveDiscoveryQueryCaseHandler(String[] input) {
        if (input.length < 3) //This is some sort of a garbled message. Should never happen.
            return;

        //We want to update the discovered users list only if this isn't a message
        //for a not-hosted public chat (Because the owner of a public chat room forwards the messages as they are and it'll mess up our
        //tracking over the users' IP addresses)
        boolean SkipUserUpdate = false;
        if (input[0].equalsIgnoreCase(Integer.toString(Constants.CONNECTION_CODE_NEW_CHAT_MSG))) //if this is a chat message
            //if the destination is us, we don't want to skip the update.
            //	SkipUserUpdate = input[3].split("[_]")[0].equalsIgnoreCase(MainScreenActivity.UniqueID)? false : true;

            if (!SkipUserUpdate)
                mService.UpdateDiscoveredUsersList(mSocket.getInetAddress().getHostAddress(), input[2], input[1]);

        String PeerIP = mSocket.getInetAddress().getHostAddress();  //get the peer's IP

		/*if (input[0].equalsIgnoreCase(Integer.toString(Constants.CONNECTION_CODE_DISCOVER)))
        {
			//discovery logic
			SendDiscoveryMessage();  //send a discovery message back to the peer
			ArrayList<ChatRoomDetails> Rooms = ConvertDiscoveryStringToChatRoomList(input); //get the chat room data from the input
			mService.UpdateChatRoomHashMap(Rooms); //update the chat rooms hash at the service.
		}*/
        //if this is a peer publication message
        if (input[0].equalsIgnoreCase(Integer.toString(Constants.CONNECTION_CODE_PEER_DETAILS_BCAST))) {
            //ParsePeerPublicationMessageAndUpdateDiscoveredPeers(input);
        }
        //if this is a join request for a private chat:
        /*if (input[0].equalsIgnoreCase(Integer.toString(Constants.CONNECTION_CODE_PRIVATE_CHAT_REQUEST)))
		{
			String result = CheckIfNotIgnored(input); //check if this user is banned or not
			
			if (result.equalsIgnoreCase(Constants.SERVICE_POSTIVE_REPLY_FOR_JOIN_REQUEST)) //if connection is approved
			{
				if (mService.mDiscoveredChatRoomsHash.get(input[2])!=null) //if a matching discovered room exists
					mService.CreateNewPrivateChatRoom(input);
				else //a matching discovered room doesn't exist
					mService.BypassDiscoveryProcedure(input,false,false); 
				
				SendReplyForAJoinRequest(PeerIP,true,MainScreenActivity.UniqueID,null,true);
			}
			else  //connection wasn't approved. result contains the reason why
			{
				SendReplyForAJoinRequest(PeerIP,false,MainScreenActivity.UniqueID,result,true);
			}	
		}*/
        //if this is a join request for a public chat:
        //if this is a reply for our private chat 'Join' request

        //if this is a reply for our public chat 'Join' request

        //if a user has request us to remove him from a room we host

        //if this is a new message targeted to on of the active chat rooms
        //else //this message came for a public chat room

    }//end of PassiveDiscoveryQueryCaseHandler()


    /**
     * Parses a peer publication string (that comes from the group owner) and updates the
     * discovered peers list
     * @param input
     */
//end of ParsePeerPublicationMessageAndUpdateDiscoveredPeers()

    /**
     * Sends a reply for a join request (also used when a banned user tries to send us a message)
     * Format: PC reply opcode$(accepted/denied)$denial reason$self unique
     * @param peerIP
     * @param isApproved
     * @param RoomID
     * @param reason
     * @param isPrivateChat
     */


    /**
     * Checks if the user is approved to join the desired chat room
     * @param input - incoming request string after parsing
     * @return Constants.SERVICE_POSTIVE_REPLY_FOR_JOIN_REQUEST if the user is not ignored,
     * Constants.SERVICE_NEGATIVE_REPLY_FOR_JOIN_REQUEST_REASON_BANNED otherwise
     */
    //end of HandleNewChatConnectionRequest()

    /**
     * Passive transaction
     */
    private void InitiatePassiveTransaction() {
        if (!ReceiveDiscoveryMessage()) //if query message reception was unsuccessful.
            return;
    }

    /**
     * active discovery
     */
    private void ActiveDiscoveryProcedure() {
        if (!SendDiscoveryMessage()) //if query message sending was unsuccessful
            return;

        ReceiveDiscoveryMessage();

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }//end of ActiveDiscoveryProcedure()

    /**
     * Sends a discovery message over the socket
     *
     * @return - true if successful, false otherwise
     */
    private boolean SendDiscoveryMessage() {
        //DISCOVERY QUERY SEND LOGIC:
        if (mOut == null || mIn == null) //if buffers aren't initialized
        {
            try //open input and output streams
            {
                // mOut = new PrintWriter(mSocket.getOutputStream(), true);
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }//if
		
		/*String toSend = BuildDiscoveryString(); //get the query string to be send
		mService.CreateAndBroadcastToast("Sending a query message string!");*/
        //mOut.println(toSend); //send via socket
        //mOut.flush();

        return true;

    }//end of SendDiscoveryMessage()

    /**
     * Tries to receive a discovery string from the socket.
     * If this thread is in Active mode:
     * On success: parses the string and invokes an update method at the service
     * On failure (Socket crash or timeout): aborts and closes the socket
     * If this thread is in Passive mode:
     * On success: parses the string and calls PassiveDiscoveryQueryCaseHandler()
     * On failure (Socket crash or timeout): aborts and closes the socket
     */
    private boolean ReceiveDiscoveryMessage() {
        //DISCOVERY RECEIVE LOGIC:
        int numberOfReadTries = 0;
        //char[] receivedMsg = new char[100];
        String receivedMsg = null;

        if (mOut == null || mIn == null) //if buffers aren't initialized
        {
            try //open input and output streams
            {
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }//if

        while (numberOfReadTries <= Constants.NUM_OF_QUERY_RECEIVE_RETRIES) //time gap between retries is 200 ms
        {
            try {
                //if (mIn.ready() && (receivedMsg = mIn.read) != null) //msg was successfully received
                if (mIn.ready()) //msg was successfully received
                {
                    //mIn.read(receivedMsg);
                    receivedMsg = mIn.readLine();
                    numberOfReadTries = Constants.NUM_OF_QUERY_RECEIVE_RETRIES + 1; //force a break from the loop
                }
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }

            numberOfReadTries++;
        }//while

        @SuppressWarnings("unused")
        boolean sda = mSocket.isConnected();

        if (receivedMsg == null)
            return false;

        String[] parsedInput = BreakDiscoveryMessageToStrings(receivedMsg); //parse the input

        //if this thread is passive, parse the message and allow us to respond to it's opcode
        if (mIsPassive) {
            PassiveDiscoveryQueryCaseHandler(parsedInput);
        } else //if Active: parse the message and update the DB
        {
            //update the discovered user details:
            mService.UpdateDiscoveredUsersList(mSocket.getInetAddress().getHostAddress(), parsedInput[2], parsedInput[1]);
		/*	ArrayList<ChatRoomDetails> Rooms = ConvertDiscoveryStringToChatRoomList(parsedInput); //create a discovered chat room list
			mService.UpdateChatRoomHashMap(Rooms); *///update the chat rooms hash at the service.
        }

        return true;
    }//end of ReceiveDiscoveryMessage()

    /**
     * Converts the discovery String that was received from a peer to a list of discovered chat rooms
     * @param input - A String array, as returned by BreakDiscoveryMessageToStrings(String)
     * @return An ArrayList of discovered chat rooms
     */
    //end of ConvertDiscoveryStringToChatRoomList()

    /**
     * Switches between Active operations (Discover / Start private chat / Join a chat room).
     */
    private void InitiateRelevantActiveTransaction() {
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            mSocket = new Socket();
            mSocket.bind(null);
            mSocket.connect((new InetSocketAddress(mPeer.mIPAddr, Constants.WELCOME_SOCKET_PORT)), 3000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (mSocket == null) {
            return;
        }

        switch (mQueryCode) {
            case Constants.CONNECTION_CODE_DISCOVER: {
                ActiveDiscoveryProcedure();
                break;
            }
        }

    }//end of InitiateRelevantTransaction()

    /**
     * Splits a string with our special character used as a delimiter
     *
     * @param input - a discovery String that was received from a peer
     * @return String array that was parsed by our special character
     */
    private String[] BreakDiscoveryMessageToStrings(String input) {
        return input.split("[" + Constants.STANDART_FIELD_SEPERATOR + "]"); //parse the string by the separator char
    }

    /**
     * Creates a discovery string, containing data about all hosted rooms on this device, to be sent via socket
     * @return String
     */
    //end of BuildDiscoveryString()


}
