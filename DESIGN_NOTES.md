# Akosua's Pharmacy — Design Rationale

## 1. Purpose

This document explains the architectural and technical decisions behind the redesigned Akosua's Pharmacy application.

The objective was not simply to reorganise the original code. It was to make the application easier to understand, maintain, extend and test while preserving its valid business behaviour.

The redesign focuses on:

- Protecting business rules from being bypassed.
- Separating responsibilities between classes.
- Representing different medication and customer behaviours appropriately.
- Maintaining reliable stock and transaction information.
- Making the code easier for another developer to work with.

## 2. Project organisation

The application follows a layered package structure:

```text
src/main/java/com/akosua/pharmacy/
├── Main.java
├── model/
├── inventory/
├── ledger/
├── service/
└── exceptions/
```

The project uses Maven's standard source directory, `src/main/java`, so that Maven can locate and compile application code without additional source-directory configuration.

The Java package structure groups related classes by responsibility.

| Package | Responsibility |
|---|---|
| `model` | Represents medications, customers and transactions. |
| `inventory` | Manages medication collections and stock-related queries. |
| `ledger` | Maintains transaction history. |
| `service` | Coordinates pharmacy operations. |
| `exceptions` | Defines application-specific business failures. |

This structure is intended to prevent unrelated responsibilities from accumulating in one class.

## 3. Design decisions and rationale

### 3.1 Abstract classes for Medication and Customer

**Decision:** `Medication` and `Customer` are abstract classes.

**Reason:** Both represent general concepts with shared properties and behaviour, but their specific types have different rules.

For example, all medications have a name, price and stock quantity. However, prescription medication requires prescriber validation, whereas over-the-counter medication does not.

Similarly, customers share information such as an ID, name and purchase history, but different customer types may receive different discounts.

An abstract class allows shared fields and implemented methods to be defined once while requiring subclasses to provide specific behaviour.

**Alternative considered:** Using separate, unrelated classes for every medication and customer type.

This would duplicate common fields and methods. An abstract parent class reduces that duplication.

**Trade-off:** Inheritance creates a relationship between parent and child classes. Changes to the parent can affect all subclasses. It should therefore be used where the types genuinely share a common identity and behaviour.

### 3.2 Encapsulation and access modifiers

**Decision:** Most fields are declared `private`, while selected constructors and methods use other access modifiers.

**Reason:** Application state should not be freely modified by unrelated classes.

For example, medication stock should not be changed through direct field assignment:

```java
medication.stock = -10;
```

Instead, stock changes should go through controlled methods such as:

```java
medication.dispenseStock(quantity);
medication.restock(quantity);
```

These methods can validate the requested operation before changing the state.

The access modifiers serve different purposes:

- `private`: Restricts access to the declaring class.
- `protected`: Allows subclasses to access selected members and also provides package-level access.
- `public`: Makes members accessible to other classes that can access the class.
- No modifier (package-private): Restricts access to the same package.

The constructors of abstract parent classes use `protected` so subclasses can initialise the inherited state without exposing those constructors as general public creation methods.

**Why some methods are `final`:** Methods that protect important stock or history rules should not be overridden casually by subclasses. Declaring them `final` prevents subclasses from replacing that implementation.

**Trade-off:** Restricting access improves control but means other classes must use the public methods provided by the model.

### 3.3 Medication and Customer inheritance

**Decision:** Specific medication and customer types extend their respective abstract parent classes.

For medication:

- `OverTheCounterMedication`
- `PrescriptionMedication`

For customers:

- `WalkInCustomer`
- `RegisteredPatient`

**Reason:** The subclasses share common state but differ in behaviour.

For example, a registered patient receives a 5% discount, while a walk-in customer does not receive that discount.

The pharmacy can still work with the common parent type:

```java
Medication medication;
Customer customer;
```

This is an example of polymorphism. The actual object's implementation determines which overridden behaviour is executed.

**Alternative considered:** Using conditional statements throughout the pharmacy service to check the type of every medication or customer.

That approach would spread type-specific rules across the application. Inheritance keeps those differences closer to the classes they concern.

**Trade-off:** Adding many new types can increase the number of subclasses.

### 3.4 BigDecimal for monetary values

**Decision:** `BigDecimal` is used for medication prices and transaction totals.

**Reason:** Java's `float` and `double` types use binary floating-point representation. Some decimal values cannot be represented exactly.

For financial calculations, this can introduce unexpected rounding errors.

For example:

```java
double price = 0.1 + 0.2;
System.out.println(price);
```

The result may not be exactly `0.3`.

