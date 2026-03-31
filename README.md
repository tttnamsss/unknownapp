# unknownapp
This is an unknown application written in Java

---- For Submission (you must fill in the information below) ----
### Use Case Diagram
![](/swcons_lab11_1.png)

### Flowchart of the main workflow

```mermaid
flowchart TD
    Start([Start]) --> LoginMenu["Login Menu\n(1) Student\n(2) Admin\n(3) Exit"]
    LoginMenu -->|1| StudentLogin["Student Login\nEnter ID or 'new'"]
    LoginMenu -->|2| AdminLogin["Admin Login\nEnter password"]
    LoginMenu -->|3| Exit["Exit and Save"]
    StudentLogin -->|Existing Student| StudentMenu["Student Menu\nView Courses, Register, Drop,\nSchedule, Billing, Edit Profile, Logout"]
    StudentLogin -->|New Student| CreateProfile["Create New Student Profile"] --> StudentMenu
    StudentMenu -->|Logout| LoginMenu
    AdminLogin -->|Valid Password| AdminMenu["Admin Menu\nView Catalog, Roster, Students,\nAdd/Edit Students & Courses, Billing, Logout"]
    AdminLogin -->|Invalid Password| LoginMenu
    AdminMenu -->|Logout| LoginMenu
    Exit --> End([End])
```

### Prompts

1. as you a master in software construction and evolution, can you 4.	Create a flowchart (using Mermaid) to show the user's flow through the main menu. put it below of ### Flowchart of the main workflow on Readme.md. 