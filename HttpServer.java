import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HttpServer {
  private ServerSocket serverSocket = null;

  //Initialize the server to have the same port as the client
  public HttpServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  //Set up a thread with ClientConnectionHandler when server is connected to
  public void handleRequests() throws IOException {
    System.out.println("Listening for requests...");

    while (true) {
      Socket clientSocket = serverSocket.accept();
      System.out.println("Accepted connection : " + clientSocket);
      ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket);
      Thread handlerThread = new Thread(handler);
      handlerThread.start();
    }
  }

  public static void main(String args[]) throws IOException {
    int port = 1234;

    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }

    try {
      HttpServer server = new HttpServer(port);
      server.handleRequests();
    } 
	catch (IOException e) {
      e.printStackTrace();
    }
  }
}
