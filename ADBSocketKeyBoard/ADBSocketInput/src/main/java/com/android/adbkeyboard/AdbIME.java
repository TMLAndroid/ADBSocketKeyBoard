package com.android.adbkeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AdbIME extends InputMethodService {
;
	final int SERVER_PORT = 10086;

	public static Boolean mainThreadFlag = true;
	public static Boolean ioThreadFlag = true;
	public static final String TAG = "chl";

	ServerSocket serverSocket = null;
	private TextSendPrepareListener prepareListener;

	@Override
    public View onCreateInputView() {
    	View mInputView = getLayoutInflater().inflate(R.layout.view, null);
		//TextView textView = (TextView) mInputView.findViewById(R.id.text);


		new Thread() {

			private ThreadReadWriterIOSocket threadReadWriterIOSocket;

			public void run() {

				try {
					Log.d("chl", "doListen()");
					serverSocket = new ServerSocket(SERVER_PORT);
					Log.d("chl", "doListen() 2");
					while (mainThreadFlag) {
						Log.d("chl", "doListen() 4");
						Socket socket = serverSocket.accept();
						Log.d("chl", "doListen() 3");
						threadReadWriterIOSocket = new ThreadReadWriterIOSocket(AdbIME.this, socket, AdbIME.this);
						prepareListener = threadReadWriterIOSocket;
						new Thread(threadReadWriterIOSocket).start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();
        return mInputView; 
    } 
    
    public void onDestroy() {

    if (prepareListener != null) {
		prepareListener.Prepare(false);
	}
		// 关闭线程
		mainThreadFlag = false;
		ioThreadFlag = false;
		// 关闭服务器
		try
		{
			Log.v(TAG, Thread.currentThread().getName() + "---->" + "serverSocket.close()");
			if (serverSocket != null) serverSocket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		Log.v(TAG, Thread.currentThread().getName() + "---->" + "**************** onDestroy****************");
    	super.onDestroy();


    }



	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		if (prepareListener != null) {
			prepareListener.Prepare(true);
			//textView.setText("ON");
		}
	}

	@Override
	public void onFinishInput() {
		super.onFinishInput();
		if (prepareListener != null) {
			//textView.setText("OFF");
			prepareListener.Prepare(false);
		}
	}

	public String getText() {
		String text = "";
		try {
			InputConnection conn =getCurrentInputConnection();
			ExtractedTextRequest req = new ExtractedTextRequest();
			req.hintMaxChars = 1000000;
			req.hintMaxLines = 10000;
			req.flags = 0;
			req.token = 1;
			text = conn.getExtractedText(req, 0).text.toString();
		} catch (Throwable t) {
		}
		return text;
	}
}
