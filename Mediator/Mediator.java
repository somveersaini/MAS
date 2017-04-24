package Mediator;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


public class Mediator {

  private static ServerSocket mediatorSocket = null;
  private static Socket agentSocket = null;

  // This mediator can accept up to maxAgentCount connections.
  private static final int maxAgentCount = 10;
  private static final AgentThread[] agentThreads = new AgentThread[maxAgentCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 5675;
    if (args.length < 1) {
      System.out.println("Hi !! I am Mediator DoubleO7!! A Multi-Agent System Design Pattern \n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    
    try {
      mediatorSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a agent socket for each connection and pass it to a new agent Thread.
     */
    while (true) {
      try {
        agentSocket = mediatorSocket.accept();
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
 * streams for a particular client or service provider, ask the it's name, informs broker
 * about the fact that a new agent has joined and as long as it receive data, it process
 * data in synchronisation. This is also responsible for unregistering the agent
 */
class AgentThread extends Thread {

  private String agentName = null;
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
      while (true) {
        os.println("--> To register with local broker enter Agent name.");
        
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("--> The name should not contain '@' or '-' character.");
        }
      }

      /* Welcome the new  Agent. */
      os.println("--> Welcome " + name + "!! I am Mediator DoubleO7.\n");
      os.println("--> To See how many services we have type # \n--> To leave type /quit in a new line.");
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] == this) {
            agentName = name;
            System.out.println("--> A new ColleagueAgent is registered : " + name);
            break;
          }
        }
        
//        for (int i = 0; i < maxAgentCount; i++) {
//          if (agentThreads[i] != null && agentThreads[i] != this) {
//            agentThreads[i].os.println("*** A new client " + name
//                + " is registered in the Multi-Agent System !!! ***");
//          }
//        }
      }
      /* Handle communication : Broker mechanism : report to proper reciever*/
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
                  
            //System.out.println(words[0].substring(1));
                for (int i = 0; i < maxAgentCount; i++) {
                  if (agentThreads[i] != null && agentThreads[i] != this
                      && agentThreads[i].agentName != null 
                      && agentThreads[i].agentName.equals(words[0].substring(1))) {
                      System.out.println("--> The agent " + name + " query : to " + words[0] + " is -> " + words[1]);
                    agentThreads[i].os.println("@" + name + " " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        }
        if (line.startsWith("$")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxAgentCount; i++) {
                  if (agentThreads[i] != null && agentThreads[i] != this
                      && agentThreads[i].agentName != null 
                      && agentThreads[i].agentName.equals(words[0].substring(1))) {
                      System.out.println("--> The agent " + name + " reply : to " + words[0] + " is -> " + words[1]);
                    agentThreads[i].os.println("$" + name + " " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        }else {
            if (line.startsWith("#")) {
                          /* inform about services available */
                synchronized (this) {
                  os.println("--> We have following ColleagueAgent registered");
                  int l = 0;
                  for (int i = 0; i < maxAgentCount; i++) {
                    if (agentThreads[i] != null && agentThreads[i].agentName != null) {
                      os.println(agentThreads[i].agentName);
                      l++;
                    }
                  }
                  os.println("--> "+l+" ColleagueAgent(s)");
                  os.println("--> To use a ColleagueAgent service enter   @<ColleagueAgent_name><space><query>");
                }
                
            }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] != this
              && agentThreads[i].agentName != null) {
            agentThreads[i].os.println("--> The  agent " + name  + " is unregistered !!!");
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