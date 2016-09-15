package kimble.orgdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;

public class QuickstartApiSample {

  private static BufferedReader reader = new BufferedReader(
        new InputStreamReader(System.in));

  PartnerConnection connection;
  String authEndPoint = "";

  public static void main(String[] args) {
     if (args.length < 1) {
        System.out.println("Usage: com.example.samples."
              + "QuickstartApiSamples <AuthEndPoint>");

        System.exit(-1);
     }

     QuickstartApiSample sample = new QuickstartApiSample(args[0]);
     sample.run();
  }

  public void run() {
    // Make a login call
     if (login()) {
        // Do a describe global
//        describeGlobalSample();

        // Describe an object
        describeSObjectsSample();

        // Retrieve some data using a query
        querySample();

        // Log out
        logout();
     }
  }

  // Constructor
  public QuickstartApiSample(String authEndPoint) {
     this.authEndPoint = authEndPoint;
  }

  private String getUserInput(String prompt) {
     String result = "";
     try {
        System.out.print(prompt);
        result = reader.readLine();
     } catch (IOException ioe) {
        ioe.printStackTrace();
     }

     return result;
  }

  private boolean login() {
     boolean success = false;
//     String username = getUserInput("Enter username: ");
//     String password = getUserInput("Enter password: ");
     String username = "ben@bvkimblehubdev.com";
     String password = "RuunasenPallit89OSsdKn1IaqOqkWAQytuEULgR";

     try {
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password);

        System.out.println("AuthEndPoint: " + authEndPoint);
        config.setAuthEndpoint(authEndPoint);

        connection = new PartnerConnection(config);
        printUserInfo(config);

        success = true;
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }

