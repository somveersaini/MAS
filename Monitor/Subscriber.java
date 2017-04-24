package Monitor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Samsaini
 */
public class Subscriber implements Runnable {

  private static Socket clientSocket = null;  
  private static PrintStream os = null; // The output stream
  private static DataInputStream is = null;  // The input stream
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) {

      // The default port and host.
    int portNumber = 5675;
    String host = "localhost";

    if (args.length < 2) {
      System.out
          .println("-->Usage: Subscriber <host> <portNumber>\n"
              + "-->Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    
     // Open a socket on a given host and port. Open input and output streams.
     
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("-->Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("-->Couldn't get I/O for the connection to the monitor -> "
          + host);
    }

    if (clientSocket != null && os != null && is != null) {
      try {
        /* Create a thread to read from the monitor. */
        new Thread(new Subscriber()).start();
        os.println("Subscriber");
        while (!closed) {
          os.println(inputLine.readLine().trim());
        }       
        //* Close the output stream, close the input stream, close the socket.         
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  //SubscriberAgent side actions
  public void run() {
    /*
     * Keep on reading from the socket till we receive "Bye" from the monitor.
     */
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("Bye") != -1){
          System.out.println("Unregistered");
          break;
        }
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}