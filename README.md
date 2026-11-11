Akosua’s Pharmacy — Stock and Dispensing Tracker

A Java application for managing pharmacy medication inventory, dispensing, restocking, customers, and transaction records.

This project is the rebuilt version of the Java Basics Lab A — Akosua’s Pharmacy starter repository. The goal was to preserve the original business behaviour while improving the code structure, enforcing important business rules, and making the system easier to extend.

Features

• Medication management: Supports over-the-counter and prescription medications..
• Customer management: Supports walk-in customers and registered patients..
• Stock control: Prevents dispensing quantities greater than available stock..
• Prescription validation: Requires a prescriber when dispensing prescription medication..
• Restocking: Allows medication stock to be replenished..
• Transaction ledger: Records dispensing and restocking transactions..
• Customer history: Keeps a record of customer dispensing activity..
• Low-stock alerts: Identifies medications that have reached their reorder levels..
• Patient discounts: Applies a discount to registered patients..

Project Structure

The application is organised into packages based on responsibility:

src/main/java/com/akosua/pharmacy/
├── Main.java
├── model/
│   ├── Medication.java
│   ├── OverTheCounterMedication.java
│   ├── PrescriptionMedication.java
│   ├── Customer.java
│   ├── WalkInCustomer.java
│   ├── RegisteredPatient.java
│   ├── Transaction.java
│   └── TransactionType.java
├── inventory/
│   └── Inventory.java
├── ledger/
│   └── Ledger.java
├── service/
│   └── Pharmacy.java
└── exceptions/
    ├── InsufficientStockException.java
    └── MissingPrescriberException.java

Requirements

• Java Development Kit (JDK) 21.
• Apache Maven.


Running the Application

Requirements

• Java Development Kit (JDK) 21.
• Apache Maven (optional).

1. Clone the repository

git clone https://github.com/suleiman-baba-mohammed/java-basics-a-akosuas-pharmacy.git
cd java-basics-a-akosuas-pharmacy

2. Using Maven

Compile the application:

mvn clean compile

Run it:

java -cp target/classes com.akosua.pharmacy.Main

3. Without Maven (using the JDK)

If Maven is not installed, you can compile and run the application directly using javac and java.

Linux / macOS (Bash):

mkdir -p target/classes
javac -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes com.akosua.pharmacy.Main

Windows (PowerShell):

New-Item -ItemType Directory -Force target/classes | Out-Null

$files = Get-ChildItem -Path src/main/java -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

javac -d target/classes $files
java -cp target/classes com.akosua.pharmacy.Main


Important Business Rules

1. Medication stock cannot become negative..
2. Dispensing and restocking quantities must be positive..
3. Prescription medication cannot be dispensed without a valid prescriber name..
4. Only registered customers can receive the registered-patient discount..
5. Medication prices must be positive..
6. Low-stock alerts are generated when stock reaches or falls below a medication’s reorder level..

Design Documentation

See the following files for more information about the implementation:


• DIAGNOSIS.md — Problems identified in the original application and the proposed structural improvements..
• DESIGN_NOTES.md — Explanation of the rebuilt architecture, design decisions, business rules, and possible future extensions..
• design-sketch.png — Initial design sketch for the proposed application structure..

Future Extension

The medication hierarchy is designed to support additional medication categories. For example, a controlled medication category could introduce stricter dispensing validation without requiring changes to the existing pharmacy dispensing workflow.