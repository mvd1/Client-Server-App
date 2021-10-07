import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Contains all of the functionalities to be performed by the client. Accepts a port number as input in the main program
 * and then connects to the server.
 */
public class ProgramTwoClient {

    public static void main(String[] args) {

        final int SERVER_PORT;
        Scanner scan = new Scanner(System.in); // used to read user's given port number
        System.out.print("This is the client program. Please enter the server port you used to start the server in order" +
                " to create a connection with the client: ");

        while (!scan.hasNextInt()) { // ensures valid user input
            System.out.println("Invalid input. Please enter a valid integer port number: ");
            scan.nextLine();
        }
        SERVER_PORT = scan.nextInt(); // port number given by user

        DataOutputStream toServer;
        DataInputStream fromServer;
        //Scanner input = new Scanner(System.in);
        String command;

        //attempt to connect to the server
        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

            while(true) {
                Scanner scan2 = new Scanner(System.in);
                System.out.print("\nPlease enter a command followed by the requisite information: ");
                command = scan2.nextLine();
                toServer.writeUTF(command);

                while(true) {
                    //received message:
                    command = fromServer.readUTF();
                    if (command.equals("end")) {
                        break;
                    }
                    if(command.equalsIgnoreCase("QUIT")) {
                        socket.close();
                        fromServer.close();
                        toServer.close();
                        System.exit(3);
                    }
                    System.out.println(command);
                }
            }

        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
    }//end main
}