`BigDecimal` provides decimal arithmetic and explicit rounding control.

The project also uses values created from strings, such as:

```java
new BigDecimal("12.50");
```

This avoids introducing binary floating-point approximation when constructing monetary values.

**Alternative considered:** Using `double` for simplicity.

This is unsuitable for calculations where predictable decimal precision matters.

**Trade-off:** `BigDecimal` is more verbose and requires deliberate decisions about scale and rounding.

### 3.5 Transaction as a record

**Decision:** `Transaction` is represented using a Java record.

**Reason:** A transaction primarily represents a collection of data describing an event: its ID, timestamp, type, customer, medication, quantity and total.

A record is appropriate because Java automatically provides:

- A canonical constructor.
- Accessor methods.
- `equals()` and `hashCode()`.
- `toString()`.

The compact constructor also allows validation of the supplied transaction data.

For example, a transaction with a non-positive quantity should be rejected.

**Alternative considered:** Creating a regular class with fields, a constructor and accessor methods.

That would work, but it would require more repetitive code for a class whose main responsibility is carrying data.

**Important limitation:** Records are only shallowly immutable. Their component references cannot be reassigned after construction, but referenced objects may still be mutable.

For example, a transaction containing a `Medication` reference does not automatically preserve a historical copy of that medication's name or price.

If historical transaction details must remain unchanged even when medication information changes, the transaction should store the relevant values as snapshots.

### 3.6 Enum for transaction types

**Decision:** `TransactionType` is an enum containing the supported transaction types, such as `DISPENSE` and `RESTOCK`.

**Reason:** The application has a known, limited set of transaction categories.

Using an enum prevents arbitrary strings such as `"dispense"`, `"Dispense"` and `"sale"` from being treated as different or inconsistent values.

**Alternative considered:** Using strings to represent transaction types.

Strings are flexible but do not provide the same compile-time restriction to a predefined set of values.

### 3.7 Separate Inventory and Ledger classes

**Decision:** Inventory management and transaction recording are handled by separate classes.

**Reason:** They represent different responsibilities.

`Inventory` manages medication collections, prevents duplicate medication names and provides low-stock queries.

`Ledger` records transactions and provides read-only snapshots of transaction history.

Combining both responsibilities in one class would make that class responsible for managing two different kinds of application state.

Separating them makes each class easier to understand and test.

**Trade-off:** The pharmacy service must coordinate these components. This introduces dependencies between classes, but keeps their individual responsibilities focused.

### 3.8 Pharmacy as a service/coordinator

**Decision:** `Pharmacy` coordinates operations involving customers, inventory and the ledger.

**Reason:** A dispensing operation involves several steps:

1. Validate the customer and medication.
2. Validate prescription requirements where applicable.
3. Check and update stock.
4. Calculate the transaction amount.
5. Apply the customer's discount.
6. Record the customer's activity.
7. Add the transaction to the ledger.

These steps involve multiple objects. The pharmacy service coordinates them rather than placing all the workflow logic in `Main`, `Inventory` or `Medication`.

The service receives its `Inventory` and `Ledger` dependencies through its constructor. This is constructor-based dependency injection.

It allows those dependencies to be supplied from outside rather than created internally by the service.

**Alternative considered:** Having `Pharmacy` create its own inventory and ledger.

That would make it harder to substitute these dependencies during testing or when introducing alternative implementations.
## 4. Additional Improvement Beyond the Original Requirements: Low-Stock Alerts

Problem identified

The original application could track medication quantities, but it did not provide a mechanism to alert pharmacy staff when a medication was approaching a critically low stock level.

This meant staff would have to inspect stock quantities manually to identify medicines that needed replenishment. They could potentially discover a shortage only when a customer requested a medication.

Improvement introduced

A low-stock alert mechanism was added to help the pharmacy identify medications that have reached or fallen below a configurable reorder threshold.

Each medication now has a reorderLevel that defines the minimum stock level at which an alert should be raised.

The condition is:

stock <= reorderLevel

For example:

Medication, Current stock, Reorder level, Alert
Paracetamol, 15, 20, Yes
Amoxicillin, 39, 10, No

Implementation

The improvement involves two main responsibilities:

1. Medication

Each medication stores its own reorder threshold and provides an isLowStock() method to determine whether its current stock has reached that threshold.

2. Inventory

The inventory provides a getLowStockMedications() method that identifies all medications requiring attention.

The pharmacy service exposes this functionality through printLowStockAlerts(), allowing the application to display the affected medications.

Why this improvement was chosen

The improvement addresses a practical pharmacy management problem rather than simply adding technical complexity.

