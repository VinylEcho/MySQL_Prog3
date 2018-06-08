/**
 * Connector: Basic connection module that opens a connection to the given SQL server, then makes and returns queries
 *
 *  Precondtions:
 *  - Input strings are valid and properly ordered
 *
 *  Postconditions:
 *  - New connection is opened with the given credentials
 *  - Connection is maintained and accessed through the Connectifier object
 * */

import java.sql.*;

public class Connectifier {
    private Connection conn;

    /** Constructor: Takes in a file of valid SQL server credentials and initializes a connection
     *
     *  Precondition:
     *  - none
     *
     *  Postconditions:
     *  - A new connection to the specified server is created with the given credentials
     * */
    public Connectifier(String dbAccess, String user, String pass){
        this.conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbAccess, user, pass);
            System.out.println ("Database connection established");
        } catch (Exception e) {
            System.err.println ("Error: connection could not be established");
        }
    }

    /** query: Takes in a String and passes the query to the currently held connection
     *
     *  Preconditions:
     *  - The connection has been successfully established and can be accessed
     *
     *  Postconditions:
     *  - Returns the ResultSet of the given query (or prints an error message if the query fails)
     * */
    public ResultSet query (String query) {
        ResultSet results = null;
        try {
            Statement statement = conn.createStatement();
            results = statement.executeQuery(query);
        }catch (Exception e){
            System.out.println("Error: invalid query");
        }
        return results;
    }

    /** update: takes in an update command and executes it on the given connection
     *
     *  Preconditions:
     *  - The connection has already been established
     *
     *  Postconditions:
     *  - The database has been updated with the given query
     * */
    public void update(String update){
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(update);
        }catch (Exception e){
            System.out.println("Error: invalid query");
        }
    }

    /** close: closes the connection
     *
     *  Preconditions:
     *  - None
     *
     *  Postconditions:
     *  - The connection is closed
     * */
    public void close(){
        if (conn != null){
            try {
                conn.close ();
                System.out.println ("Database connection terminated");
            } catch (Exception e) {
             /* ignore close errors */ }
        }
    }

}
