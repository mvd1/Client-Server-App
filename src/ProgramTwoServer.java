import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Contains all functionality for the server. Allows the user to enter a port number to create a connection,
 * then listens for a connection from the client. One a client connection has been established, the server
 * starts a thread to handle the connection. This allows for multiple clients to connect to the server at
 * the same time.
 */
public class ProgramTwoServer {

    public static void main(String[] args) {

        final int SERVER_PORT;
        Scanner scan = new Scanner(System.in); // used to read in user input
        System.out.print("Welcome to the server program. Please enter the port number you would like to use to" +
                " start the server: ");

        while (!scan.hasNextInt()) { // ensures valid user input
            System.out.println("Invalid input. Please enter a valid integer port number: ");
            scan.nextLine();
        }
        SERVER_PORT = scan.nextInt(); // user enters the port number they would like to host the server on
        // server attempts to communicate with the client
        talkToClient(SERVER_PORT);
    }//end main

    public static void talkToClient(int port) {
        int clientNumber = 0;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on " + new Date() + ".");
            //listen for new connection request
            while(true) {
                Socket socket = serverSocket.accept();
                clientNumber++;  //increment client num

                //Find client's host name
                //and IP address
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("Connection from client " +
                        clientNumber);
                System.out.println("\tHost name: " +
                        inetAddress.getHostName());
                System.out.println("\tHost IP address: "+
                        inetAddress.getHostAddress());


                //create and start new thread for the connection, adds to an ArrayList
                ClientHandler ch = new ClientHandler(clientNumber, socket, serverSocket);
                ch.start();

            }//end while
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }//end createMultithreadedCommunicationLoop
}
