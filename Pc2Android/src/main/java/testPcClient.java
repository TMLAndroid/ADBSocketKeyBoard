

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * 测试usb与pc通信 通过adb端口转发方式
 * 
 * @author chl
 * 1
 */
public class testPcClient
{


    private  BufferedInputStream in;
    private  BufferedOutputStream out;
	private int currentDevice = 1;
	private Socket socket;
	private static final String NULLFLAG = "nulltext";
	private int perIndex = 0;
	private String perText = "";
	private final JTextArea jTextField;

    public testPcClient() {


		JFrame jFrame = new JFrame();
		jFrame.setPreferredSize(new Dimension(400,200));
		jTextField = new JTextArea();
		jTextField.setPreferredSize(new Dimension(100,100));
		System.out.println("文本变化a");


		jFrame.getContentPane().add(jTextField);
		jFrame.pack();
		jFrame.setVisible(true);
		jTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				int keyCode = e.getKeyCode();
				if (keyCode == 37 || keyCode == 39){//左
                    int caretPosition = jTextField.getCaretPosition();
                    byte[] startCursor = ByteUtils.int2Bytes(perIndex, 2);
                    byte[] endCursor = ByteUtils.int2Bytes(caretPosition, 2);
                    byte[] sendBytes = ByteUtils.byteMergerAll(startCursor, endCursor, jTextField.getText().getBytes());
                    try {
                        out.write(sendBytes);
                        out.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    perIndex = caretPosition;
				}

			}
		});


		addChangeListener(jTextField, new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				//JTextField s = (JTextField) e.getSource();
                try {



                    String text = jTextField.getText();
                    System.out.println("ttt:" + text);
                    byte[] startCursor = ByteUtils.int2Bytes(perIndex, 2);
                    int endIndex = jTextField.getCaretPosition();
                    byte[] endCursor = ByteUtils.int2Bytes(jTextField.getCaretPosition(), 2);
                   // byte[] getTextBytes = ByteUtils.byteMergerAll(startCursor, startCursor, "getText".getBytes());
                    //out.write(getTextBytes);
                    //out.flush();


                    //String sendText = "";
                    System.out.println("startIndex:" + perIndex + " endIndex:" + endIndex + " text:" + text);
                    //发送的协议为 之前光标的位置 之后光标的位置 增加的文本
                    byte[] sendBytes = ByteUtils.byteMergerAll(startCursor, endCursor, text.getBytes());
                    out.write(sendBytes);
                    out.flush();
                    perIndex = endIndex;
                }catch (IOException e1){
                    e1.printStackTrace();
                }
			}
		});

		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				try {
					if (socket != null)
					{
						socket.close();
						System.out.println("socket.close()");
					}
					Runtime.getRuntime().exec("adb forward --remove tcp:12580"); // 端口转换
				} catch (IOException e1) {
					e1.printStackTrace();
				}


			}
		});

		try
		{
			// adb 指令

			Runtime.getRuntime().exec("adb forward tcp:12580 tcp:10086"); // 端口转换
			Thread.sleep(3000);
		} catch (Exception e)
		{
			e.printStackTrace();

		}
		socket = null;
		try
		{
			InetAddress serveraddr = null;
			serveraddr = InetAddress.getByName("127.0.0.1");
			System.out.println("TCP 1111" + "C: Connecting...");
			socket = new Socket(serveraddr, 12580);
			String str = "hi,chenhl";
			System.out.println("TCP 221122" + "C:RECEIVE");
			out = new BufferedOutputStream(socket.getOutputStream());
            in = new BufferedInputStream(socket.getInputStream());


            while (true){
				Thread.sleep(100);
                System.out.println("接收");
				String strFormsocket = readFromSocket(in);
				if (strFormsocket != null) {

                    byte[] bytes = strFormsocket.getBytes();
                    byte[] startCursor = Arrays.copyOfRange(bytes, 0,2);
                    int startIndex = ByteUtils.bytes2Int(startCursor, 0, 2);
                   // byte[] endCursor = Arrays.copyOfRange(bytes, 2,4);
                    String result = new String(bytes,4,bytes.length-4);
                    System.out.println("startIndex:"+startIndex+" result:"+result);
                    jTextField.setText(result);
                    jTextField.setCaretPosition(startIndex);
					/*if (strFormsocket.equals("send")) { //获取焦点

						//if (true) {//如果不是同一个文本框
						//	jTextField.setText("");
						//}
						//jTextField.getDocument().addDocumentListener(this);
					} else if (strFormsocket.equals("cancelsend")) {
						//jTextField.getDocument().removeDocumentListener(this);
					}*/
				}
			}



		} catch (UnknownHostException e1)
		{
			System.out.println("TCP 331133" + "ERROR:" + e1.toString());
		} catch (Exception e2)
		{
			System.out.println("TCP 441144" + "ERROR:" + e2.toString());
		}
	}

	public static void main(String[] args)
	{



		testPcClient testPcClient = new testPcClient();

	}

	/* 从InputStream流中读数据 */
	public static String readFromSocket(InputStream in)
	{
		int MAX_BUFFER_BYTES = 4000;
		String msg = "";
		byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
		try
		{
			int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
			if (numReadedBytes >= 0) {
				msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
				tempbuffer = null;
			}


		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return msg;
	}


	public  void addChangeListener(JTextComponent text, ChangeListener changeListener) {
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});

			}
		};
		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document)e.getOldValue();
			Document d2 = (Document)e.getNewValue();
			if (d1 != null) d1.removeDocumentListener(dl);
			if (d2 != null) d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = text.getDocument();
		if (d != null) d.addDocumentListener(dl);
	}

}
