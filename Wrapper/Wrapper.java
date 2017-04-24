package Wrapper;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;


public class Wrapper {

  private static ServerSocket wrapperSocket = null;
  private static Socket agentSocket = null;
  
  // This wrapper agent can accept up to maxAgentCount connections.
  private static final int maxAgentCount = 10;
  private static final AgentThread[] agentThreads = new AgentThread[maxAgentCount];
  public static ArrayList serviceList = new ArrayList<String>();;

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 5680;
    if (args.length < 1) {
      System.out.println("Hi !! I am wrapper DoubleO7!! A Multi-Agent System Design Pattern \n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    //update available sevice list here
    serviceList.add("divisors");
    try {
      wrapperSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a agent socket for each connection and pass it to a new agent Thread.
     */
    while (true) {
      try {
        agentSocket = wrapperSocket.accept();
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
  static String getQuery(int i, String q){
      String result = null;
      LegacyCode la = new LegacyCode();
      switch(i){
          case 0:
              result = la.getDivisors(q);
              break;
      }
      return result;
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
      
      name = is.readLine();
      System.out.println("--> A new Client agent reported : "+name);
      
      /* Welcome the new  Agent. */
      os.println("--> Welcome  Client " + name + " !! I am wrapper DoubleO7.");
      os.println("--> To See how many services we have type # \n--> To quit type /quit in a new line.");
      
      /* Handle communication : Wrapper mechanism : provide services to client through wrapper*/
      while (true) {
        String line = is.readLine();

        if (line.startsWith("/quit")) {        
          break;
        }
        if(line.startsWith("#")){
            System.out.println("--> Client asking for service list : "+name);
            os.println("--> Thank You!! We have following services in wrapper");
            for (Iterator it = Wrapper.serviceList.iterator(); it.hasNext();) {
                String service = (String) it.next();
                os.println(service);
            }
            os.println("--> To use a service enter   <service_name><space><query>");
            continue;
        }
        
        String[] words = line.split("\\s", 2);
        if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
              System.out.println("--> " + name + " requests : " + words[0] + " " + words[1]); 
              int i = Wrapper.serviceList.indexOf(words[0]);
              if(i == -1){
                os.println("--> Sorry.. We only have following services");
                for (Iterator it = Wrapper.serviceList.iterator(); it.hasNext();) {
                    String service = (String) it.next();
                    os.println(service);
                }
                os.println("--> To use a service enter   <service_name><space><query>");
              }else{
                  os.println(Wrapper.getQuery(i, words[1]));
              }
              
            }
          }else{
              os.println("Query is empty");
          }
        }else{
            os.println("Invalid Query");
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