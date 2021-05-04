# Appian RPA - IBM 3270 Workflow Library - EHLLAPI

The IBM 3270 Workflow Library is a low code workflow library to ease the management of an IBM3270 terminal using Appian RPA. Using EHLLAPI, these methods execute faster than using the keyboard with the emulator. These low code methods handle:
  - Interaction with an emulator using EHLLAPI using the emulator DLL
  - Abstraction of complex EHLLAPI function inputs and outputs

This workflow libraby was developed using IBM Personal Communications v12 emulator & corresponding EHLLAPI documentation:
https://www.ibm.com/docs/en/personal-communications/12.0?topic=SSEQ5Y_12.0.0/com.ibm.pcomm.doc/books/html/emulator_programming08.htm

The library as-is may work with other emulators that support EHLLAPI, but changes may also be needed.

# Methods

  - IBM Connect to Emulator
  - IBM Disconnect from Emulator
  - IBM Maximize Window
  - IBM Enter Credential 
  - IBM Find Text
  - IBM Get Text at Line
  - IBM Get Text at Coordinates
  - IBM Get Field at Coordinates
  - IBM Bulk Get Field at Coordinates
  - IBM Go to Text Position (with Offset)
  - IBM Go to Coordinates
  - IBM Write Here
  - IBM Write at Coordinates
  - IBM Bulk Write at Coordinates
  - IBM Write at Label (with Offset)

# Getting Started

  - Use Appian low-code method 'Press Keyboard Shortcuts' for Win + (R) to start the application
  - To get the session letter after starting the application, do the following:
    - Use Appian low-code method 'Wait active window title' with 'List of regular expressions' set to: .\*Session.\*
    - If previous step true, use Appian low-code method 'Get active window title' and save the value (example: Session A - [24 x 80]) into a variable pv!windowTitle
    - Use Appian low-code method 'Evaluate expression' to extract the session letter with an expression such as: index(extract(pv!windowTitle,"Session "," -"),1,null)
    - The session letter is needed when connecting to the emulator session
  - Once connected, use the other methods in this library to interact with the emulator
  - If interacting with 2 or more emulators at the same time, disconnect from the current and connect to another in order to switch between the emulators
  - Use Appian low-code methods 'Activate Window' and 'Press Keyboard Shortcuts' for Alt + Function Key (4) to close the application

# Tips

  - The column coordinate in these methods match the column number displayed in the emulator, starting at 1 and incrementing by 1 moving from left to right
  - The row coordinate in these methods match the row number displayed in the emulator, starting at 1 and incrementing by 1 moving from top to bottom
  - The emulator will show the coordinate position (row,column) in the bottom right of the app and also inside the OIA info bar if shown (View > Expanded OIA)
  - To consolidate actions, use IBM Write at Coordinates instead of IBM Go to Coordinates then IBM Write Here
  - To consolidate actions, use IBM Bulk Write at Coordinates when entering mutliple pieces of data on the same screen
  - To consolidate actions, use IBM Bulk Get Field at Coordinates when reading mutliple pieces of data on the same screen

# Method Details

## IBM Connect to Emulator
  - This method must be called at the beginning of the RPA workflow, after the application has launched, before calling any other IBM methods in the workflow library.
  - The DLL file is located inside the emulator installation on the machine running the agent
    - Example DLL inputs
      - C:\Program Files (x86)\IBM\Personal Communications
      - pcshll32
    - Example session letter input
      - If window title is Session A - [24 x 80], then session letter is A

## IBM Disconnect from Emulator
  - This method disconnects from the current connected session
  - Use this when switching between multiple emulator sessions on the same machine
  - Use this when done using an emulator or just close the application

## IBM Maximize Window
  - Maximizes the emulator window
  
## IBM Enter Credential
  - This method will get credentials from the Appian RPA Console and enter them into the emulator at the current cursor position.
  - If credentials must be entered at a position different than the current cursor position, use 'IBM Go To Coordinates' to navigate first.
  - This method will reserve the credentials and release them once the robotic execution is complete (same as the OOTB credentials methods), which only matters if the credentials have a "max use" value set in the Appian RPA Console

## IBM Find Text
  - Returns the row column coordinate location of the search string
  - Case-sensitive
  - Output is an Appian map with fields: row & column. Returns null if not found.
    - Example is: {"column":"40","row":"1"}

## IBM Get Text at Line
  - Returns the full text string on the specificed line number

## IBM Get Text at Coordinates
  - Returns the text string of specified length starting at the speciified coordinate
  - If the specified length goes past the current row, the result will contain text from multiple rows

## IBM Get Field at Coordinates
  - Reads a field from the specified row column locations
  - Returns the full text string from the mainframe application field

## IBM Bulk Get Field at Coordinates
  - Reads multiple fields from the specified row column locations
  - Returns the full text strings from the mainframe application fields
  - Useful when reading multiple pieces of data on the same page
  - Input is JSON list object with fields: row, column
    - Example is a!toJson({{row:1,column:1},{row:5,column:5}}
  - Output is Appian map (multiple) with fields: field, row, column
    - Example is [{"field":"Smith","column":"20","row":"6"},{"field":"John","column":"20","row":"7"}]

## IBM Go to Text Position (with Offset)
  - Navigates to the location of the search string on the screen
  - Option to go to the first character or last character of the found string
  - Accepts offset for moving one full space to the right of the search string (go to last character, column offset of 2)
  - Case-sensitive

## IBM Go to Coordinates
  - Navigates to the specified row column location

## IBM Write Here
  - Writes text at the current cursor position
  - Option to clear the current field before writing

## IBM Write at Coordinates
  - Writes text at the specified row column location
  - Option to clear the field at the location before writing
  - Combination of 'IBM Go to Coordinates' and 'IBM Write Here'

## IBM Bulk Write at Coordinates
  - Writes multiple text strings at the specified row column locations
  - Option to clear the fields at the locations before writing
  - Useful when entering multiple pieces of data on the same page
  - Input is JSON list object with fields: text, row, column
    - Example is a!toJson({{text:"test1",row:1,column:1},{text:"test2",row:5,column:5}}

## IBM Write at Label (with Offset)
  - Writes text at the location of the search string on the screen
  - Option to write at the first character or last character of the found label
  - Accepts offset for writing the text one full space to the right of the label (write at last character, column offset of 2)
  - Case-sensitive
