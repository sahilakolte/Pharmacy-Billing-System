# Pharmacy Billing System

A **Java-based Pharmacy Billing System** using **JDBC** and **MySQL**. This project helps manage pharmacy inventory, billing, and customer details effectively.

---

## **Features**
- Add and manage medicines
- Generate bills for customers
- View and update stock
- Database-driven with **MySQL**
- Console-based interface

---

## **Tech Stack**
- **Java**
- **JDBC**
- **MySQL**
- **MySQL Connector JAR**

---

## **Project Structure**
```
Java_Project/
│
├── src/
│   └── Pharmacy.java          # Main Java file
│
├── bin/
│   └── Pharmacy.class         # Compiled classes
│
├── lib/
│   └── mysql-connector-j-8.0.32.jar  # JDBC Driver
│
├── data.sql                   # Sample data
├── medicaldb_create.sql       # DB schema
└── medicaldb_alter.sql        # DB updates
```

---

## **Prerequisites**
- Java JDK 8+
- MySQL Server
- MySQL Connector JAR (already included in `lib`)

---

## **Database Setup**
1. Open MySQL CLI or Workbench.
2. Execute:
   ```sql
   SOURCE medicaldb_create.sql;
   SOURCE medicaldb_alter.sql;
   SOURCE data.sql;
   ```
3. Verify:
   ```sql
   USE medicaldb;
   SHOW TABLES;
   ```

---

## **Steps to Run**
Run the following commands **from the `/src` directory**:

```bash
export CLASSPATH='<relative_path_to_project>/lib/mysql-connector-j-8.0.32.jar:.'
javac Pharmacy.java
java Pharmacy
```

*(On Windows, use `;` instead of `:` in the CLASSPATH.)*

---

## **Configuration**
Edit database credentials in `Pharmacy.java`:
```java
String url = "jdbc:mysql://localhost:3306/medicaldb";
String user = "root";
String password = "your_password";
```

---
