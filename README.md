# Viettel CCCD/Passport Verification App - Demo version

## 📌 Overview
Viettel CCCD/Passport Verification App is an Android application designed for kiosk and tablet environments (e.g., **Joyusing Z10S Pro**).  
It enables secure verification of **Vietnamese citizen ID cards (CCCD)** and **passports** at telecom service counters, meeting Vietnam's government mandates for chip-based ID authentication.

---

## ✨ Features

### 🔹 Document Type Selection
- Choose between **CCCD** or **Passport**  
- Determines the flow for capture and validation

### 🔹 Document Capture
- Capture front & back photos of CCCD or passport photo page  
- Built with **CameraX** and **Joyusing SDK** for LED control:
  - Front LED → Face capture
  - Bottom LED → Document capture

### 🔹 OCR (MRZ Extraction)
- Powered by **Google ML Kit OCR**  
- Extracts MRZ from passports and CCCD backs

### 🔹 NFC Chip Reading
- Reads chip data securely  
- Integrates **EidSDK (Viettel)** + **JMRTD**

### 🔹 Chip Data Display
- Shows:
  - Name, DOB, document number, gender, nationality  
- Displays images:
  - Front, back, and chip portrait

### 🔹 Portrait Liveness Detection
- **Google ML Kit Face Detection** validates:
  - Smile, blink, head turn (left/right)  
- Prevents spoofing

### 🔹 Portrait Comparison
- Matches **live portrait** with **chip portrait**  
- Uses [ATIN Face Engine API](https://face-engine-api.atin.vn/api/v1/match)  
- Displays similarity percentage

### 🔹 Contract PDF Signing
- Loads PDF contracts for review  
- Digital signature via:
  - Checkbox + drawn signature  
- Ensures signature validity

### 🔹 Video Call Verification
- Real-time verification with agents  
- Built on **Stringee/VTS SDK**

### 🔹 Payment
- Supports **Cash** and **QR Code payments**  
- Syncs with **Viettel BCCS system**

### 🔹 Customer Feedback
- Collects satisfaction ratings  
- Allows feedback for low ratings

---

## 🏗 Technical Stack

- **Frontend:** Android (Kotlin, Fragments)  
- **Image Capture:** CameraX + Joyusing SDK (`ControlLightUtil`)  
- **OCR & Liveness:** Google ML Kit  
- **NFC Reading:** JMRTD + Scuba + EidSDK (Viettel)  
- **APIs:**  
  - ATIN Face Engine (Face Match)  
  - EidSDK (Chip Validation)  
  - Viettel BCCS (Order/Validation)  
- **PDF Signing:** Android PDF Viewer  
- **Reactive Programming:** RxJava

---

## 📦 Dependencies

- [CameraX](https://developer.android.com/training/camerax)  
- [Google ML Kit](https://developers.google.com/ml-kit)  
- Joyusing SDK (for LED light control)  
- [JMRTD](https://jmrtd.org/) + Scuba (for NFC)  
- EidSDK (Viettel)  
- [RxJava](https://github.com/ReactiveX/RxJava)  
- [Android PDF Viewer](https://github.com/barteksc/AndroidPdfViewer)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)  
- Android device with:
  - **NFC support**
  - **Front/Bottom LED (Joyusing Z10S Pro recommended)**

### Installation
```bash
git clone https://github.com/your-org/viettel-cccd-passport-verification.git
cd viettel-cccd-passport-verification
