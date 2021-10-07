/**
 *  The Record class contains the definition and functionality for a record that is created by the user. A record contains an id number, a person's first
 * name, last name and phone number. All are stored as strings for easy writing from client to server. Getters are defined to return
 * the records when necessary. An ArrayList data structure will store these records, and use the getters when the user invokes
 * the LIST command.
 */
public class Record {

    private String recordID;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public Record() {
        this("0000", "Empty", "Empty", "Empty");
    }

    public Record(String recordID, String firstName, String lastName, String phoneNumber) {
        this.recordID = recordID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    String getRecordID() { return recordID; }
    String getFirstName() { return firstName; }
    String getLastName() { return lastName; }
    String getPhoneNumber() { return phoneNumber; }
}
