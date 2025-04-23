📝 Project Overview: Viettel CCCD/Passport Verification App
🔹 Purpose:
Designed for kiosk/tablet environments (like the Joyusing Z10S Pro).

Verifies citizen ID cards (CCCD) or passports at telecom service counters (Viettel flow).

Meets Vietnam's government mandates for chip-based ID authentication.

🚀 Main Features & Flow:
Document Type Selection

Choose between CCCD or passport.

Drives the capture flow.

Document Capture

Front & back photos of CCCD / passport photo page.

Uses CameraX with fill light control (via Joyusing SDK):

Front LED: For face capture.

Bottom LED: For document capture.

OCR (MRZ Extraction)

Uses Google ML Kit for OCR.

Reads MRZ (Machine Readable Zone) for passports/CCCD backs.

NFC Chip Reading

Reads chip data from CCCD/passport via NFC.

Uses EidSDK (Viettel) + JMRTD for secure chip communication.

Chip Data Display

Shows chip data:

Name, DOB, document number, gender, nationality, etc.

Displays captured images:

Front, back, and chip portrait.

Portrait Liveness Check

Uses ML Kit face detection.

Validates user actions:

Smile, blink, turn head left/right.

Ensures real person is interacting.

Portrait Comparison

Compares live portrait (smile image) vs chip portrait.

Calls Face Match API (https://face-engine-api.atin.vn/api/v1/match).

Displays matching percentage.

PDF Signing

Shows contract PDF to the customer.

Customer digitally signs on the screen.

Includes signature validation (checkbox + drawn signature).

Video Call Verification

Connects with call center agents for manual verification via Stringee/VTS.

Payment

Supports cash or QR code payments.

Updates Viettel's BCCS system accordingly.

Customer Feedback Survey

Collects satisfaction ratings.

Offers additional feedback options for lower ratings.

🏗 Technical Stack:
Frontend:
Android (Kotlin) with fragments for each step.

CameraX for image capture.

ML Kit for:

OCR (MRZ extraction).

Face detection (liveness check).

Joyusing SDK (ControlLightUtil) for LED light control.

Backend APIs:
Face match via ATIN Face Engine.

NFC chip validation via EidSDK.

BCCS Viettel API (for order management, validation).

Dependencies:
JMRTD + Scuba for NFC (chip reading).

RxJava for reactive flows.

Android PDF Viewer for contract signing.

