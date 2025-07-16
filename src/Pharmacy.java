//STEP 1. Import required packages
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Pharmacy {

   // Set JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
   static final String DB_URL = "jdbc:mysql://localhost:3306/medicaldb";

   // Database credentials
   static final String USER = "root";// add your user
   static final String PASSWORD = "pass";// add password

   public static void main(String[] args) {
      Connection conn = null;
      Statement stmt = null;

      Scanner sc = new Scanner(System.in);
      boolean exit = false;

      // Connecting to the Database
      try {
         // Register JDBC driver
         Class.forName(JDBC_DRIVER);
         // Open a connection
         conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
         // Disable auto-commit for transaction management
         conn.setAutoCommit(false);
         
         while (!exit) {
            // Display the menu
            System.out.println("\n---------------------------Pharmacy----------------------------");
            System.out.println("1. Buy Medicine");
            System.out.println("2. Update Inventory");
            System.out.println("3. View Inventory");
            System.out.println("4. View All Bills");
            System.out.println("5. View Specific Bill");
            System.out.println("6. Exit");
            System.out.println("---------------------------------------------------------------\n");
            System.out.print("Please choose an option: ");
            
            int choice;
            try {
               choice = sc.nextInt();
            } catch (InputMismatchException e) {
               System.out.println("Invalid input. Please enter a number.");
               sc.nextLine();
               continue; // back to menu
            }
            System.out.println("");

            switch (choice) {
               case 1: 
                  // Buying Medicine
                  buyMedicine(conn, sc);
                  break;
               case 2: 
                  // Updating Inventory
                  updateInventory(conn, sc);
                  break;
               case 3: 
                  // Viewing Inventory
                  sc.nextLine(); // consume newline
                  viewInventory(conn);
                  break;
               case 4:
                  // Viewing All Bills
                  sc.nextLine(); // consume newline
                  viewAllBills(conn);
                  break;
               case 5:
                  // Viewing Specific Bill
                  viewSpecificBill(conn, sc);
                  break;
               case 6:
                  // Exit
                  exit = true;
                  break;
               default:
                  sc.nextLine(); // consume newline
                  System.out.println("Invalid choice, please try again.");
            }
         }


      } catch (SQLException se) { // Handle errors for JDBC
         try {
            if (conn != null) {
               conn.rollback();
               System.out.println("Failed and rolled back!");
            }
         } catch (SQLException rollbackEx) {
            rollbackEx.printStackTrace();
         }
         se.printStackTrace();
      } catch (Exception e) { // Handle errors for Class.forName
         e.printStackTrace();
      } finally { // finally block used to close resources regardless of whether an exception was
                  // thrown or not
         try {
            if (stmt != null)
               stmt.close();
         } catch (SQLException se2) {
         }
         try {
            if (conn != null)
               conn.close();
         } catch (SQLException se) {
            se.printStackTrace();
         }
      }
      System.out.println("---End of Code---");
   } // end main

   // Method to handle buying medicine
   private static void buyMedicine(Connection conn, Scanner sc) throws SQLException {
      try {
         sc.nextLine(); // consume newline
         System.out.print("Enter customer phone number: ");
         String phone = sc.nextLine().trim();
         if (!phone.matches("\\d{10}")) {
            System.out.println("Invalid phone number. It should be 10 digits.");
            return;
         }
   
         // Check if customer exists
         String getCustomerQuery = "SELECT * FROM customer WHERE customer_phone = ?";
         PreparedStatement pstmt = conn.prepareStatement(getCustomerQuery);
         pstmt.setString(1, phone);
         ResultSet rs = pstmt.executeQuery();
   
         String customerId;
         String customerName;
         
         // If customer exists, get their ID and name
         // If not, create a new customer
         if (rs.next()) {
            customerId = rs.getString("customer_id");
            customerName = rs.getString("customer_name");
            System.out.println("\nWelcome back, " + customerName + "!\n");
         } else {
            // Create new customer
            System.out.print("New customer!\nPlease enter your name: ");
            customerName = sc.nextLine().trim();
            if (customerName.isEmpty()) {
               System.out.println("Invalid name. Cannot be empty.");
               return;
            }
   
            // Generate new customer ID
            Statement stmt = conn.createStatement();
            ResultSet rsMax = stmt.executeQuery("SELECT MAX(CAST(SUBSTRING(customer_id, 2) AS UNSIGNED)) FROM customer");
            int newIdNum = 1;
            if (rsMax.next()) {
               newIdNum = rsMax.getInt(1) + 1;
            }
            customerId = String.format("C%03d", newIdNum);
   
            // Insert new customer into the database
            String insertCustomer = "INSERT INTO customer (customer_id, customer_name, customer_phone) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertCustomer);
            insertStmt.setString(1, customerId);
            insertStmt.setString(2, customerName);
            insertStmt.setString(3, phone);
            insertStmt.executeUpdate();
            System.out.println("\nWelcome, " + customerName + "! Your ID is " + customerId + "\n");
            conn.commit();    // adding new customer to the database
         }
   
         // Ask for number of medicines to buy
         System.out.print("How many different medicines do you want to buy?\nEnter number: ");
         int n;
         try {
            n = sc.nextInt();
            sc.nextLine(); // consume newline after int
         } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine(); // clear invalid input from scanner
            return; // back to menu
         }

         if (n <= 0) {
            System.out.println("You must buy at least one medicine.");
            return;
         }
   
         // List to store valid bill items
         class Item {
            int medicineId;
            String medicineName;
            int quantity;
            double price;
         }
         List<Item> validItems = new ArrayList<>();
   
         for (int i = 0; i < n; i++) {
            System.out.print("\nEnter medicine name: ");
            String medName = sc.nextLine().trim();
            if (medName.isEmpty()) {
               System.out.println("Invalid medicine name.");
               return;
            }
   
            System.out.print("Enter quantity: ");
            int qty;
            try {
               qty = sc.nextInt();
               sc.nextLine(); // consume newline after int
            } catch (InputMismatchException e) {
               System.out.println("Invalid input. Please enter a number.");
               sc.nextLine(); // clear invalid input from scanner
               return; // back to menu
            }
   
            if (qty <= 0) {
               System.out.println("Quantity must be greater than 0.");
               return;
            }
   
            // Check if medicine exists
            String medQuery = "SELECT * FROM medicine WHERE LOWER(medicine_name) = LOWER(?)";
            PreparedStatement medStmt = conn.prepareStatement(medQuery);
            medStmt.setString(1, medName);
            ResultSet rsMed = medStmt.executeQuery();
            
            // If medicine exists, check quantity
            // If not, notify user
            if (rsMed.next()) {
               int availableQty = rsMed.getInt("quantity");
               int medId = rsMed.getInt("medicine_id");
               double price = rsMed.getDouble("price");
   
               if (availableQty >= qty) {
                  Item item = new Item();
                  item.medicineId = medId;
                  item.medicineName = rsMed.getString("medicine_name");
                  item.quantity = qty;
                  item.price = price;
                  validItems.add(item);
               } else {
                  System.out.println("Not enough stock for " + medName + ". Only " + availableQty + " available.");
               }
            } else {
               System.out.println("Medicine " + medName + " not found.");
            }
         }
   
         if (validItems.isEmpty()) {
            System.out.println("No valid items to bill.");
            return;
         }
   
         // Total bill amount
         double total = 0;
         for (Item item : validItems) {
            total += item.quantity * item.price;
         }
   
         // Insert bill
         String billInsert = "INSERT INTO bill (customer_id, bill_amount, bill_date) VALUES (?, ?, CURDATE())";
         PreparedStatement billStmt = conn.prepareStatement(billInsert, Statement.RETURN_GENERATED_KEYS);
         billStmt.setString(1, customerId);
         billStmt.setDouble(2, total);
         billStmt.executeUpdate();
   
         ResultSet rsBill = billStmt.getGeneratedKeys();
         rsBill.next();
         int billId = rsBill.getInt(1);
   
         // Insert into billitem and update stock
         for (Item item : validItems) {
            String itemInsert = "INSERT INTO billitem (bill_id, medicine_id, quantity, customer_id) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemInsert);
            itemStmt.setInt(1, billId);
            itemStmt.setInt(2, item.medicineId);
            itemStmt.setInt(3, item.quantity);
            itemStmt.setString(4, customerId);
            itemStmt.executeUpdate();
   
            String updateStock = "UPDATE medicine SET quantity = quantity - ? WHERE medicine_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateStock);
            updateStmt.setInt(1, item.quantity);
            updateStmt.setInt(2, item.medicineId);
            updateStmt.executeUpdate();
         }
   
         // Commit the transaction
         conn.commit();
         System.out.println("Bill ID: " + billId + " | Total Amount: ₹" + total);
   
      } catch (Exception e) {
         // Handle exceptions and rollback if necessary
         System.out.println("An error occurred during purchase: " + e.getMessage());
         conn.rollback();
      }
   }
   
   // Method to handle updating inventory
   private static void updateInventory(Connection conn, Scanner sc) throws SQLException {
      try {
         sc.nextLine(); // Consume leftover newline
         System.out.print("Enter Admin ID: ");
         String adminId = sc.nextLine().trim();
         System.out.print("Enter Password: ");
         String password = sc.nextLine().trim();
   
         if (adminId.isEmpty() || password.isEmpty()) {
            System.out.println("Admin ID and Password cannot be empty.");
            return;
         }
   
         // Authenticate Admin
         String authQuery = "SELECT * FROM admin WHERE admin_id = ? AND password = ?";
         PreparedStatement authStmt = conn.prepareStatement(authQuery);
         authStmt.setString(1, adminId);
         authStmt.setString(2, password);
         ResultSet authRs = authStmt.executeQuery();
   
         if (!authRs.next()) {
            System.out.println("Invalid admin credentials!");
            return;
         } else {
            System.out.println("Hello, " + authRs.getString("admin_name") + "!\n");
         }
   
         // Ask for number of medicines to add/update
         System.out.print("How many medicines do you want to add/update?\nEnter number: ");
         int n;
         try {
            n = sc.nextInt();
         } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine(); // clear invalid input from scanner
            return; // back to menu
         }
         if (n <= 0) {
            System.out.println("You must update at least one medicine.");
            return;
         }
         sc.nextLine(); // Consume newline
   
         // Loop to add/update medicines
         for (int i = 0; i < n; i++) {
            System.out.print("\nEnter medicine name: ");
            String medName = sc.nextLine().trim();
            if (medName.isEmpty()) {
               System.out.println("Medicine name cannot be empty.");
               return;
            }
   
            // Check if medicine exists
            String checkQuery = "SELECT * FROM medicine WHERE LOWER(medicine_name) = LOWER(?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, medName);
            ResultSet rs = checkStmt.executeQuery();
   
            if (rs.next()) {
               int medId = rs.getInt("medicine_id");
               System.out.println("Medicine found! Updating existing entry.\n");
   
               // Update quantity
               System.out.print("Do you want to change the quantity? (yes/no): ");
               if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                  System.out.print("Enter quantity to add: ");
                  int newQty;
                  try {
                     newQty = Integer.parseInt(sc.nextLine().trim());
                     if (newQty <= 0) {
                        System.out.println("Invalid quantity.");
                        conn.rollback();
                        System.out.println("Updates for " + medName + " failed due to invalid quantity.");
                        return;
                     }
                  } catch (NumberFormatException e) {
                     System.out.println("Invalid quantity. Must be a valid number.");
                     conn.rollback();
                     System.out.println("Updates for " + medName + " failed due to invalid quantity.");
                     return;
                  }
   
                  // Update the quantity in the database
                  String updateQty = "UPDATE medicine SET quantity = quantity + ? WHERE medicine_id = ?";
                  PreparedStatement stmtQty = conn.prepareStatement(updateQty);
                  stmtQty.setInt(1, newQty);
                  stmtQty.setInt(2, medId);
                  stmtQty.executeUpdate();
                  System.out.println("Quantity updated.");
               }
   
               // Update price
               System.out.print("Do you want to change the price? (yes/no): ");
               if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                  System.out.print("Enter new price: ");
                  double newPrice;
                  try {
                     newPrice = Double.parseDouble(sc.nextLine().trim());
                     if (newPrice <= 0) {
                        System.out.println("Invalid price.");
                        conn.rollback();
                        System.out.println("Updates for " + medName + " failed due to invalid price.");
                        return;
                     }
                  } catch (NumberFormatException e) {
                     System.out.println("Invalid price. Must be a valid number.");
                     conn.rollback();
                     System.out.println("Updates for " + medName + " failed due to invalid price.");
                     return;
                  }
   
                  String updatePrice = "UPDATE medicine SET price = ? WHERE medicine_id = ?";
                  PreparedStatement stmtPrice = conn.prepareStatement(updatePrice);
                  stmtPrice.setDouble(1, newPrice);
                  stmtPrice.setInt(2, medId);
                  stmtPrice.executeUpdate();
                  System.out.println("Price updated.");
               }
   
               // Update expiry date
               System.out.print("Do you want to change the expiry date? (yes/no): ");
               if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                  System.out.print("Enter new expiry date (YYYY-MM-DD): ");
                  String newExpDate = sc.nextLine().trim();
                  if (!newExpDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                     System.out.println("Invalid date format.");
                     conn.rollback();
                     System.out.println("Updates for " + medName + " failed due to invalid date format.");
                     return;
                  }
   
                  // Update the expiry date in the database
                  String updateExp = "UPDATE medicine SET exp_date = ? WHERE medicine_id = ?";
                  PreparedStatement stmtExp = conn.prepareStatement(updateExp);
                  stmtExp.setString(1, newExpDate);
                  stmtExp.setInt(2, medId);
                  stmtExp.executeUpdate();
                  System.out.println("Expiry date updated.");
               }
               conn.commit();
               System.out.println("Medicine updated successfully!");
            } 
            else {
               // New medicine, insert it
               System.out.println("Medicine not found. Adding as new.");
   
               System.out.print("Enter price: ");
               double price;
               try {
                  price = Double.parseDouble(sc.nextLine().trim());
                  if (price < 0) {
                     System.out.println("Price cannot be negative.");
                     return;
                  }
                  if (price == 0) {
                     System.out.println("Price cannot be zero.");
                     return;
                  }
               } catch (NumberFormatException e) {
                  System.out.println("Invalid price. Must be a valid number.");
                  return;
               }
   
               System.out.print("Enter quantity: ");
               int quantity;
               try {
                  quantity = Integer.parseInt(sc.nextLine().trim());
                  if (quantity < 0) {
                     System.out.println("Quantity cannot be negative.");
                     return;
                  }
                  if (quantity == 0) {
                     System.out.println("Quantity cannot be zero.");
                     return;
                  }
               } catch (NumberFormatException e) {
                  System.out.println("Invalid quantity. Must be a valid number.");
                  return;
               }
   
               System.out.print("Enter company: ");
               String company = sc.nextLine().trim();
               if (company.isEmpty()) {
                  System.out.println("Company cannot be empty.");
                  return;
               }
   
               System.out.print("Enter manufacture date (YYYY-MM-DD): ");
               String mfgDate = sc.nextLine().trim();
               if (!mfgDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                  System.out.println("Invalid manufacture date format.");
                  return;
               }
   
               System.out.print("Enter expiry date (YYYY-MM-DD): ");
               String expDate = sc.nextLine().trim();
               if (!expDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                  System.out.println("Invalid expiry date format.");
                  return;
               }
   
               // Insert new medicine into the database
               String insertQuery = "INSERT INTO medicine (medicine_name, price, quantity, company, mfg_date, exp_date) VALUES (?, ?, ?, ?, ?, ?)";
               PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
               insertStmt.setString(1, medName);
               insertStmt.setDouble(2, price);
               insertStmt.setInt(3, quantity);
               insertStmt.setString(4, company);
               insertStmt.setString(5, mfgDate);
               insertStmt.setString(6, expDate);
               insertStmt.executeUpdate();
               System.out.println("\nInserted new medicine: " + medName);
               conn.commit();
            }
         }
   
         // Commit all changes
         conn.commit();
         System.out.println("Inventory updates committed successfully!");
   
      } catch (Exception e) {
         // Handle exceptions and rollback if necessary
         System.out.println("An error occurred during inventory update: " + e.getMessage());
         conn.rollback();
      }
   }   

   // Method to handle viewing inventory
   private static void viewInventory(Connection conn) throws SQLException {
      // View all medicines in the inventory
      String viewQuery = "SELECT * FROM medicine";
      Statement stmt = conn.createStatement();
      ResultSet rsInventory = stmt.executeQuery(viewQuery);

      System.out.println("Inventory:");
      while (rsInventory.next()) {
         System.out.println("\nMedicine ID: " + rsInventory.getInt("medicine_id") + 
            "\n | Name: " + rsInventory.getString("medicine_name") + 
            "\n | Price: " + rsInventory.getDouble("price") + 
            "\n | Quantity: " + rsInventory.getInt("quantity") + 
            "\n | Expiry Date: " + rsInventory.getDate("exp_date"));
      }
   }

   // Method to handle viewing all bills
   private static void viewAllBills(Connection conn) throws SQLException {
      // Query to get all bills
      // Join bill, billitem, medicine, and customer tables
      
      String billQuery = "SELECT b.bill_id, b.bill_amount, c.customer_name, c.customer_phone, " +
                        "m.medicine_name, bi.quantity, (bi.quantity * m.price) AS subtotal " +
                        "FROM bill b " +
                        "JOIN billitem bi ON b.bill_id = bi.bill_id " +
                        "JOIN medicine m ON bi.medicine_id = m.medicine_id " +
                        "JOIN customer c ON b.customer_id = c.customer_id " +
                        "ORDER BY b.bill_id";

      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(billQuery);

      int currentBillId = -1;
      double billTotal = 0;

      while (rs.next()) {
         // Check if we are on a new bill
         // If so, print the previous bill total and reset for the new bill
         // Print bill header
         int billId = rs.getInt("bill_id");
         if (billId != currentBillId) {
            if (currentBillId != -1) {
                  System.out.println("---------------------------------------------------");
                  System.out.println("Total: ₹" + billTotal + "\n");
            }
            currentBillId = billId;
            billTotal = 0;
            System.out.println("---------------------------------------------------");
            System.out.println("Bill ID: " + billId);
            System.out.println("Customer: " + rs.getString("customer_name") + " | Phone: " + rs.getString("customer_phone"));
            System.out.println("---------------------------------------------------");
            System.out.printf("%-20s %-10s %-10s\n", "Medicine", "Quantity", "Subtotal");
         }

         // Print each medicine in the bill
         String medicineName = rs.getString("medicine_name");
         int quantity = rs.getInt("quantity");
         double subtotal = rs.getDouble("subtotal");

         System.out.printf("%-20s %-10d ₹%-10.2f\n", medicineName, quantity, subtotal);
         billTotal += subtotal;
      }

      // Print total for last bill
      if (currentBillId != -1) {
         System.out.println("---------------------------------------------------");
         System.out.println("Total: ₹" + billTotal);
      } else {
         System.out.println("No bills found.");
      }
   }

   // Method to view a specific bill by bill ID
   private static void viewSpecificBill(Connection conn, Scanner sc) throws SQLException {
      // View a specific bill by bill ID
      sc.nextLine(); // consume newline
      System.out.print("Enter Bill ID to view: ");
      int billIdInput;
      try {
         billIdInput = sc.nextInt();
      } catch (InputMismatchException e) {
         System.out.println("Invalid input. Please enter a valid Bill ID.");
         sc.nextLine(); // Clear wrong input
         return;
      }

      // Query to get bill details
      String specificBillQuery = "SELECT b.bill_id, b.bill_amount, c.customer_name, c.customer_phone, " +
                                 "m.medicine_name, bi.quantity, (bi.quantity * m.price) AS subtotal " +
                                 "FROM bill b " +
                                 "JOIN billitem bi ON b.bill_id = bi.bill_id " +
                                 "JOIN medicine m ON bi.medicine_id = m.medicine_id " +
                                 "JOIN customer c ON b.customer_id = c.customer_id " +
                                 "WHERE b.bill_id = ?";

      PreparedStatement pstmt = conn.prepareStatement(specificBillQuery);
      pstmt.setInt(1, billIdInput);
      ResultSet rs = pstmt.executeQuery();

      boolean found = false;
      double billTotal = 0;

      // Print bill details
      while (rs.next()) {
         if (!found) {
            found = true;
            // Print bill header
            System.out.println("\n---------------------------------------------------");
            System.out.println("Bill ID: " + rs.getInt("bill_id"));
            System.out.println("Customer: " + rs.getString("customer_name") + " | Phone: " + rs.getString("customer_phone"));
            System.out.println("---------------------------------------------------");
            System.out.printf("%-20s %-10s %-10s\n", "Medicine", "Quantity", "Subtotal");
         }

         // Print each medicine in the bill
         String medicineName = rs.getString("medicine_name");
         int quantity = rs.getInt("quantity");
         double subtotal = rs.getDouble("subtotal");

         System.out.printf("%-20s %-10d ₹%-10.2f\n", medicineName, quantity, subtotal);
         billTotal += subtotal;
      }

      // Print total for the bill
      if (found) {
         System.out.println("---------------------------------------------------");
         System.out.println("Total: ₹" + billTotal);
      } else {
         System.out.println("No bill found with ID: " + billIdInput);
      }
   }


}