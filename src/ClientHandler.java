
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles all of the main functionality of the program; reads and writes commands, updates the address book and users logged in. Each
 * command (except SHUTDOWN) is separated into its own method. An extra method called writeToDisk handles writing the address book data in memory
 * to the text file.
 */
public class ClientHandler extends Thread {

    private Socket socket;  // connected socket
    private ServerSocket serverSocket;  // server's socket
    private int clientNumber; // number of client connected
    private Record record; // record to be placed in address book
    private File file; // opens the file AddressBook.txt, where records are stored
    private FileWriter writeToFile; // used to handle data written to the file
    private Scanner readFromFile; // used to handle data read from the file
    private boolean clientLoggedIn; // keeps track of whether the client is properly logged in to an account
    private  ArrayList<User> userList; // list of all users who can potentially log in
    private static ArrayList<Record> addressBook; // in-memory data structure to contain all of the records
    private static int recordCount; // total count of all records stored in the in-memory addressBook
    private static User root; // user root
    private static User john; // user john
    private static User david; // user david
    private static User mary; // user mary

    /*
   Constructor, where all of the above fields are initialized and any data that is already stored in the
   text file AddressBook.txt is placed in memory.
   */
    public ClientHandler(int clientNumber, Socket socket, ServerSocket serverSocket) {
        this.socket = socket;
        this.serverSocket = serverSocket;
        this.clientNumber = clientNumber;
        file = new File("AddressBook.txt");
        try {
            readFromFile = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addressBook = new ArrayList<>(20);
        userList = new ArrayList<>(4);
        recordCount = 0;
        clientLoggedIn = false;

        // Data already stored in on-disk memory is read into the ArrayList in this while loop.
        while(readFromFile.hasNext() && recordCount <= 19) {
            String id = readFromFile.next();
            String firstName = readFromFile.next();
            String lastName = readFromFile.next();
            String phoneNumber = readFromFile.next();
            record = new Record(id, firstName, lastName, phoneNumber);
            addressBook.add(recordCount, record);
            recordCount++;
        }

        userList.add(root = new User("root", "root01"));
        userList.add(john = new User("john", "john01"));
        userList.add(david = new User("david", "david01"));
        userList.add(mary = new User("mary", "mary01"));


    }//end ClientHandler

    //run() method is required by all
    //Runnable implementers
    @Override
    public void run() {
        //run the thread in here
        try {
            DataInputStream fromClient = new DataInputStream(socket.getInputStream());
            DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
            writeToFile = new FileWriter("AddressBook.txt");
            String[] split; // used to split the data sent from the client based on whitespace, where the value at index 0 will be checked as the command to determine an action
            boolean serveClient = true; // allows the ClientHandler to serve the client, when the user enters the quit command serveClient is set to false and client will disconnect

            //continuously serve the client
            while(serveClient) {
                split = fromClient.readUTF().split(" "); // splits the data sent by the client, and stores in array
                System.out.println("\n\t[[Command " + split[0].toUpperCase() +
                        " received from client " + clientNumber +"]]");

                switch (split[0].toUpperCase()) { // if command is written as lowercase, automatically converted to uppercase

                    case "ADD":
                        if (!clientLoggedIn) {
                            toClient.writeUTF("s: 401 You are not currently logged in, login first.");
                        }
                        else add(split, toClient);
                        break;

                    case "DELETE":
                        if (!clientLoggedIn) {
                            toClient.writeUTF("s: 401 You are not currently logged in, login first.");
                        }
                        else delete(split, toClient);
                        break;

                    case "LIST":
                        list(toClient);
                        break;

                    case "QUIT":
                        quit(toClient);
                        serveClient = false; // will cause an exit from the while loop to shutdown the client
                        break;

                    /*
                    Performs the SHUTDOWN command, written simply as SHUTDOWN. The writeToDisk method is called, and all
                    sockets and I/O streams are closed. Then, the program is terminated.
                     */
                    case "SHUTDOWN":
                        if (!clientLoggedIn) {
                            toClient.writeUTF("s: 401 You are not currently logged in, login first.");
                        }
                        else {
                            if (!root.getLoginStatus()) {
                                toClient.writeUTF("402 User not allowed to execute this command.");
                            } else {
                                toClient.writeUTF("s: 200 OK");
                                toClient.writeUTF("QUIT");
                                writeToDisk();
                                socket.close();
                                serverSocket.close();
                                toClient.close();
                                fromClient.close();
                                writeToFile.close();
                                readFromFile.close();
                                System.exit(2);
                            }
                        }

                    case "LOGIN":
                        login(split, toClient);
                        break;

                    case "LOGOUT":
                        logout(split[0], toClient);
                        break;

                    case "WHO":
                        who(toClient);
                        break;

                    case "LOOK":
                        look(split, toClient, Integer.parseInt(split[1]));
                        break;

                    /*
                    If the user does not begin their input with any of the accepted commands, a message notifying
                    the user of an invalid command is sent to the client, and prompts them to re-enter a command.
                     */
                    default:
                        toClient.writeUTF("\ns: 300 Invalid command");
                }

                /*
                Once a command has been performed, or some variety of error is given, the server sends the message "end"
                to the client. End tells the client to prompt the user to enter a new command.
                 */
                toClient.writeUTF("end");

            }//end while

            // serveClient boolean value has been set to false in quit command, so loop is exited and client quits
            socket.close();
            toClient.close();
            fromClient.close();
            writeToFile.close();
            System.out.println("Client " + clientNumber + " has been disconnected.");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch

    }//end run

    /*
    Performs the ADD command. If the length of the array called split is less than 4, it means that the user
    failed to include all information required for the add command, and the user is given an error.
    If the total count of all records is greater than or equal to 19, it means there are already 20
    records stored and the user will be unable to add another record. If neither of these conditions are
    met, than the new record is successfully added in memory and the record count is incremented.
    The command is written as ADD [first name] [last name] [phone number].
    */
    public void add(String[] commandToRead, DataOutputStream sendToClient) throws IOException {
        if (commandToRead.length < 4) {
            sendToClient.writeUTF("s: 301 message format error");
        } else if (recordCount >= 19) {
            sendToClient.writeUTF("s: There are 20 records, cannot exceed this amount.");
        } else {
            record = new Record(String.valueOf(1000 + recordCount), commandToRead[1], commandToRead[2], commandToRead[3]);
            addressBook.add(recordCount, record);
            sendToClient.writeUTF("s: 200 OK\nThe new Record ID is " + (1000 + recordCount));
            recordCount++;
        }
    }

    /*
    Performs the DELETE command, written as DELETE [record id]. If the length of the array called
    split is less than 2, it means the user failed to include all information required for the DELETE command
    and the user is given an error. Otherwise, the program checks the in-memory address book for
    matching record. If the record id provided matches an id in memory, the record is successfully deleted
    and a boolean value called deleted is set to true. Once the entire address book is checked, if deleted is still false,
    it means that no match was found. The user is then informed that the record they desired to delete
    is not stored in memory.
    */
    public void delete(String[] commandToRead, DataOutputStream sendToClient) throws IOException {
        if (commandToRead.length < 2) {
            sendToClient.writeUTF("s: 301 message format error");
        } else {
            boolean deleted = false;
            for (int index = 0; index < addressBook.size(); index++) {
                if (addressBook.get(index).getRecordID().equals(commandToRead[1])) {
                    addressBook.remove(index);
                    sendToClient.writeUTF("s: 200 OK");
                    deleted = true;
                }
            }
            if (!deleted) {
                sendToClient.writeUTF("s: 403 The record does not exist.");
            }
        }

    }

    /*
    Performs the LIST command, written simply as LIST. The entirety of the address book is then sent
    to the client.
    */
    public void list(DataOutputStream sendToClient) throws IOException  {
        if (addressBook.isEmpty()) {
            sendToClient.writeUTF("s: The address book is empty.");
        }
        else {
            sendToClient.writeUTF("s: 200 OK\nThe list of records in the book:");
            for (int index = 0; index < addressBook.size(); index++) {
                sendToClient.writeUTF(addressBook.get(index).getRecordID() + "\t" + addressBook.get(index).getFirstName() + " " +
                        addressBook.get(index).getLastName() + "\t" + addressBook.get(index).getPhoneNumber());
            }
        }
    }

    /*
    Performs the QUIT command, written simply as QUIT. The writeToDisk method is called, and the String
    value response is set to "QUIT", so the server knows to close the connection with the client.
    */
    public void quit(DataOutputStream sendToClient) throws IOException  {
        sendToClient.writeUTF("s: 200 OK");
        sendToClient.writeUTF("QUIT");
        writeToDisk();
    }

    /*
    Performs the LOGIN command. If the entered username matches with the user's password, the user is successfully
    logged in. Otherwise, the user is informed of wrong user id or password and must try again.
     */
    public void login(String[] commandToRead, DataOutputStream sendToClient) throws IOException  {
        if (commandToRead.length < 3) {
            sendToClient.writeUTF("s: 301 message format error");
        }
        else {
            switch(commandToRead[1]) {

                case "root":
                    if (commandToRead[2].equals(userList.get(0).getPassword())) {
                        userList.get(0).userLogin();
                        clientLoggedIn = true;
                        sendToClient.writeUTF("s: 200 OK");
                    }
                    else {
                        sendToClient.writeUTF("410 Wrong UserID or Password");
                    }
                    break;
                case "john":
                    if (commandToRead[2].equals(userList.get(1).getPassword())) {
                        userList.get(1).userLogin();
                        clientLoggedIn = true;
                        sendToClient.writeUTF("s: 200 OK");
                    }
                    else {
                        sendToClient.writeUTF("410 Wrong UserID or Password");
                    }
                    break;
                case "david":
                    if (commandToRead[2].equals(userList.get(2).getPassword())) {
                        userList.get(2).userLogin();
                        clientLoggedIn = true;
                        sendToClient.writeUTF("s: 200 OK");
                    }
                    else {
                        sendToClient.writeUTF("410 Wrong UserID or Password");
                    }
                    break;
                case "mary":
                    if (commandToRead[2].equals(userList.get(3).getPassword())) {
                        userList.get(3).userLogin();
                        clientLoggedIn = true;
                        sendToClient.writeUTF("s: 200 OK");
                    }
                    else {
                        sendToClient.writeUTF("410 Wrong UserID or Password");
                    }
                    break;
                default:
                    sendToClient.writeUTF("410 Wrong UserID or Password");
            }
        }
    }

    /*
    Performs the LOGOUT command. If a given user is successfully logged in, then they will be logged out.
     */
    public void logout(String commandToRead, DataOutputStream sendToClient) throws IOException  {
        boolean successfulLogout = false;
        for (int index = 0; index < userList.size(); index++) {
            if (userList.get(index).getLoginStatus()) {
                userList.get(index).userLogout();
                clientLoggedIn = false;
                successfulLogout = true;
                sendToClient.writeUTF("s: 200 OK");
            }
            if(!successfulLogout) {
                sendToClient.writeUTF("s: 401 You are not currently logged in, login first.");
            }
        }
    }

    /*
    Performs the WHO command. Gives the list of all logged in users.
     */
    public void who(DataOutputStream sendToClient) throws IOException  {
        for (int index = 0; index < userList.size(); index++) {
            if (userList.get(index).getLoginStatus()) {
                sendToClient.writeUTF(userList.get(index).getUsername() + " " + socket.getInetAddress());
            }
        }
    }

    /*
    Performs the LOOK command. Looks through the list of records for a matching record based upon desired search criteria.
    Written as LOOK [number which determines lookup criteria] [name or phone number]. The numbers which determine lookup
    criteria are as follows:

            1 - First Name
            2 - Last Name
            3 - Phone Number

     Returns all matched records with the given criteria. If no matched records are found, a message is displayed
     acknowledging this result.
     */
    public void look(String[] commandToRead, DataOutputStream sendToClient, int userNumber) throws IOException  {
        ArrayList<Record> matchedRecords = new ArrayList<>();
        if (commandToRead.length < 3) {
            sendToClient.writeUTF("s: 301 message format error");
        }
        else {
            //sendToClient.writeUTF("s: 200 OK");
            for (int index = 0; index < addressBook.size(); index++) {
                if (userNumber == 1) {
                    if (commandToRead[2].equals(addressBook.get(index).getFirstName())) {
                        matchedRecords.add(addressBook.get(index));
                    }
                } else if (userNumber == 2) {
                    if (commandToRead[2].equals(addressBook.get(index).getLastName())) {
                        matchedRecords.add(addressBook.get(index));
                    }
                } else if (userNumber == 3) {
                    if (commandToRead[2].equals(addressBook.get(index).getPhoneNumber())) {
                        matchedRecords.add(addressBook.get(index));
                    }
                } else sendToClient.writeUTF("s: 301 message format error");
            }

            if (matchedRecords.isEmpty()) {
                sendToClient.writeUTF("s: 404 Your search did not match any records.");
            }
            else {
                sendToClient.writeUTF("s: 200 OK");
                for (int index = 0; index < matchedRecords.size(); index++) {
                    sendToClient.writeUTF(matchedRecords.get(index).getRecordID() + "\t" + matchedRecords.get(index).getFirstName() + " " +
                            matchedRecords.get(index).getLastName() + "\t" + matchedRecords.get(index).getPhoneNumber());
                }
            }
        }
    }

    /*
    writeToDisk writes all of the records stored in memory to the text file AddressBook.txt. writeToDisk is called after
    the user enters the QUIT or SHUTDOWN command.
    */
    public void writeToDisk() {
        for (int index = 0; index < addressBook.size(); index++) {
            try {
                writeToFile.write(addressBook.get(index).getRecordID() + " " + addressBook.get(index).getFirstName()
                        + " " + addressBook.get(index).getLastName() + " " + addressBook.get(index).getPhoneNumber() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