     return success;
  }

  private void printUserInfo(ConnectorConfig config) {
     try {
        GetUserInfoResult userInfo = connection.getUserInfo();

        System.out.println("\nLogging in ...\n");
        System.out.println("UserID: " + userInfo.getUserId());
        System.out.println("User Full Name: " + userInfo.getUserFullName());
        System.out.println("User Email: " + userInfo.getUserEmail());
        System.out.println();
        System.out.println("SessionID: " + config.getSessionId());
        System.out.println("Auth End Point: " + config.getAuthEndpoint());
        System.out
              .println("Service End Point: " + config.getServiceEndpoint());
        System.out.println();
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }
  }

  private void logout() {
     try {
        connection.logout();
        System.out.println("Logged out.");
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }
  }

  /**
   * To determine the objects that are available to the logged-in user, the
   * sample client application executes a describeGlobal call, which returns
   * all of the objects that are visible to the logged-in user. This call
   * should not be made more than once per session, as the data returned from
   * the call likely does not change frequently. The DescribeGlobalResult is
   * simply echoed to the console.
   */
  private void describeGlobalSample() {
     try {
        // describeGlobal() returns an array of object results that
        // includes the object names that are available to the logged-in user.
        DescribeGlobalResult dgr = connection.describeGlobal();

        System.out.println("\nDescribe Global Results:\n");
        // Loop through the array echoing the object names to the console
        for (int i = 0; i < dgr.getSobjects().length; i++) {
           System.out.println(dgr.getSobjects()[i].getName());
        }
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }
  }

  /**
   * The following method illustrates the type of metadata information that can
   * be obtained for each object available to the user. The sample client
   * application executes a describeSObject call on a given object and then
   * echoes the returned metadata information to the console. Object metadata
   * information includes permissions, field types and length and available
   * values for picklist fields and types for referenceTo fields.
   */
  private void describeSObjectsSample() {
     String objectToDescribe = getUserInput("\nType the name of the object to "
           + "describe (try Account): ");

     try {
        // Call describeSObjects() passing in an array with one object type
        // name
        DescribeSObjectResult[] dsrArray = connection
              .describeSObjects(new String[] { objectToDescribe });

        // Since we described only one sObject, we should have only
        // one element in the DescribeSObjectResult array.
        DescribeSObjectResult dsr = dsrArray[0];

        // First, get some object properties
        System.out.println("\n\nObject Name: " + dsr.getName());

        if (dsr.getCustom())
           System.out.println("Custom Object");
        if (dsr.getLabel() != null)
           System.out.println("Label: " + dsr.getLabel());

        // Get the permissions on the object

        if (dsr.getCreateable())
           System.out.println("Createable");
        if (dsr.getDeletable())
           System.out.println("Deleteable");
        if (dsr.getQueryable())
           System.out.println("Queryable");
        if (dsr.getReplicateable())
           System.out.println("Replicateable");
        if (dsr.getRetrieveable())
           System.out.println("Retrieveable");
        if (dsr.getSearchable())
           System.out.println("Searchable");
        if (dsr.getUndeletable())
           System.out.println("Undeleteable");
        if (dsr.getUpdateable())
           System.out.println("Updateable");

        System.out.println("Number of fields: " + dsr.getFields().length);

        // Now, retrieve metadata for each field
        for (int i = 0; i < dsr.getFields().length; i++) {
           // Get the field
           Field field = dsr.getFields()[i];

           // Write some field properties
           System.out.println("Field name: " + field.getName());
           System.out.println("\tField Label: " + field.getLabel());

           // This next property indicates that this
           // field is searched when using
           // the name search group in SOSL
           if (field.getNameField())
              System.out.println("\tThis is a name field.");

           if (field.getRestrictedPicklist())
              System.out.println("This is a RESTRICTED picklist field.");

           System.out.println("\tType is: " + field.getType());

           if (field.getLength() > 0)
              System.out.println("\tLength: " + field.getLength());

           if (field.getScale() > 0)
              System.out.println("\tScale: " + field.getScale());

           if (field.getPrecision() > 0)
              System.out.println("\tPrecision: " + field.getPrecision());

           if (field.getDigits() > 0)
              System.out.println("\tDigits: " + field.getDigits());

           if (field.getCustom())
              System.out.println("\tThis is a custom field.");

           // Write the permissions of this field
           if (field.getNillable())
              System.out.println("\tCan be nulled.");
           if (field.getCreateable())
              System.out.println("\tCreateable");
           if (field.getFilterable())
              System.out.println("\tFilterable");
           if (field.getUpdateable())
              System.out.println("\tUpdateable");

           // If this is a picklist field, show the picklist values
           if (field.getType().equals(FieldType.picklist)) {
              System.out.println("\t\tPicklist values: ");
              PicklistEntry[] picklistValues = field.getPicklistValues();

              for (int j = 0; j < field.getPicklistValues().length; j++) {
                 System.out.println("\t\tValue: "
                       + picklistValues[j].getValue());
              }
           }

           // If this is a foreign key field (reference),
           // show the values
           if (field.getType().equals(FieldType.reference)) {
              System.out.println("\tCan reference these objects:");
              for (int j = 0; j < field.getReferenceTo().length; j++) {
                 System.out.println("\t\t" + field.getReferenceTo()[j]);
              }
           }
           System.out.println("");
        }
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }
  }

  private void querySample() {
     String soqlQuery = "SELECT FirstName, LastName FROM Contact";
     try {
        QueryResult qr = connection.query(soqlQuery);
        boolean done = false;

        if (qr.getSize() > 0) {
           System.out.println("\nLogged-in user can see "
                 + qr.getRecords().length + " contact records.");

           while (!done) {
              System.out.println("");
              SObject[] records = qr.getRecords();
              for (int i = 0; i < records.length; ++i) {
            	 SObject con = records[i];
//                 Contact con = (Contact) records[i];
//                 String fName = con.getFirstName();
//                 String lName = con.getLastName();
                 String fName = (String)con.getField("firstName");
                 String lName = (String)con.getField("lastName");

                 if (fName == null) {
                    System.out.println("Contact " + (i + 1) + ": " + lName);
                 } else {
                    System.out.println("Contact " + (i + 1) + ": " + fName
                          + " " + lName);
                 }
              }

              if (qr.isDone()) {
                 done = true;
              } else {
                 qr = connection.queryMore(qr.getQueryLocator());
              }
           }
        } else {
           System.out.println("No records found.");
        }
     } catch (ConnectionException ce) {
        ce.printStackTrace();
     }
  }
}