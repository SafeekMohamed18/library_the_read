# The Read - Library Management System

A small, offline Java Swing application for a single librarian workstation. It follows the supplied assignment brief and stores its records as CSV files.

## Requirements

- JDK 17 or newer (the current development machine has JDK 26)
- Maven 3.9+ (optional; useful for building from the command line)
- VS Code with the Extension Pack for Java, IntelliJ IDEA Community, or another Java IDE

No database server or internet connection is needed to run the application. The interface uses built-in Swing components and a custom light theme, so there are no runtime UI dependencies.

## Run

Open this folder in a Java IDE and run `lms.Main`, or use Maven:

```powershell
mvn package
java -jar target/the-read-library-1.0.0.jar
```

Without Maven, compile and launch from PowerShell:

```powershell
New-Item -ItemType Directory -Force target\classes | Out-Null
javac --release 17 -encoding UTF-8 -d target\classes (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })
java -cp target\classes lms.Main
```

The application creates its data directory under the user's home folder at `.the-read-library`. On first launch, create the librarian account on the setup screen. Its password is stored as a salted PBKDF2 hash.

## Stored files

The application creates `books.csv`, `members.csv`, `rentals.csv`, and `librarians.csv` under `.the-read-library`. Writes use temporary files and replace the previous data file only after the new file has been written. A `.bak` copy of the previous file is kept. Existing `loans.csv` data is read automatically and new saves use `rentals.csv`.
Books with completed rental history are archived when removed from the catalogue so historical reports stay valid.

## Core smoke test

The test has no external dependencies. After compiling the main source as above, run:

```powershell
New-Item -ItemType Directory -Force target\test-classes | Out-Null
javac --release 17 -encoding UTF-8 -cp target\classes -d target\test-classes (Get-ChildItem -Recurse -Filter *.java src\test\java | ForEach-Object { $_.FullName })
java -ea -cp "target\classes;target\test-classes" lms.service.LibraryServiceSmokeTest
```

## Assignment rules

- Maximum of 3 active rentals per member
- Standard rental duration of 14 days
- One renewal of 7 days, only before the due date
- Fine of Rs. 10 per overdue day, capped at the book price
- Overdue rentals and unpaid fines block new issues
- ISBN and member IDs must be unique
- Books with active rentals cannot be deleted; members are deactivated instead

The rental limit, rental duration, renewal duration, and fine rate are centralized in `LibraryService`.
