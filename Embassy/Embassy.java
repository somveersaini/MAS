package Embassy;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Iterator;


public class Embassy {

  private static ServerSocket embassySocket = null;
  private static Socket agentSocket = null;

  // This Embassy can accept up to maxAgentCount connections.
  private static final int maxAgentCount = 10;
  private static final AgentThread[] agentThreads = new AgentThread[maxAgentCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 5675;
    if (args.length < 1) {
      System.out.println("Hi !! I am Embassy DoubleO7!! A Multi-Agent System Design Pattern \n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    
    try {
      embassySocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a agent socket for each connection and pass it to a new agent Thread.
     */
    while (true) {
      try {
        agentSocket = embassySocket.accept();
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
  private int permission = 0;
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
        desc = is.readLine().trim();
        //if(desc.equals("LOCAL"))
        os.println("--> Please enter Agent name.");
        
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("--> The name should not contain '@'  character.");
        }
      }

      /* Welcome the new  Agent. */
      os.println("--> Welcome " + name + "!! I am Embassy DoubleO7.\n");
      if(desc.equals("LOCAL")){
        os.println("--> To See how many services we have type # \n--> To leave type /quit in a new line.");
      }else{
        os.println("--> Here you cant do anything without my permission.");
        os.println("--> If You want my permission type /permit.");
        os.println("--> Or you can quit by typing /quit.");
      }
      synchronized (this) {
        for (int i = 0; i < maxAgentCount; i++) {
          if (agentThreads[i] != null && agentThreads[i] == this) {
            agentName = name;
            agentDesc = desc;
            if(desc.equals("LOCAL"))
                permission = 1;
            System.out.println("--> A new " + desc + " agent is acknowledged : "+name);
            break;
          }
        }
        
      }
      /* Handle communication : Embassy mechanism : report to proper reciever*/
      while (true) {
        String line = is.readLine();
        
        if (line.startsWith("/quit")) {
          
          break;
        }
        if(permission == 0){
            if (line.startsWith("/permit")) {    
                permission = 1;
                System.out.println("--> Permission Granted to : "+name);
                os.println("--> Thank You!! We have following services");
                os.println("--> We have following agents");
                  int l = 0;
                  for (int i = 0; i < maxAgentCount; i++) {
                    if (agentThreads[i] != null && agentThreads[i].agentName != null) {
                      os.println(agentThreads[i].agentName);
                      l++;
                    }
                  }
                  os.println("--> "+l+" agent(s)");
                  os.println("--> To use a agent service enter   @<service_name><space><query>");
            }else{
                os.println("--> Here you cant do anything without my permission.");
                os.println("--> If You want my permission type /permit.");
                os.println("--> Or you can quit by typing /quit.");
                continue;
            }
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
        } else {
            if (line.startsWith("#") ) {
                          /* inform about services available */
                synchronized (this) {
                  os.println("--> We have following agents");
                  int l = 0;
                  for (int i = 0; i < maxAgentCount; i++) {
                    if (agentThreads[i] != null && agentThreads[i].agentName != null) {
                      os.println(agentThreads[i].agentName);
                      l++;
                    }
                  }
                  os.println("--> "+l+" agent(s)");
                  os.println("--> To use a agent service enter   @<service_name><space><query>");
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
      os.println("Bye " + name + " ** ");
   
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