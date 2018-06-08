/**
 * Prog3: driver for the 3rd program.  Utilizes a Connectifier object to send SQL queries to a specified server
 *
 *  Preconditions:
 *  - The program was run with exactly 1 command line argument
 *  - Arg[0] is a valid file name
 *
 *  Postconditions:
 *  - New Connectifier object is created using the given server, username and password
 *  - Reads in prompts from the console enabling the user to find flights to and from the selected airports
 *  - Closes connection to the database when the user is done
 * */

import java.io.File;
import java.sql.ResultSet;
import java.util.Scanner;

public class Prog3 {
    private static Connectifier c;

    public static void main (String[] args){
        c = null;
        String dbAccess, user, pass;
        if(args.length == 1) {
            try{
                File file = new File(args[0]);
                Scanner sc = new Scanner(file);
                dbAccess = sc.next();
                user = sc.next();
                pass = sc.next();
                c = new Connectifier(dbAccess,user,pass);
            }catch (Exception e){
                System.out.println("Error: invalid file name");
            }
        }else{
            System.out.println("Error: invalid number of arguments.  Please run with a valid file name");
            return;
        }

        System.out.println("Welcome to the Flight Finder (tm)!");
        boolean done = false;
        Scanner sc = new Scanner(System.in);
        String direction, origin, dateDepart, dateReturn, dest, section, query, queryReturn;

        while(!done){
            section = "First";
            origin = dest = "";
            System.out.println("Please select the type of flight that you are looking for:");
            System.out.println(" -- One for one way flights");
            System.out.println(" -- Two for round trip flights");
            System.out.print(" -- Quit to quit: ");
            direction = sc.next();
            if(direction.equalsIgnoreCase("quit")) {
                done = true;
            }else if(direction.equalsIgnoreCase("one") || direction.equalsIgnoreCase("two")){
                System.out.print("Please enter the starting airport code: ");
                origin = sc.next().toUpperCase();
                System.out.print("Please enter your desired destination code: ");
                dest = sc.next().toUpperCase();
                System.out.print("Please enter your desired cabin class: ");
                section = sc.next();
                System.out.print("Please enter your desired departure date (in YYYY-MM-DD format): ");
                dateDepart = sc.next();


                if(origin.length()==3 && dest.length()==3) {
                    if (direction.equalsIgnoreCase("one")) {
                        query = "select flightNumber, price as pr, manufacturer, model, airlineName, numRows, seatsPerRow " +
                                "from flights as fl " +
                                "natural join fares " +
                                "join aircraft as ar on ar.typePlane = fl.typePlane " +
                                "join airlines as al on al.airlineCode = fl.airlineCode " +
                                "where origin = '" + origin + "' and destination = '" + dest + "' " +
                                "and cabin = '" + section + "' and flightDate = '" +dateDepart+"';";
                        queryPrinter(c.query(query), section);
                    } else if (direction.equalsIgnoreCase("two")) {
                        System.out.print("Please enter your desired return date (in YYYY-MM-DD format): ");
                        dateReturn = sc.next();
                        query = "select flightNumber, price as pr, manufacturer, model, airlineName, numRows, seatsPerRow " +
                                "from flights as fl " +
                                "natural join fares " +
                                "join aircraft as ar on ar.typePlane = fl.typePlane " +
                                "join airlines as al on al.airlineCode = fl.airlineCode " +
                                "where origin = '" + origin + "' and destination = '" + dest + "' " +
                                "and cabin = '" + section + "' and flightDate = '" +dateDepart+"';";
                        queryReturn = "select flightNumber, price as pr, manufacturer, model, airlineName, numRows, seatsPerRow " +
                                "from flights as fl " +
                                "natural join fares " +
                                "join aircraft as ar on ar.typePlane = fl.typePlane " +
                                "join airlines as al on al.airlineCode = fl.airlineCode " +
                                "where origin = '" + dest + "' and destination = '" + origin + "' " +
                                "and cabin = '" + section + "' and flightDate = '" +dateReturn+"';";
                        roundPrinter(c.query(query), c.query(queryReturn), section);
                    }
                }else{
                    System.out.println("Error: invalid origin or destination code (must be 3 characters)");
                }
            }else {
                System.out.println("Error: must specify whether the flight will be one or two way");
            }

        }

        c.close();
    }

    /** queryPrinter: Does what it says on the tin, takes in a resultSet and prints out all the entries
     *
     *  Preconditions:
     *  - Results is a valid ResultSet with columns:
     *         flightNumber, price as pr, manufacturer, model, airlineName, numRows, seatsPerRow
     *
     *  Postconditions:
     *  - Prints the resultset to the console as a table, or an error if the set is empty
     * */
    private static void queryPrinter(ResultSet results, String section){
        int seats;
        System.out.println();
        try {
            if(!results.next()){
                System.out.println("No flights found");
            }
            System.out.printf("%12s%10s%40s%26s%6s%12s%8s\n",
                    "Direction", "FlightNum", "Airline", "PlaneType", "Seats", "Section", "Price");
            while (results.next()) {
                seats = results.getInt("numRows") * results.getInt("seatsPerRow");
                System.out.printf("%12s%10s%40s%26s%6s%12s%8s\n", "Departure",
                        results.getString("flightNumber"), results.getString("airlineName"), results.getString("manufacturer") + " "
                                + results.getString("model"), seats, section, "$"+results.getString("pr"));
            }

        }catch (Exception e){
            // Silences exceptions since the query method already reports invalid sets
        }
        System.out.println();
    }

    /** roundPrinter: prints out a series of tables showing the cost of a single way trip followed by all return trips
     *
     *  Preconditions:
     *  - ResultSets are both initialized to contain the columns:
     *      flightNumber, price as pr, manufacturer, model, airlineName, numRows, seatsPerRow
     *
     *  Postconditions:
     *  - The contents of the sets are printed to the console in small tables of all departing flights followed by all returning flights
     *  - The price column displays the base price for a one-way flight for the first entry, and the round trip price for all following columns
     *  - If the returning flight resultSet is empty, runs the single direction query printer instead
     * */
    private static void roundPrinter(ResultSet start, ResultSet end, String section){
        int basePrice, totalPrice, seats;
        System.out.println();
        try {
            if(!start.next()){
                System.out.println("No flights found");
            } if (!end.next()){
                System.out.println("No return flights found");
                queryPrinter(start, section);
                return;
            }
            while (start.next()) {
                basePrice = start.getInt("pr");
                seats = start.getInt("numRows") * start.getInt("seatsPerRow");
                System.out.printf("%12s%10s%40s%26s%6s%12s%8s\n",
                        "Direction", "FlightNum", "Airline", "PlaneType", "Seats", "Section", "Price");

                System.out.printf("%12s%10s%40s%26s%6s%12s%8s\n", "Departure",
                        start.getString("flightNumber"), start.getString("airlineName"),start.getString("manufacturer") + " "
                                + start.getString("model"), seats, section, "$"+basePrice);
                while (end.next()){
                    seats = start.getInt("numRows") * start.getInt("seatsPerRow");
                    totalPrice = basePrice + end.getInt("pr");
                    System.out.printf("%12s%10s%40s%26s%6s%12s%8s\n", "Return",
                            end.getString("flightNumber"), end.getString("airlineName"), end.getString("manufacturer") + " "
                                    + end.getString("model"), seats, section, "$"+totalPrice);
                }
                System.out.println();
                end.beforeFirst();
            }

        }catch (Exception e){
            // Silences exceptions since the query method already reports invalid sets
        }
        System.out.println();
    }
}
