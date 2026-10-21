# Library Management System

A simple Java Swing desktop application structured to match the CST 124-2 OOP final assessment.

## Requirements

- JDK 17 or later
- VS Code with the Extension Pack for Java, or another Java IDE

No database, API, Maven dependency, or internet connection is required to run the application.

## Project structure

```text
library_the_read/                   <- project root; open this folder in VS Code
|-- src/                            <- application and test source files
|   |-- main/                       <- code and resources used by the application
|   |   |-- java/                   <- Java source-code root
|   |   |   `-- lms/                <- main package: files declaring `package lms;`
|   |   |       |-- Main.java       <- starts the application
|   |   |       |-- Library.java    <- book/member collections and library rules
|   |   |       |-- SampleData.java <- creates demonstration records
|   |   |       |-- model/          <- Book, Member, and BorrowRecord classes
|   |   |       `-- ui/             <- Swing window and screens
|   |   `-- resources/              <- images and other non-Java app files
|   |       `-- lms/resources/      <- the library logo
|   `-- test/java/lms/              <- assessment smoke test
|-- target/                         <- generated .class files; safe to delete
|-- pom.xml                         <- optional Maven build configuration
`-- README.md                       <- this guide
```

The nested Java folders match the `package` line at the top of each source file.
For example, `src/main/java/lms/model/Book.java` starts with `package lms.model;`.
This is standard Java organization: `src/main/java` separates production code from
tests and resources, `lms` is the application's package, and `model` and `ui`
group related classes. These folders are part of the same project, not separate
Java installations or applications.

## Run from PowerShell

From the project folder:

```powershell
New-Item -ItemType Directory -Force target\classes | Out-Null
javac --release 17 -encoding UTF-8 -d target\classes (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })
New-Item -ItemType Directory -Force target\classes\lms\resources | Out-Null
Copy-Item "src\main\resources\lms\resources\the read.png" target\classes\lms\resources\
java -cp target\classes lms.Main
```

Alternatively, open the project in a Java IDE and run `lms.Main`.
The app logo is stored as a classpath resource in `src/main/resources/lms/resources/`.

The application starts with eight sample books and eleven sample members. It uses in-memory `ArrayList` collections, as allowed by the assessment. Changes made while the app is running are not saved after closing it.

## Features

- Dashboard with total, available, and borrowed book counts plus member count
- Left-side navigation between the dashboard, books, members, and borrow/return screens
- Add, view, search, and remove books
- View and search members; add members
- Borrow an available book for a selected member
- Search members and available books while choosing them for a borrowing transaction
- Return a borrowed book and update its borrowing record
- Input validation, duplicate-ID protection, and clear error messages

## OOP class design

- `Book` and `Member` encapsulate their fields and validation.
- `BorrowRecord` relates a `Book` to a `Member` and records borrowing and return dates.
- `Library` owns the `ArrayList` collections and provides the application's book, member, borrow, return, and search methods.
- `LibraryFrame` builds the Swing interface and calls `Library` methods; the rules are not implemented only in button event handlers.
- `SampleData` creates the demo records used when `Main` starts.

## Run the assessment checks

```powershell
New-Item -ItemType Directory -Force target\test-classes | Out-Null
javac --release 17 -encoding UTF-8 -cp target\classes -d target\test-classes (Get-ChildItem -Recurse -Filter *.java src\test\java | ForEach-Object { $_.FullName })
java -ea -cp "target\classes;target\test-classes" lms.LibraryAssessmentTest
```

The previous CSV files in `%USERPROFILE%\.the-read-library` are left untouched, but this assessment-aligned version uses in-memory collections and does not load them.
