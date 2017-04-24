package Monitor;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;


public class Monitor {

  private static ServerSocket monitorSocket = null;
  private static Socket agentSocket = null;

  // This Monitor can accept up to maxAgentCount connections.
  private static final int maxAgentCount = 10;
  private static final AgentThread[] agentThreads = new AgentThread[maxAgentCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 5675;
    if (args.length < 1) {
      System.out.println("Hi !! I am monitor DoubleO7!! A Multi-Agent System Design Pattern \n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    
    try {
      monitorSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a agent socket for each connection and pass it to a new agent Thread.
     */
    while (true) {
      try {
        agentSocket = monitorSocket.accept();
        int i = 0;
        for (i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] == null) {
            (agentThreads[i] = new AgentThread(agentSocket, agentThreads)).start();
            break;
          }
        }
        if (i == maxAgentCount) {
          PrintStream os = new PrintStream(agentSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          agentSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * This agent thread opens the input and the output
 * streams for a particular client or service provider, ask the it's name, informs monitor
 * about the fact that a new agent has joined and as long as it receive data, it process
 * data in synchronisation. This is also responsible for quiting the agent
 */
class AgentThread extends Thread {

  private String agentName = null;
  private String agentDesc = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket agentSocket = null;
  private final AgentThread[] agentThreads;
  private ArrayList subscriberList = null;
  private final int maxAgentCount;

  public AgentThread(Socket agentSocket, AgentThread[] agentThreads) {
    this.agentSocket = agentSocket;
    this.agentThreads = agentThreads;
    this.subscriberList = new ArrayList<String>();
    maxAgentCount = agentThreads.length;
  }

  public void run() {
    int maxAgentCount = this.maxAgentCount;
    AgentThread[] agentThreads = this.agentThreads;

    try {
      /*
       * Create input and output streams for this agent.
       */
      is = new DataInputStream(agentSocket.getInputStream());
      os = new PrintStream(agentSocket.getOutputStream());
      String name;
      String desc;
      while (true) {
        os.println("--> To register with Monitor Agent enter Agent name.");
        desc = is.readLine().trim();
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("--> The name should not contain '@' or '-' character.");
        }
      }

      /* Welcome the new  Agent. */
      os.println("--> Welcome " + name
          + "!! I am Monitor DoubleO7.\n");
      if(desc.equals("Subscriber")){
        os.println("--> To See how many Subject we have type # \n--> To leave type /quit in a new line.");
      }else{
        os.println("--> Thanks for registering " + name +". \n--> To leave type /quit in a new line."); 
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] == this) {
            agentName = name;
            agentDesc = desc;
            System.out.println("--> A new " + desc + " is registered : "+name);
            break;
          }
        }
 
      }
      /* Handle communication : Monitor mechanism : Notify the subscriber*/
      while (true) {
        String line = is.readLine();
        
        if (line.startsWith("/quit")) {
          break;
        }
        
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxAgentCount; i++) {
                  if (agentThreads[i] != null && agentThreads[i] != this
                      && agentThreads[i].agentName != null && !agentThreads[i].agentDesc.equals(desc)) {
                      if(desc.equals("Subject")){
                          if(subscriberList.contains(agentThreads[i].agentName)){
                              agentThreads[i].os.println("@" + name + " " + words[1]);
                              System.out.println("--> The subscriber " + agentThreads[i].agentName + " is notified for changes in subject " +name);
                          }
                       
                      }else{
                          if(agentThreads[i].agentName.equals(words[0].substring(1))){
                               agentThreads[i].subscriberList.add(name);
                               System.out.println("--> The subscriber " + name + " is subscribed for subject " + agentThreads[i].agentName);
                          }
                      }
                    //agentThreads[i].os.println("@" + name + " " + words[1]);
                    //break;
                  }
                }
              }
            }
          }
        } else {
            if (line.startsWith("#") && this.agentDesc.equals("Subscriber")) {
                          /* inform about services available */
                synchronized (this) {
                  os.println("--> We have following subjects available");
                  int l = 0;
                  for (int i = 0; i < maxAgentCount; i++) {
                    if (agentThreads[i] != null && agentThreads[i].agentDesc.equals("Subject") && agentThreads[i].agentName != null) {
                      os.println(agentThreads[i].agentName);
                      l++;
                    }
                  }
                  os.println("--> "+l+" subject(s)");
                  os.println("--> To use a subscribe a subject enter   @<subject_name><space>register");
                }
                
            }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] != this
              && agentThreads[i].agentName != null) {
            agentThreads[i].os.println("--> The  "+ desc + " " + name
                + " is quited !!!");
          }
        }
      }
      os.println("Bye " + name + " *** cheeku ");
   
      //Clean up. Set the current thread variable to null so that a new agent     
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] == this) {
            agentThreads[i] = null;
          }
        }
      }
      
      is.close();
      os.close();
      agentSocket.close();
    } catch (IOException e) {
    }
  }
}
