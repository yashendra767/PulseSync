# PulseSync App

PulseSync is a hospital management application designed to streamline operations across multiple user roles such as Admin, Doctor, Nurse, and Pharmacist. It provides features for patient admissions, bed management, inventory control, medication dispensing, and reporting, integrated with Firebase backend services, also OkHTTP3 services.

## Table of Contents

- [Features](#features)  
- [Architecture](#architecture)  
- [User Roles and Permissions](#user-roles-and-permissions)  
- [Technology Stack](#technology-stack)  
- [Setup & Installation](#setup--installation)  
- [Usage](#usage)  
- [Screenshots](#screenshots)  
- [Contributing](#contributing)  
- [License](#license)  
- [Contact](#contact)  

---

## Features

- Role-based dashboard with tailored navigation drawers and bottom navigation tabs  
- Patient admissions and bed management with real-time status updates
- Admissions Bar charts and Vitals Line Charts for easy analytics  
- Inventory management with alerts for low stock and expiry  
- Medication and consumable dispensation tracking
- A QR code scanner for Pharmacist to scan inventory items and get the results based on loaded information
- Reporting and analytics for admissions, inventory, and tasks  
- User profile management and role-based access control  
- Real-time Firestore database integration 
- Notifications for critical alerts (planned via Firebase Cloud Messaging)
- Profile image upload from local storage 

---

## Architecture

- Modular architecture with separate Activities and Fragments per user role  
- Firestore collections: `users`, `beds`, `admissions`, `inventory`, `tasks`, `alerts`, `dispensing_history`, `doctors`, `reports`
- Firebase Authentication for user management and role enforcement  
- Material Design UI components for consistent user experience
- Well structured and queried database  

---

## User Roles and Permissions

| Role         | Access                                                                                   |
|--------------|------------------------------------------------------------------------------------------|
| Admin        | Full access: manage users, beds, admissions, inventory, reports, settings                |
| Doctor       | View/update admissions, view reports, profile, settings                                 |
| Nurse        | View bed info, manage inventory, access settings                                        |
| Pharmacist   | Manage inventory, dispense medications, view dispensing history, access settings         |

---

## Technology Stack

- Android (Kotlin)  
- Firebase Firestore  
- Firebase Authentication  
- Firebase Cloud Functions (planned for notifications)  
- Material Design Components
- Glide
- Cloudinary
- MP Android Charts
- ZXing Barcode Scanner
- MVVM architecture (partially implemented)  

---

## Setup & Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/yourusername/pulsesync-app.git
   cd pulsesync-app
2. Open in Android Studio.
3. Setup Firebase project:
    -Create a Firebase project at Firebase Console.
    -Add your Android app to Firebase and download google-services.json.
    -Place google-services.json in the app/ directory.
4. Configure Firestore rules and enable Firebase Authentication (Email/Password).
5. Build and run the app on an emulator or device.

---

## Usage

- Login with a registered user account.
- Navigation drawer and bottom tabs adjust based on user role.
- Use the dashboards to manage patients, inventory, admissions, and reports.
- Admin users can manage users and system settings.
- Use all the roles seamlessly.

---
  
## Screenshots 

![IMG-20250529-WA0009](https://github.com/user-attachments/assets/19b71b71-2309-4d8d-830a-311456e89f7a)
![IMG-20250529-WA0008](https://github.com/user-attachments/assets/0a5c8bc1-06ce-4d43-9af7-042716d1355b)
![IMG-20250529-WA0007](https://github.com/user-attachments/assets/8aaa3f68-26e2-4767-b68b-055e01ab6542)
![IMG-20250529-WA0004](https://github.com/user-attachments/assets/cb7db3dd-b635-476b-9574-91a8b36a3726)
![IMG-20250529-WA0003](https://github.com/user-attachments/assets/bcf0528a-890b-41cb-932f-065dcfa12ae8)
![IMG-20250529-WA0001](https://github.com/user-attachments/assets/82296ce4-5203-4680-b226-8f0269b94c94)
![IMG-20250529-WA0012](https://github.com/user-attachments/assets/b182e84f-46b5-4110-8d55-b2440956391d)
![IMG-20250529-WA0010](https://github.com/user-attachments/assets/a2954545-7934-4b33-adf9-e9a22bed0eb5)

---

## Contributing

Contributions are welcome! Please follow these steps:

  - Fork the repository
  - Create a feature branch (git checkout -b feature/YourFeature)
  - Commit your changes (git commit -m 'Add some feature')
  - Push to the branch (git push origin feature/YourFeature)
  - Open a Pull Request

---

## License

A license for this project has not been chosen yet.

Until a license is added, contributions are welcome. Assume to be open sourced.

---

## Contact

Yashendra Awasthi- yashendraawasthi@gmail.com
Project Link : https://github.com/yashendra767/PulseSync/

   
