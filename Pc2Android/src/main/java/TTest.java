import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TTest {

    public static void main(String[] args){
        byte[] bytes = ByteUtils.int2Bytes(2, 2);
        byte[] bytes1 = new byte[3];
        int i = ByteUtils.bytes2Int(bytes, 0, 2);
        byte[] bytes2 = ByteUtils.byteMergerAll(bytes, bytes1);
        System.out.println("1:"+i);
        JFrame jFrame = new JFrame();
        jFrame.setPreferredSize(new Dimension(400,200));
        JTextArea jTextField = new JTextArea();
        jTextField.setPreferredSize(new Dimension(100,100));
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                System.out.println("插入:"+jTextField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                System.out.println("删除:"+jTextField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println("更新:"+jTextField.getText());
            }
        });
        jFrame.getContentPane().add(jTextField);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
