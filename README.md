# ErrorExplorer

ErrorExplorer is a Java Swing application that simulates IEEE-754 double-precision floating-point arithmetic and visualizes how rounding errors appear during computation. It is designed as an educational tool for understanding binary representation, exponent alignment, mantissa behavior, rounding, and floating-point edge cases. :contentReference[oaicite:4]{index=4}

## Current Features

- Interactive GUI for entering two decimal values
- Supports add, subtract, multiply, and divide operations
- Shows binary64 structure including sign, exponent, and mantissa
- Visualizes alignment, guard bits, round bits, sticky bits, and rounding steps
- Includes built-in examples for precision loss, catastrophic cancellation, and associativity issues :contentReference[oaicite:5]{index=5}

## Tech Stack

- Java
- Swing GUI
- IEEE-754 binary64 simulation concepts :contentReference[oaicite:6]{index=6}

## Future Plans

This project has strong long-term potential as a C++ educational and systems programming tool. A future rewrite could move beyond a Java visualization app and become a deeper floating-point exploration environment with lower-level control and expanded numerical analysis features.

Planned future directions:
- Rewrite the simulator in C++
- Add support for float, double, and extended precision comparisons
- Show bit-level manipulation more directly using C++ memory and type tools
- Add step-through execution mode for each arithmetic stage
- Include visual comparisons between ideal real-number math and machine arithmetic
- Add graphing tools to show error growth over repeated operations
- Expand into a mini numerical methods lab with root-finding and approximation demos
- Add preset experiments for cancellation, overflow, underflow, NaN, infinities, and denormals
- Possibly grow into a calculator-style desktop tool for teaching computer architecture and numerical computing

## Why This Project Matters

ErrorExplorer is a strong portfolio project because it shows understanding of floating-point representation, numerical correctness, and how computers actually perform arithmetic. A future C++ version would make it even stronger for systems, simulation, and low-level engineering work.

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