It provides several benefits:


• Helps staff identify medicines that may soon run out..
• Reduces dependence on manually checking every medication..
• Supports more proactive stock management..
• Allows different medications to have different reorder thresholds..

Scope and limitations

The alerts are informational. They do not automatically restock medication, contact suppliers or generate purchase orders.

The reorder thresholds used in the demonstration are sample values:


• Paracetamol: 20 units..
• Amoxicillin: 10 units..

These values would need to be configured according to actual pharmacy demand and replenishment requirements.

This improvement provides a foundation for future supplier and purchase-order management functionality.
## 5. Business rules and data integrity

The redesign introduces or reinforces rules that prevent invalid operations.

### Stock control

A medication cannot be dispensed in a quantity greater than its available stock.

For example, attempting to dispense 100 units when only 39 remain must fail.

The failed operation should not create a successful dispensing transaction.

### Prescription validation

Prescription medication requires a valid prescriber before it can be dispensed.

This validation belongs with the prescription medication behaviour rather than being duplicated throughout the application.

### Low-stock alerts

Each medication has a `reorderLevel`.

The inventory identifies medications where:

```java
stock <= reorderLevel
```

This is a warning mechanism. It does not automatically create a purchase order or restock the medication.

The original application did not define reorder thresholds. The demonstration uses:

- Paracetamol: 20 units.
- Amoxicillin: 10 units.

These are sample values and must be confirmed by the pharmacy before real operational use.

### Transaction history

The ledger exposes an unmodifiable snapshot rather than its internal collection.

This prevents callers from directly adding or removing transactions through the returned list.

It does not, by itself, provide permanent storage or a fully immutable audit trail.

## 6. Exception handling

The project defines custom exceptions for specific business-rule failures:

- `InsufficientStockException`
- `MissingPrescriberException`

These provide more meaningful exception types than relying exclusively on generic exceptions.

For example, a caller can distinguish insufficient stock from a missing prescriber and respond differently.

However, defining a custom exception does not automatically mean the application uses it. The corresponding validation code must explicitly throw the custom exception.

If the application continues to throw `IllegalStateException` or `IllegalArgumentException` directly, the custom exception classes are not yet providing their intended benefit.

Both custom exceptions extend unchecked exception types, so Java does not require callers to catch or declare them.

## 7. Behaviour preservation and corrections

The redesigned demonstration preserves the valid original transactions:

1. Kofi purchases two Paracetamol 500mg at GHS 12.50 each, giving a total of GHS 25.00.
2. Registered patient Ama purchases one Amoxicillin 250mg at GHS 30.00, receiving a 5% discount and paying GHS 28.50.
3. Paracetamol is restocked by 50 units.

The original attempt to dispense 100 Amoxicillin units when only 39 remain is rejected.

This corrects the stock-control defect identified during diagnosis. The failed operation must not be recorded as a successful dispensing transaction, and stock must remain at 39.

## 8. Limitations and future improvements

The current design improves the organisation and control of the application, but it is not yet a complete production pharmacy system.

The following limitations remain:

| Area | Current limitation | Possible improvement |
|---|---|---|
| Persistence | Inventory and ledger data are held in memory. | Introduce database persistence. |
| Concurrency | Simultaneous dispensing operations are not protected by a database transaction or locking strategy. | Add transaction management and concurrency control. |
| Audit history | The ledger provides in-memory history, not durable audit storage. | Store transactions permanently with appropriate access controls. |
| Transaction snapshots | Transactions may retain references to mutable domain objects. | Store historical values directly in transaction records. |
| Reordering | Low-stock alerts are informational only. | Add supplier and purchase-order management. |
| Testing | Business rules need automated test coverage. | Add unit tests for validation, discounts, dispensing and restocking. |
| Security | The demonstration does not provide authentication or role-based permissions. | Introduce user authentication and authorisation. |

These improvements should be introduced as the application grows rather than adding unnecessary complexity to the current implementation.

## 9. Design summary

The main architectural principle behind the redesign is separation of responsibilities combined with controlled access to application state.

- Abstract classes provide shared structure and enforce subtype-specific behaviour.
- Encapsulation protects important business rules.
- Records represent transaction data with less boilerplate.
- Enums restrict transaction categories to known values.
- `BigDecimal` supports reliable monetary calculations.
- Inventory and ledger classes manage separate responsibilities.
- The pharmacy service coordinates operations across those components.
- Custom exceptions make specific business failures distinguishable when used by the validation logic.

The result is a more maintainable foundation that can be extended with persistence, testing and additional pharmacy functionality without placing every responsibility in a single class.