/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package  com.example.carinside.voice;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.lang.reflect.Method;
import java.net.Socket;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.io.FileOutputStream;
import 	android.content.BroadcastReceiver;
import 	android.content.IntentFilter;
import android.view.KeyEvent;
import java.util.List;
import android.bluetooth.BluetoothAssignedNumbers;
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */



public class BluetoothChatService implements BluetoothProfile.ServiceListener{
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "MainActivitySecure";
    private static final String NAME_INSECURE = "MainActivityInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    // Member fields
    private  BluetoothAdapter mAdapter;
    private  BluetoothHeadset mBluetoothHeadset; 
    private  Handler mHandler = null;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 4;  // now connected to a remote device
    
    public static int VOICE_CMD_MEDIABUTTON = 0x1000;
    public final static int VOICE_CMD_UP = 1;
    public final static int VOICE_CMD_DOWN = 2;
    public final static int VOICE_CMD_PREV = 3;
    public final  static int VOICE_CMD_NEXT = 4;
    public final  static int VOICE_CMD_PLAY = 5;
    protected boolean    m_keep_running ;
    
    private  BluetoothDevice mDevices;
    /**
     * Constructor. Prepares a new MainActivity session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    
	    public void onServiceConnected(int profile, BluetoothProfile proxy) { 
	        if (profile == BluetoothProfile.HEADSET) { 
	            mBluetoothHeadset = (BluetoothHeadset) proxy; 
	            List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();         
	            for ( final BluetoothDevice dev : devices ) {           
	            	if(BluetoothDevice.BOND_BONDED == dev.getBondState())
            	    {
	            		mDevices = dev;
	            		Log.e(TAG,"BluetoothChatService BINDING DEVICE:" + dev.getName());
	  					connect(mDevices);
	            		break;
            	    }
	            }
	        } 
	    } 
	    public void onServiceDisconnected(int profile) { 
	        if (profile == BluetoothProfile.HEADSET) { 
	        	mBluetoothHeadset = null; 
				this.stop();
				Log.e(TAG,"BluetoothChatService Disconnect BINDING DEVICE:" + mDevices.getName());
	        } 
	    } 

    public BluetoothChatService(Context context,Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
		Log.e(TAG,"BluetoothChatService INIT ===================");
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mAdapter.getProfileProxy(context, this, BluetoothProfile.HEADSET);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
        ///////////////////////////////////////////////
        try
	     {
	            m_keep_running = true ;
	     }
	     catch(Exception e)
	     {
	    	 e.printStackTrace();
	     }
        /////////////////////////////////////////////
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
       //james 
      /*  if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }*/
        connect(mDevices);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) 
        {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected, Socket Type:" );

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        mAdapter.closeProfileProxy(BluetoothProfile.HEADSET,mBluetoothHeadset);
        
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
    	if(mHandler != null)
    	{
	        Message msg = mHandler.obtainMessage(VoiceRecognize.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(VoiceRecognize.TOAST, "Unable to connect device");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
    	}
    	 setState(STATE_DISCONNECTED);
        // Start the service over to restart listening mode
       // BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
    	if(mHandler != null)
    	{
	        Message msg = mHandler.obtainMessage(VoiceRecognize.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(VoiceRecognize.TOAST, "Device connection was lost");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
    	}	
        // Start the service over to restart listening mode
    	 setState(STATE_DISCONNECTED);
      //  BluetoothChatService.this.start();
        
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";
            if (D) Log.d(TAG, "Socket Type: " + mSocketType + "::BEGIN AcceptThread::" + secure );
            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                        case STATE_DISCONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
            	tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SPP);//MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
          
            mmSocket = tmp;
        }

        public void run() {
            Log.e(TAG, "BEGIN mConnectThread ");
            setName("ConnectThread" );

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                Log.e(TAG, "Connection Failed  socket during connection failure:", e);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        public void run() {
            Log.e(TAG, "BEGIN mConnectedThread");
         
            int bytes;
         	byte [] bytes_pkg = null ;
	         // Keep listening to the InputStream while connected
            while (true) {
                try {
                	//byte[]  r = "AT+BRSF=30\r".getBytes();
                	//mmOutStream.write(r);
                	byte[] buffer = new byte[200];
                	bytes = mmInStream.read(buffer);
                	String command = new String(buffer).trim();
                	//Log.e(TAG,"output=" + command);
                	parseData(bytes,buffer);
	                if(command.equals("AT+CKPD=200"))
	                {
	                	byte[]  r1= "OK\r\n".getBytes();
	                	mmOutStream.write(r1);
	                	if(mHandler != null)
	                	{
	                		Message msg = mHandler.obtainMessage();
							msg.what = DeviceService.VOICE_CMD_MEDIABUTTON;
							Bundle bundle = new Bundle();
							bundle.putBoolean(DeviceService.VOICE_ACTION_STATUS, true);
							bundle.putInt(DeviceService.VOICE_CMD_TYPE, DeviceService.VOICE_CMD_UP);
							msg.setData(bundle);
							msg.sendToTarget();
	                	}
	                }else if(command.equals("AT+BRSF=30"))
	                {
	                		Log.e(TAG, "AT COMMAND AT+BRSF=30");
	                	byte[]  r1= "+BRSF:30\r\n".getBytes();
	                	mmOutStream.write(r1);
	                }
	            
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode

                    //BluetoothChatService.this.start();
                    break;
                }
            }
        }
        
        private void parseData(int bytes,byte[] out_bytes)
        {
        	for(int i = 0 ;i+16<= bytes ; i+=16)
			{
        		
				if(out_bytes[i] == (byte)0xff && out_bytes[i+1] == (byte)0xff)
				{
						byte action_down = out_bytes[i+8];
						byte action_up = out_bytes[i+9];
						if(action_down > 0 || action_up > 0)
						{
							int iAction = 0;
							boolean bVoice = false;
							if((action_down & 0x2) > 0)
							{
								iAction =VOICE_CMD_NEXT;
							}
							
							if((action_down & 0x4) > 0)
							{
								iAction =VOICE_CMD_PREV;
							}
							
							if((action_down & 0x1) > 0)
							{
								iAction =VOICE_CMD_PLAY;
								bVoice = true;
							}
							
							if((action_up & 0x01)> 0)
							{
								//iAction =VOICE_CMD_PLAY;
								bVoice = false;
							}
							
							if(iAction > 0)
							{
								Message msg = mHandler.obtainMessage();
								msg.what = DeviceService.VOICE_CMD_MEDIABUTTON;
								Bundle bundle = new Bundle();
								bundle.putBoolean(DeviceService.VOICE_ACTION_STATUS, bVoice);
								bundle.putInt(DeviceService.VOICE_CMD_TYPE, iAction);
								msg.setData(bundle);
								msg.sendToTarget();
								Log.e(TAG,"output=" +String.valueOf(iAction));
							}
						}
						
					//	Log.i("gryo device output:","acc_x=" + accel_x +" acc_y=" + accel_y +" acc_z=" + accel_z +
					//			" gyro_x=" + gyro_x +" gyro_y=" + gyro_y +" gyro_z=" + gyro_z );
					}
				}
			}
       
        public String  printHexString( byte[] b,int len) 
        { 
        	String str = "";
        	for (int i = 0; i <len; i++) { 
        	String hex = Integer.toHexString(b[i] & 0xFF); 
        	if (hex.length() == 1) { 
        	hex = '0' + hex; 
        	} 
        	 str += hex.toUpperCase(); 
        	} 
        	return str;
        } 
        
        public void cancel() {
        	
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
