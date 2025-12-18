# Floating-Point Error Visualizer

A Java Swing application that simulates and visualizes IEEE-754 double-precision (binary64) floating-point arithmetic operations, showing how rounding errors occur and accumulate.

## Features

- **Interactive GUI**: Input two decimal numbers and see how they're represented in binary64 format
- **Detailed Visualization**: Shows the complete arithmetic process including:
  - Binary representation (sign, exponent, mantissa)
  - Alignment of operands
  - Guard, Round, and Sticky bits
  - Rounding operations
  - Final result in both binary and decimal
- **Pre-loaded Examples**: Demonstrates common floating-point issues like precision errors, catastrophic cancellation, and associativity problems
- **Step-by-step Breakdown**: See exactly how floating-point operations work under the hood

## Requirements

- Java 8 or higher
- A graphical environment (X11/Windows/macOS) to run the Swing GUI

## How to Run

### Compile
```bash
javac Minimal_Floating_Simulator.java
```

### Run
```bash
java Minimal_Floating_Simulator
```

## Usage

1. Launch the application
2. Enter two decimal numbers in the input fields (A and B)
3. Click one of the operation buttons:
   - **Add** (A + B)
   - **Subtract** (A - B)
   - **Multiply** (A × B)
   - **Divide** (A ÷ B)
4. View the detailed breakdown of the floating-point operation in the text area

### Example Buttons

- **Example: Precision Error**: Demonstrates how small numbers can lose precision
- **Example: Cancellation**: Shows catastrophic cancellation when subtracting nearly equal numbers
- **Example: Associativity**: Illustrates that (a + b) + c ≠ a + (b + c) in floating-point arithmetic

## Notes

- This simulator is educational and shows the internal workings of IEEE-754 arithmetic
- The application requires a GUI environment and will not run in headless environments (like some cloud terminals or CI/CD pipelines)
- All calculations follow the IEEE-754 binary64 standard with proper rounding modes