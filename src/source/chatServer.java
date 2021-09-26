import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.BevelBorder;

public class chatServer implements Runnable{
    //connection status parameters
    public final static int NULL = 0;
    public final static int DISCONNECTED = 1;
    public final static int DISCONNECTING = 2;
    public final static int BEGIN_CONNECT = 3;
    public final static int CONNECTED = 4;

    //status messages
    public final static String statusMessages[] = {
        " Error! Could not connect!", " Disconnected",
        " Disconnecting...", " Connecting...", " Server ON"
    };
    public final static chatServer chatobj = new chatServer();
    public final static String END_CHAT_SESSION = Character.toString('0'); // Indicates the end of a session

    //connected user count and global system meta data
    public static Set<String> names = new HashSet<>();
    public static Set<PrintWriter> writers = new HashSet<>();
    public static int counter = 0;
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


    //TCP socket and thread parameters
    public static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static ServerSocket listener = null;
    public static Socket socket = null;
    public static Scanner in = null;
    public static PrintWriter out = null;


    //GUI initiation
    private static void initGUI()
    {
        fr = new JFrame("Server Window");
        pane = new JPanel(gbl);

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


        fr.add(pane);
        fr.add(statuspane, BorderLayout.SOUTH);
        fr.setSize(300, 200);
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
               changeStatusTS(CONNECTED, true);
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
   private static void cleanUp() {
      try {
         if (listener != null) {
            listener.close();
            listener = null;
         }
      }
      catch (IOException e) { listener = null; }

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
         //statuslb.setText("Disconnected");
         tf1.setEnabled(true);
         tf2.setEnabled(true);
         statusColor.setBackground(Color.RED);
         break;

      case DISCONNECTING:
         btn1.setEnabled(false);
         btn2.setEnabled(false);
         statusColor.setBackground(Color.ORANGE);
         //statuslb.setText("Disconnecting");
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         break;

      case CONNECTED:
         btn1.setEnabled(false);
         btn2.setEnabled(true);
         statusColor.setBackground(Color.GREEN);
         //statuslb.setText("Server ON");
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         break;

      case BEGIN_CONNECT:
         btn1.setEnabled(false);
         statusColor.setBackground(Color.ORANGE);
         //statuslb.setText("Connecting");
         tf1.setEnabled(false);
         tf2.setEnabled(false);
         break;
      }

      statuslb.setText(statusString);
      fr.repaint();
   }



   // The main method
   public static void main(String args[]) {
      int port = 59001;
      String name = "";

      initGUI();
      while (true) {
         try { // Poll every ~10 ms
            listener = new ServerSocket(port);
            Thread.sleep(10);
         }
         catch (InterruptedException | IOException e) {}

         switch (connectionStatus) {
         case CONNECTED:
            try 
            {
               socket = listener.accept();
               pool.execute(new Runnable(){
                  public void run() 
                  {
                     chatterName(in, out, name, socket);
                  }
               });
         }
            // If error, clean up and output an error message
            catch (IOException e) {
               cleanUp();
               changeStatusTS(DISCONNECTED, false);
            }
            break;
         default: 
            break; // do nothing
        }
      }
   }

   static void chatterName(Scanner in, PrintWriter out, String name, Socket socket)
   {
      try
      {
         in = new Scanner(socket.getInputStream());
         out = new PrintWriter(socket.getOutputStream(), true);

         //keep asking a name util it is unique among other chatters
         while(true)
         {
            out.println("SUBMIT A NAME");
            name = in.nextLine();
            if(name == null)
            {
               in.close();
               return;
            }
            synchronized(names)
            {
                  if(!name.isBlank() && !names.contains(name))
                  {
                     names.add(name);
                     break;
                  }
            }
         }
         out.println("NAME ACCEPTED " + name);
         for(PrintWriter writer: writers)
         {
            writer.println("System " + name + " has joined");
         }
         writers.add(out);
         counter = names.size();
         for(PrintWriter writer: writers)
         {
            writer.println("Counter " + counter);
         }

         //Accept messages from this client and broadcast them
         while(in.hasNextLine())
         {
            String input = in.nextLine();
            if(input.toLowerCase().startsWith("/quit"))
            {
               in.close();
               return;
            }
            for(PrintWriter writer: writers)
                  writer.println("MESSAGE " + name + ": "+input);
         }
      }
      catch(Exception e)
      {
         System.out.println(e);
      }
      finally
      {
         if(out != null)
         {
            writers.remove(out);
         }
         if(name != null)
         {
            //System.out.println(name + " is leaving");
            names.remove(name);
            counter = names.size();
            for(PrintWriter writer: writers)
            {
                  writer.println("System "+name+" has left");
                  writer.println("Counter " + counter);
            }
         }
         try
         {
            socket.close();
         }
         catch(IOException e)
         {

         }
      }
   }
}
