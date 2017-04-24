package Broker;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


public class Broker {

  private static ServerSocket brokerSocket = null;
  private static Socket agentSocket = null;

  // This Broker can accept up to maxAgentCount connections.
  private static final int maxAgentCount = 10;
  private static final AgentThread[] agentThreads = new AgentThread[maxAgentCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 5675;
    if (args.length < 1) {
      System.out.println("Hi !! I am broker DoubleO7!! A Multi-Agent System Design Pattern \n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    
    try {
      brokerSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a agent socket for each connection and pass it to a new agent Thread.
     */
    while (true) {
      try {
        agentSocket = brokerSocket.accept();
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
 * This agent thread opens the input and the output streams for a particular client
 * or service provider, ask the it's name and as long as it receive data, it process
 * data in synchronisation. This is also responsible for unregistering the agent
 */
class AgentThread extends Thread {

  private String agentName = null;
  private String agentDesc = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket agentSocket = null;
  private final AgentThread[] agentThreads;
  private final int maxAgentCount;

  public AgentThread(Socket agentSocket, AgentThread[] agentThreads) {
    this.agentSocket = agentSocket;
    this.agentThreads = agentThreads;
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
        os.println("--> To register with local broker enter Agent name.");
        desc = is.readLine().trim();
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("--> The name should not contain '@' or '-' character.");
        }
      }

      /* Welcome the new  Agent. */
      os.println("--> Welcome " + name + "!! I am Broker DoubleO7.\n");
      if(desc.equals("client")){
        os.println("--> To See how many services we have type # \n--> To Unregister type /unregister in a new line.");
      }else{
        os.println("--> Thanks for providing Your service " + name +". \n--> To Unregister type /unregister in a new line."); 
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] == this) {
            agentName = "@" + name;
            agentDesc = desc;
            System.out.println("--> A new " + desc + " is registered : "+name);
            break;
          }
        }
      }
      /* Handle communication : Broker mechanism : report to proper reciever*/
      while (true) {
        String line = is.readLine();
        
        if (line.startsWith("/unregister")) {  
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
                      && agentThreads[i].agentName != null && !agentThreads[i].agentDesc.equals(desc)
                      && agentThreads[i].agentName.equals(words[0])) {
                      if(desc.equals("client"))
                       System.out.println("--> The client " + name + " is requesting for service : "+ words[0]  + " query -> " + words[1]);
                      else{
                       System.out.println("--> The service " + name + " reply for client : " + words[0] + " is -> " + words[1]);
                      }
                    agentThreads[i].os.println("@" + name + " " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        } else {
            if (line.startsWith("#") && this.agentDesc.equals("client")) {
                          /* inform about services available */
                synchronized (this) {
                  os.println("--> We have following service registered");
                  int l = 0;
                  for (int i = 0; i < maxAgentCount; i++) {
                    if (agentThreads[i] != null && agentThreads[i].agentDesc.equals("service") && agentThreads[i].agentName != null) {
                      os.println(agentThreads[i].agentName.substring(1));
                      l++;
                    }
                  }
                  os.println("--> "+l+" service(s)");
                  os.println("--> To use a service enter   @<service_name><space><query>");
                }
                
            }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] != this
              && agentThreads[i].agentName != null) {
            agentThreads[i].os.println("--> The  "+ desc + " " + name
                + " is unregistered !!!");
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