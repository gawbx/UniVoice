# UniVoice: Empowering Campus Progress, One Voice at a Time

> **OOP 101 – Object-Oriented Programming** · Information Technology Department
> Batangas State University – The National Engineering University, Alangilan Campus
> College of Informatics and Computing Sciences · 1st Semester, AY 2026-2027
> **Team 12 – BeeSahCool** · Section BSIT-2107

---

## 1. Project Title

**UniVoice: Empowering Campus Progress, One Voice at a Time**

## 2. Project Description

UniVoice is a Java-based application that centralizes, organizes, and streamlines the collection and management of campus feedback. Students, faculty, and staff can submit suggestions and complaints in real time, while administrators get an organized view of all records so they can track, resolve, and clean up issues efficiently.

Each submission is saved as a record with an auto-generated Record ID, SR-Code, department, type, message, date filed, and a default status of **Pending**. Records are saved to a file, so data is kept between runs.

## 3. Problem Being Addressed

University campuses rely on outdated, fragmented feedback channels such as physical suggestion boxes and static surveys. These suffer from low engagement and zero visibility, so daily operational issues, broken infrastructure, and administrative bottlenecks often go unreported or unresolved.

This affects both sides of the campus:

- **Students and faculty** feel unheard when their reports get swallowed by administrative delays.
- **Administrators** lack the centralized data needed to quickly identify, prioritize, and fix pressing concerns.

A centralized digital platform turns scattered complaints into actionable data, supporting campus safety, operational efficiency, overall student satisfaction, and continuous institutional improvement.

## 4. Project Objectives

### General Objective

To develop a Java-based application that will centralize, organize, and streamline the collection and management of campus feedback, enabling students and faculty members to report suggestions and complaints in real time while providing administrators with actionable data to efficiently resolve issues.

### Specific Objectives

- To design and implement a user-friendly Java interface that enables students, faculty, and staff to easily submit categorized campus suggestions and complaints.
- To address persistent campus issues that haven't been fixed by giving administrators a clear, organized way to view complaints and take action.

## 5. Target Users

| Target User | How They Will Use the System |
|---|---|
| Students | Submission of suggestions and complaints |
| Faculty/Staff | Submission of suggestions and complaints |
| Administrator | Administers the records submitted in the system |

## 6. Main Features

| Feature | Description |
|---|---|
| **Role Selection and Admin Login** | At startup the system asks whether the user is an Administrator. Administrators must enter the correct password (3 attempts allowed); everyone else proceeds to the user menu. |
| **Submit Suggestion or Complaint** | A user enters their SR-Code, selects a department (CICS, COE, CAFAD, BAET, CET, CHE), chooses Suggestion or Complaint, and types the message. Inputs are validated before saving. |
| **Record Keeping** | Each submission becomes a record with an auto-generated Record ID, SR-Code, department, type, message, date filed, and a default status of Pending. Records are saved to a file so data is kept between runs. |
| **View and Search Records (Admin)** | Administrators can view all records, filter by Pending or Resolved status, or search by SR-Code. |
| **Update Status and Delete Records (Admin)** | Administrators can mark a record as Pending or Resolved. Only Resolved records can be deleted, and deletion requires confirmation. |

### Program Flow

**User flow:** Start → Ask: Administrator? (No) → Enter SR-Code → Select Department → Choose Suggestion or Complaint → Type the message → Validate input → Save record as Pending → Show Record ID → Return to start / Exit

**Administrator flow:** Start → Ask: Administrator? (Yes) → Enter password (3 tries) → Admin Menu → View / Search records → Update status (Pending / Resolved) → Delete Resolved record → Logout → Return to start / Exit

## 7. Technologies Used

- **Language:** Java
- **Data storage:** Plain text file (file I/O)
- **Core Java features:** `ArrayList`, arrays, `Scanner`, exception handling
- **Version control:** Git and GitHub

## 8. Team Members and Roles

**Team Name:** BeeSahCool · **Team Number:** 12 · **Section:** BSIT-2107

| Member | SR-Code | Role / Assigned Tasks |
|---|---|---|
| Año, Gabb Nickolai D. | 25-06680 | **Project lead.** Designs the `Feedback`, `Suggestion`, and `Complaint` classes (inheritance, polymorphism, abstraction); sets up and maintains the GitHub repository. |
| Cruzat, Neorwin Symone B. | 25-04041 | Develops `FeedbackManager`: record storage, file saving/loading, and the administrator features (view, search, update status, delete). |
| Dimayacyac, Nico Elijah B. | 25-06814 | Develops the main program: role selection, admin login, user menu, and input validation with exception handling; leads testing and debugging. |

