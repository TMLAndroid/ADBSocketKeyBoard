package com.android.adbkeyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import static com.android.adbkeyboard.AdbIME.TAG;


public class ThreadReadWriterIOSocket implements Runnable,TextSendPrepareListener
{
	private Socket client;
	private Context context;
	private InputMethodService inputMethodService;
	private BufferedOutputStream out;
	private BufferedInputStream in;
	private static final String NULLFLAG = "nulltext";
	private int perIndex = 0;
	private String perStr = "";

	public ThreadReadWriterIOSocket(Context context, Socket client, InputMethodService inputMethodService)
	{
		this.client = client;
		this.context = context;
		this.inputMethodService =inputMethodService;
	}

	@Override
	public void run()
	{
		Log.d("chl", "a client has connected to server!");
		try {
			/* PC端发来的数据msg */
			String currCMD = "";
			out = new BufferedOutputStream(client.getOutputStream());
			in = new BufferedInputStream(client.getInputStream());
			try
			{


				while (AdbIME.ioThreadFlag )
				{
					try
					{
						if (!client.isConnected())
						{
							Log.v(TAG,   "未连接");
							break;
						}
						//本地数据发送给pc端

						String text = getText();//手机的文本
						InputConnection ic = inputMethodService.getCurrentInputConnection();
						ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
						int selectionStart = et.selectionStart;
						Log.v(TAG,   "发送index:" + selectionStart+"text:"+text);
						if (text.equals(perStr) && selectionStart == perIndex){
							Log.v(TAG,   "相等");
						}else {

							byte[] phoneStartCursor = ByteUtils.int2Bytes(selectionStart, 2);
							byte[] sendToPcBytes = ByteUtils.byteMergerAll(phoneStartCursor, phoneStartCursor, text.getBytes());
							out.write(sendToPcBytes);
							out.flush();
						}
					/* 接收PC发来的数据 */
						Log.v(TAG, Thread.currentThread().getName() + "---->" + "will read......");
					/* 读操作命令 */
						currCMD = readCMDFromSocket(in);
						if (currCMD.equals("")){
							return;
						}

						byte[] bytes = currCMD.getBytes();
						byte[] startCursor = Arrays.copyOfRange(bytes, 0,2);
						byte[] endCursor = Arrays.copyOfRange(bytes, 2,4);
						String result = new String(bytes,4,bytes.length-4);



						int startIndex = ByteUtils.bytes2Int(startCursor, 0, 2);
						int endIndex = ByteUtils.bytes2Int(endCursor, 0, 2);
						Log.i(TAG, "startIndex:"+startIndex+" endIndex:"+endIndex+" result:"+result);
						Log.v(TAG, Thread.currentThread().getName() + "---->" + "**currCMD ==== " + currCMD);
						InputConnection inputConnection = inputMethodService.getCurrentInputConnection();

						perIndex = endIndex;
						perStr = result;

					/*	if (result.equals("") &&startIndex == endIndex ){//移动指针
							inputConnection.setSelection(endIndex,endIndex);//设置指针
							Log.i("TTT","jinru");
						}else {*/
							//CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
							//CharSequence beforCursorText = inputConnection.getTextBeforeCursor(currentText.length(), 0);
							//CharSequence afterCursorText = inputConnection.getTextAfterCursor(currentText.length(), 0);
							//inputConnection.deleteSurroundingText(beforCursorText.length(), afterCursorText.length());
							inputConnection.beginBatchEdit();
							inputConnection.deleteSurroundingText(100000, 100000);
							inputConnection.commitText(result,result.length());
							inputConnection.setSelection(endIndex,endIndex);
							inputConnection.endBatchEdit();
						//}




					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				out.close();
				in.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					if (client != null)
					{
						Log.v(TAG, Thread.currentThread().getName() + "---->" + "client.close()");
						client.close();
					}
				} catch (IOException e)
				{
					Log.e(TAG, Thread.currentThread().getName() + "---->" + "read write error333333");
					e.printStackTrace();
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getText() {
		String text = "";
		try {
			InputConnection conn = inputMethodService.getCurrentInputConnection();
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

	/* 读取命令 */
	public String readCMDFromSocket(InputStream in)
	{
		int MAX_BUFFER_BYTES = 2048;
		String msg = "";
		byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
		try
		{
			int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
			if (numReadedBytes < 0){
				Log.d(TAG,"没有读取到");
				return "";
			}
			msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
			tempbuffer = null;
		} catch (Exception e)
		{
			Log.v(TAG, Thread.currentThread().getName() + "---->" + "readFromSocket error");
			e.printStackTrace();
		}

		// Log.v(Service139.TAG, "msg=" + msg);
		return msg;
	}

	@Override
	public void Prepare(boolean hasPrepare) {
		/*if (client.isConnected() && out != null)
		{
			try {
			 if (hasPrepare){
				out.write("send".getBytes());
			}else {
				out.write("cancelsend".getBytes());
			}
			out.flush();





			} catch (IOException e) {
			e.printStackTrace();
		}
		}*/
	}
}