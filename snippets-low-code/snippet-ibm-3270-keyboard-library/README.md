# Appian RPA - IBM 3270 Workflow Library - Keyboard

The IBM 3270 Workflow Library is a low code workflow library to ease the management of an IBM3270 terminal using Appian RPA. These low code methods handle:
  - Entering special characters into the emulator for both regular text and credentials
  - Typing with character pauses to ensure proper data entry
  - Navigating to input fields using calculations and keystrokes
  - Extracting text on the current screen via various selection parameters

# Methods

  - IBM Set Emulator
  - IBM Maximize Window
  - IBM Enter Credential 
  - IBM Find Text
  - IBM Get Text at Line
  - IBM Get Text at Coordinate
  - IBM Go to Text Position (with Offset)
  - IBM Go to Coordinates
  - IBM Write Here
  - IBM Write at Coordinates
  - IBM Bulk Write at Coordinates
  - IBM Write at Label (with Offset)

# Tips

  - Use Appian keyboard low-code module method 'Types windows key + letter' (R) to start the application
  - Use Appian low-code methods 'Activate Window' and 'Types alt + function key' (4) to close the application
  - The X coordinate in these methods corresponds to the column number, starting at 1 and incrementing by 1 moving from left to right
  - The Y coordinate in these methods corresponds to the row number, starting at 1 and incrementing by 1 moving from top to bottom
  - The emulator will show the coordinate position (y,x) in the bottom right of the app and inside the OIA info bar if shown (View > Expanded OIA)
  - To consolidate actions, use IBM Write at Coordinates instead of IBM Go to Coordinates then IBM Write Here
  - To consolidate actions, use IBM Bulk Write at Coordinates when entering lots of data on the same screen

# Method Details

## IBM Set Emulator
  - This method must be called at the beginning of the RPA workflow, before calling any other IBM methods in the workflow library.
  - The emulator type (PCOMM or WC3270) must be passed because different logic is used by the workflow library depending on the emulator.
  - The window xpath for the emulator window title (example: .\*Session.\*) must be passed because the workflow library activates the window before every interaction

## IBM Maximize Window
  - Maximizes the emulator window
  
## IBM Enter Credential
  - This method will get credentials from the Appian RPA Console and enter them into the emulator at the current cursor position, hanlding for special characters.
  - This method will reserve the credentials and release them once the robotic execution is complete (same as the OOTB credentials methods), which only matters if the credentials have a "max use" value set in the Appian RPA Console

## IBM Find Text
  - Returns the XY coordinate location (Appian data type of multiple number integer) of the search string

## IBM Get Text at Line
  - Returns the full text string on the specificed line number

## IBM Get Text at Coordinate
  - Returns the text string of specified length starting at the speciified coordinate
  - This only returns text on the same line as the coordinate (stops at the end of the row)

## IBM Go to Text Position (with Offset)
  - Navigates to the location of the first character of the search string
  - Accepts offset for moving to the right of the search string (x offset of len(pv!searchString)+1)
  - Case sensitive
  - Designed to retry search 3 times, then exception if text not found (use IBM Find Text to check if text exists on screen as it handles nulls)

## IBM Go to Coordinates
  - Navigates to the specified XY location

## IBM Write Here
  - Writes text at the current cursor position
  - Handles special character entry and character pauses

## IBM Write at Coordinates
  - Writes text at the specified XY location
  - Handles special character entry and character pauses
  - Combination of IBM Go to Coordinates and IBM Write Here

## IBM Bulk Write at Coordinates
  - Writes multiple text strings at the specified XY locations
  - Handles special character entry and character pauses
  - Useful when entering lots of data on the same page
  - Expects fields: text, x, y
  - Example is a!toJson({{text:"test1",x:1,y:1},{text:"test2",x:5,y:5}}

## IBM Write at Label (with Offset)
  - Writes text at the location of the first character of the search string
  - Accepts offset for writing the text to the right of the label (x offset of len(pv!searchString)+1)
  - Case sensitive
  - Designed to retry search 3 times, then exception if label not found (use IBM Find Text to check if text exists on screen as it handles nulls)
