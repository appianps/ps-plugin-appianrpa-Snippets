# Snippet IBM3270 Manager

Snippet IBM3270 Manager Library is a low code workflow library to ease the management of an IBM3270 terminal using Appian RPA. These low code methods handle:
  - Entering special characters into the emulator for both regular text and credentials
  - Typing with character pauses to ensure proper data entry
  - Navigating to input fields using calculations and keystrokes
  - Extracting text on the current screen via various selection parameters

# Main Methods

  - IBM Set Emulator
  - IBM Enter Credential 
  - IBM Find Text
  - IBM Get Text at Line
  - IBM Get Text at Coordinate
  - IBM Go to Text Position
  - IBM Go to Coordinates
  - IBM Write Here
  - IBM Write at Coordinates
  - IBM Bulk Write at Coordinates
  - IBM Write at Label (with Offset)

# Method Details

## IBM Set Emulator
This method must be called at the beginning of the RPA workflow, before calling any other IBM methods in the workflow library.
The emulator type (PCOMM or WC3270) must be passed because different logic is used by the workflow library depending on the emulator.
The window xpath for the emulator window title (example: .\*Session.\*) must be passed because the workflow library activates the window before every interaction