## 9. OOP Concepts Demonstrated

| OOP Concept | How It Is Used |
|---|---|
| **Classes and Objects** | Each submission is an object created from a class (`Feedback` with the attributes ID, SR-Code, department, type, message, status, and date filed). A manager class holds the collection of record objects, and a main class runs the menus. |
| **Methods** | Each task is a separate method, such as `adminLogin()`, `submitFeedback()`, `askDepartment()`, `listRecords()`, `updateStatus()`, `deleteRecord()`, `loadRecords()`, and `saveRecords()`, so the code is reusable and easy to test. |
| **Encapsulation** | Record attributes are private and accessed through getters. The status can only change through a controlled method (`setStatus`), and the admin password is kept private inside the system class. |
| **Inheritance** | `Suggestion` and `Complaint` are subclasses of a common `Feedback` class. They inherit the shared attributes and methods and only add what is specific to each type. |
| **Polymorphism** | The `display()` and `toFileLine()` methods are overridden in `Suggestion` and `Complaint`, so the system can treat every record as a `Feedback` and still get type-specific output. |
| **Abstraction** | `Feedback` is an abstract class that defines what every record must do (for example, `display()`) without fixing how. The menus only need to know what a record can do, not how it is stored. |
| **Exception Handling** | `try-catch` is used for invalid numeric input (`NumberFormatException`) and for reading/writing the data file (`IOException`), so the program shows a clear message instead of crashing. |
| **Arrays / Data Handling** | A `String` array stores the fixed list of departments, an `ArrayList` stores all `Feedback` records, and a text file keeps the data saved. Loops are used to search, filter, and display records. |

### Proposed Classes

| Class | Purpose | Main Attributes | Main Methods |
|---|---|---|---|
| `Feedback` (abstract) | Base class for every submitted record | id, srCode, department, type, message, status, dateFiled | getters, `setStatus()`, `display()`, `toFileLine()` |
| `Suggestion` / `Complaint` | Subclasses of `Feedback` for the two submission types | inherits `Feedback` attributes | `display()` and `toFileLine()` (overridden) |
| `FeedbackManager` | Stores and manages all records, including file saving and loading | `ArrayList` of `Feedback`, nextId, data file name | `addFeedback()`, `findById()`, `listRecords()`, `updateStatus()`, `deleteRecord()`, `loadRecords()`, `saveRecords()` |
| `CampusTracker` (main) | Runs the program: role selection, admin login, and the user and admin menu | `Scanner`, admin password, `DEPARTMENTS` array | `main()`, `adminLogin()`, `userMenu()`, `adminMenu()`, `askSRCode()`, `askDepartment()`, `askType()` |

> The classes may be changed as the project is developed.

## 10. Instructions for Running the Program

### Requirements

- Java Development Kit (JDK) 11 or higher
- Git (optional, for cloning)

### Steps

(IN PROGRESS)

## 11. Sample Screenshots

(IN PROGRESS)

## 12. Known Limitations

- The system runs in the console only. It has no graphical, web, or mobile interface.
- There is a single shared administrator password. Individual admin accounts and password changes are not supported.
- SR-Codes are checked for format only, not verified against the university's student database.
- Users cannot view, edit, or track the status of their own submissions. They only receive a Record ID.
- No email or SMS notifications are sent when a record is resolved.
- Attachments such as photos or documents cannot be submitted.
- Records are stored in a plain text file without encryption, and the system is not designed for several users at the same time over a network.
- Reports, charts, and analytics are not included.

## 13. Future Improvements

- Graphical, web, or mobile interface
- Individual admin accounts with changeable passwords
- SR-Code verification against the university's student database
- A way for users to view and track the status of their own submissions
- Email or SMS notifications when a record is resolved
- Support for attachments such as photos or documents
- Encrypted or database-backed storage with multi-user network support
- Reports, charts, and analytics for administrators

## 14. Project Contributors

| Name | GitHub |
|---|---|
| Año, Gabb Nickolai D. | [@gawbx](https://github.com/gawbx) |
| Cruzat, Neorwin Symone B. | [@neorwinsymone-del](https://github.com/neorwinsymone-del) |
| Dimayacyac, Nico Elijah B. | [@Nico-Dimayacyac](https://github.com/Nico-Dimayacyac) |

**Instructor:** Ms. Fatima Marie P. Agdon, MSCS

---

*Leading Innovations, Transforming Lives, Building the Nation*
