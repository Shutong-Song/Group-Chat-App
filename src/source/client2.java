import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class client2 implements Runnable{
    //connection status parameters
    public final static int NULL = 0;
    public final static int DISCONNECTED = 1;
    public final static int DISCONNECTING = 2;
    public final static int BEGIN_CONNECT = 3;
    public final static int CONNECTED = 4;

    //status messages
    public final static String statusMessages[] = {
        " Error! Could not connect!", " Disconnected",
        " Disconnecting...", " Connecting...", " Connected"
    };
    public final static client2 chatobj = new client2();
    public final static String END_CHAT_SESSION = Character.toString('0'); // Indicates the end of a session

    //connected user count and global system meta data
    public static int connectionStatus = DISCONNECTED;
    public static String statusString = statusMessages[connectionStatus];
        
    //GUI parameters
    public static JFrame fr = null;
    public static JPanel pane = null;
    public static JLabel lb1 = null;
    public static JLabel lb2 = null;
    public static JTextField tf1 = null;
    public static JTextField tf2 = null;
    public static JButton btn1 = null;
    public static JButton btn2 = null;
    public static JPanel statuspane = null;
    public static JLabel statuslb = null;
    public static JTextField statusColor = null;
    public static GridBagLayout gbl = new GridBagLayout();
    public static GridBagConstraints c = new GridBagConstraints();
    //GUI send and receive messages section
    public static JPanel chatpane = null;
    public static JTextField tf = null; //type message field
    public static JTextPane messageArea = new JTextPane(); //show all messages field
    public static JLabel lb = null; //show current members label
    public static JScrollPane chatTextPane = null; 
    public static Font lbFont =new Font("JSL Ancient",Font.BOLD | Font.ITALIC,12);


    //TCP socket and thread parameters
    public static Socket socket = null;
    public static Scanner in = null;
    public static PrintWriter out = null;


    //GUI initiation
    private static void initGUI()
    {
        fr = new JFrame("Chatter Window");
        pane = new JPanel(gbl);
        chatpane = new JPanel(new FlowLayout());

        //**********left side connection panel ***********///
        //label 1
        lb1 = new JLabel("Server IP:");
        lb1.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 0)); //top, left, bottom, right
        c.gridx = 0;
        c.gridy = 0;
        pane.add(lb1, c);

        //label 2
        lb2 = new JLabel("Port:");
        lb2.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 0)); //top, left, bottom, right
        c.gridx = 0;
        c.gridy = 1;
        pane.add(lb2, c);

        //text field 1
        tf1 = new JTextField("localhost",10);
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,10, 0, 0); //top, left, bottom, right
        pane.add(tf1, c);

        //text field 2
        tf2 = new JTextField("59001", 10);
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(0,10, 0, 0); //top, left, bottom, right
        pane.add(tf2, c);

        //button 1 connect button
        btn1 = new JButton("Connect");
        btn1.setBounds(30,40,200,100);
        btn1.addActionListener(new ButtonListener());
        btn1.setEnabled(true);
        c.gridx = 0;
        c.gridy = 2;
        pane.add(btn1, c);

        //button 2 disconnect button
        btn2 = new JButton("Disconnect");
        btn2.addActionListener(new ButtonListener());
        btn2.setEnabled(false);
        c.gridx = 1;
        c.gridy = 2;
        pane.add(btn2, c);

        //**********right side connection panel ***********///
        //lb for displaying current members in the group
        lb = new JLabel(" ", SwingConstants.CENTER);
        chatpane.add(lb, BorderLayout.NORTH);

        //messageArea for display messages from everyone
        messageArea.setEditable(false);
        messageArea.setPreferredSize(new Dimension(200, 230));
        chatTextPane = new JScrollPane(messageArea,
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatpane.add(chatTextPane, BorderLayout.CENTER);

        // textfield for compose your message
        tf = new JTextField(20);
        //send on enter action and clear field for next message
        tf.addActionListener(new ActionListener()
        {
                public void actionPerformed(ActionEvent e)
                {
                    out.println(tf.getText());
                    tf.setText("");
                }
        });
        chatpane.add(tf, BorderLayout.SOUTH);


        //**********bottom status pane ***********///
        //status pane
        statuspane = new JPanel();
        statuspane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statuspane.setPreferredSize(new Dimension(fr.getWidth(), 20));
        statuspane.setLayout(new BoxLayout(statuspane, BoxLayout.X_AXIS));
        statusColor = new JTextField("");
        statuslb = new JLabel();
        statuslb.setText(statusMessages[DISCONNECTED]);
        statusColor.setHorizontalAlignment(SwingConstants.LEFT);
        statuspane.add(statusColor);
        statuspane.add(statuslb);


        fr.add(pane, BorderLayout.WEST);
        fr.add(chatpane, BorderLayout.CENTER);
        fr.add(statuspane, BorderLayout.SOUTH);
        fr.setSize(460, 350);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setVisible(true);
    }


    //button listener
   static class ButtonListener implements ActionListener
   {
       public void actionPerformed(ActionEvent e)
       {
           if(e.getActionCommand().equals("Connect"))
           {
               changeStatusTS(BEGIN_CONNECT, true);
           }
           else
           {
               changeStatusTS(DISCONNECTED, true);
           }
       }
   }

   //change status of the GUI
   private static void changeStatusTS(int newConnectStatus, boolean noError) {
      // Change state if valid state
      if (newConnectStatus != NULL) {
         connectionStatus = newConnectStatus;
      }

      // If there is no error, display the appropriate status message
      if (noError) {
         statusString = statusMessages[connectionStatus];
      }
      // Otherwise, display error message
      else {
         statusString = statusMessages[NULL];
      }

      // Call the run() routine (Runnable interface) on the
      // error-handling and GUI-update thread
      SwingUtilities.invokeLater(chatobj);
   }



   // Cleanup for disconnect
   private static void cleanUp(Socket socket, Scanner in, PrintWriter out) {
      try {
         if (socket != null) {
            socket.close();
            socket = null;
         }
      }
      catch (IOException e) { socket = null; }

      try {
         if (in != null) {
            in.close();
            in = null;
         }
      }
      catch (Exception e) { in = null; }

      if (out != null) {
         out.close();
         out = null;
      }
   }

   /////////////////////////////////////////////////////////////////

   // Checks the current state and sets the enables/disables
   // accordingly
   public void run() {
      switch (connectionStatus) {
      case DISCONNECTED:
         btn1.setEnabled(true);
         btn2.setEnabled(false);
         statusColor.setBackground(Color.RED);
         tf1.setEnabled(true);
         tf2.setEnabled(true);
         tf.setEnabled(false);
         messageArea.setText("");
         lb.setText(" ");
         cleanUp(socket, in, out);
         break;

      case DISCONNECTING:
         btn1.setEnabled(false);
         btn2.setEnabled(false);
         statusColor.setBackground(Color.ORANGE);
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         tf.setEnabled(false);
         break;

      case CONNECTED:
         btn1.setEnabled(false);
         btn2.setEnabled(true);
         statusColor.setBackground(Color.GREEN);
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         tf.setEnabled(true);

         break;

      case BEGIN_CONNECT:
         btn1.setEnabled(false);
         statusColor.setBackground(Color.ORANGE);
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         tf.setEnabled(false);
         break;
      }

      statuslb.setText(statusString);
      fr.repaint();
   }

   private static String getName()
   {
       return JOptionPane.showInputDialog(fr, "Choose a screen name", "Screen name selection",
              JOptionPane.PLAIN_MESSAGE);
   }


   // The main method
   public static void main(String args[]) {
      String usedName = "";

      initGUI();
            while (true) {
               try { // Poll every ~10 ms
                  Thread.sleep(10);
               }
               catch (InterruptedException e) {}

               switch (connectionStatus) {
               case BEGIN_CONNECT:
                  try 
                  {
                    int port = Integer.parseInt(String.valueOf(tf2.getText()));
                    String host = String.valueOf(tf1.getText());
                    socket = new Socket(host, port);
                    in = new Scanner(socket.getInputStream());
                    out = new PrintWriter(socket.getOutputStream(), true);
                    changeStatusTS(CONNECTED, true);
                  }
                  // If error, clean up and output an error message
                  catch (IOException e) {
                     cleanUp(socket, in, out);
                     changeStatusTS(DISCONNECTED, false);
                  }
                  break;

               case CONNECTED:
                  try
                  {
                    //design send and received text format
                    SimpleAttributeSet left = new SimpleAttributeSet();
                    StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
                    StyleConstants.setForeground(left, Color.BLACK);

                    SimpleAttributeSet right = new SimpleAttributeSet();
                    StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
                    StyleConstants.setForeground(right, Color.BLACK);

                    SimpleAttributeSet middle = new SimpleAttributeSet();
                    StyleConstants.setAlignment(middle, StyleConstants.ALIGN_CENTER);
                    StyleConstants.setForeground(middle, Color.BLUE);

                    StyledDocument doc = messageArea.getStyledDocument();

                    while(in.hasNextLine())
                    {
                        var line = in.nextLine();
                        if(line.startsWith("Counter"))
                        {
                            String[] words = line.split(" ");
                            String lastWord = words[words.length - 1];
                            lb.setFont(lbFont);
                            lb.setText("Current members: " + lastWord);
                        }
                        if(line.startsWith("SUBMIT A NAME"))
                        {
                            out.println(getName());
                        }
                        else if(line.startsWith("NAME ACCEPTED"))
                        {
                            fr.setTitle("Chatter - " + line.substring(13));
                            usedName = line.substring(14);
                            tf.setEditable(true);
                        }
                        else if(line.startsWith("MESSAGE"))
                        {
                            String senderName = line.substring(8).split(":")[0];
                            if(senderName.equals(usedName))
                            {
                                doc.insertString(doc.getLength(), line.substring(8+usedName.length()+2) + "\n", right);
                                doc.setParagraphAttributes(doc.getLength()-1,doc.getLength(), right, false);
                            }
                            else
                            {
                                doc.insertString(doc.getLength(), line.substring(8) + "\n", left);
                                doc.setParagraphAttributes(doc.getLength()-1, doc.getLength(), left, false);
                            }
                        }
                        else if(line.startsWith("System"))
                        {
                            doc.insertString(doc.getLength(), line.substring(7) + "\n", middle);
                            doc.setParagraphAttributes(doc.getLength()-1, doc.getLength(), middle, false);
                        }
                    }
                  }
                  catch(Exception e)
                  {
                      cleanUp(socket, in, out);
                      changeStatusTS(DISCONNECTED, false);
                  }

               case DISCONNECTED:
                  try
                  {
                     cleanUp(socket, in, out);
                  }
                  catch(Exception e)
                  {
                  }
                  break;
               
               default: 
                  break; // do nothing
                  }                                                                                                                                                                           
            }
   }

}
