import java.awt.*;
import java.math.BigInteger;
import javax.swing.*;

public class Minimal_Floating_Simulator {
    // Simulates IEEE-754 double-precision floating-point arithmetic with a minimal GUI

    // Binary64 layout
    private static final int EXP_BITS  = 11;
    private static final int FRAC_BITS = 52;
    private static final int EXP_BIAS  = 1023;
    private static final long EXP_MAX  = (1L << EXP_BITS) - 1;     // 0x7FF

    // Working precision: 53 (hidden 1 + 52), plus three GRS bits
    private static final int WORK_PRECISION = FRAC_BITS + 1; // 53
    private static final int EXT_BITS = 3;                   // guard, round, sticky

    public static void main(String[] args) {
    // Launch the GUI using the Swing event thread
        SwingUtilities.invokeLater(Minimal_Floating_Simulator::buildUI);
    }

    // GUI setup and event handling

    private static void buildUI() {
    // Build the main window and all GUI components
        JFrame f = new JFrame("Floating-Point Simulator (binary64) — Minimal");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900, 520); // window size
        f.setLocationRelativeTo(null);

    JTextField aDec = new JTextField("2.5", 16); // Default value A
    JTextField bDec = new JTextField("3.75", 16); // Default value B

    JTextArea out = new JTextArea(18, 80); // More rows and columns for better readability
    out.setEditable(false);
    out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    JScrollPane scroll = new JScrollPane(out);
    scroll.setPreferredSize(new Dimension(0, 320)); // Make scroll area taller

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;

        c.gridx = 0; c.gridy = r; top.add(new JLabel("Decimal A:"), c);
        c.gridx = 1; c.gridy = r; top.add(aDec, c);
    c.gridx = 2; c.gridy = r; JButton example1 = new JButton("Example: Precision Error"); top.add(example1, c); r++;

        c.gridx = 0; c.gridy = r; top.add(new JLabel("Decimal B:"), c);
        c.gridx = 1; c.gridy = r; top.add(bDec, c);
    c.gridx = 2; c.gridy = r; JButton example2 = new JButton("Example: Loss of Significance"); top.add(example2, c); r++;

        JPanel ops = new JPanel(new GridLayout(1,4,8,8));
        JButton add = new JButton("Add");
        JButton sub = new JButton("Sub");
        JButton mul = new JButton("Mul");
        JButton div = new JButton("Div");
        ops.add(add); ops.add(sub); ops.add(mul); ops.add(div);
    // Set up input fields, example buttons, and operation buttons
    // Example buttons show classic floating-point errors
    // Operation buttons (Add, Sub, Mul, Div) perform arithmetic

        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(top, BorderLayout.NORTH);
        root.add(ops, BorderLayout.CENTER);
        root.add(scroll, BorderLayout.SOUTH);
    // Layout: inputs and examples at top, operations center, output at bottom
        f.setContentPane(root);
        f.setVisible(true);

        Runnable runAdd = () -> runOp(aDec, bDec, out, Op.ADD);
        Runnable runSub = () -> runOp(aDec, bDec, out, Op.SUB);
        Runnable runMul = () -> runOp(aDec, bDec, out, Op.MUL);
        Runnable runDiv = () -> runOp(aDec, bDec, out, Op.DIV);
    // Runnables for each operation

        add.addActionListener(e -> runAdd.run());
        sub.addActionListener(e -> runSub.run());
        mul.addActionListener(e -> runMul.run());
        div.addActionListener(e -> runDiv.run());
    // Add click handlers for all operation buttons

        example1.addActionListener(e -> { aDec.setText("0.1"); bDec.setText("0.2"); runAdd.run(); });
        example2.addActionListener(e -> { aDec.setText("10000000000000000"); bDec.setText("1"); runAdd.run(); aDec.setText(outcomeDecimal(out, "Result")); bDec.setText("10000000000000000"); runSub.run(); });
    // Example buttons run preset error examples
    }

    private static void runOp(JTextField aDec, JTextField bDec, JTextArea out, Op op) {
    // Perform the selected arithmetic operation and update the output area
        try {
            double a = parseDecimal(aDec.getText());
            double b = parseDecimal(bDec.getText());

            String aBits = toBits(a);
            String bBits = toBits(b);

            // Prepare to collect step-by-step explanation and result
            StringBuilder steps = new StringBuilder(128);
            String resBits;
            // Select and perform the requested operation
            switch (op) {
                case ADD -> resBits = addOrSub(aBits, bBits, false, steps); // Addition
                case SUB -> resBits = addOrSub(aBits, bBits, true,  steps); // Subtraction
                case MUL -> resBits = multiply(aBits, bBits, steps);        // Multiplication
                case DIV -> resBits = divide(aBits, bBits, steps);          // Division
                default -> throw new IllegalStateException();               // Should not happen
            }
            double res = fromBits(resBits); // Convert result back to double

            // Display all relevant info and steps in the output area
            StringBuilder explanation = new StringBuilder();
            // Always show user-friendly explanation for classic floating-point error examples
            if (op == Op.ADD && Math.abs(a - 0.1) < 1e-12 && Math.abs(b - 0.2) < 1e-12) {
                explanation.append("Note: 0.1 + 0.2 does not exactly equal 0.3 due to how decimals are represented in binary.\n");
                explanation.append("Binary floating-point cannot represent 0.1 or 0.2 exactly, so the sum is slightly off.\n");
            }
            if (op == Op.ADD && Math.abs(a - 1e16) < 1e-2 && Math.abs(b - 1) < 1e-12) {
                explanation.append("Note: Adding 1 to a large number like 1e16 may not change the result due to limited precision.\n");
                explanation.append("The value 1 is too small to affect 1e16 in binary64, so the sum is still 1e16.\n");
            }
            if (op == Op.SUB && Math.abs(a - 1e16) < 1e-2 && Math.abs(b - 1e16) < 1e-2) {
                explanation.append("Note: Subtracting two large, nearly equal numbers can lose precision.\n");
                explanation.append("The result may not be exactly zero if the previous addition lost the small increment.\n");
            }
            out.setText(
                "A  (dec): " + a + "\n" +
                "A  (bin): " + spaced64(aBits) + "\n" +
                "B  (dec): " + b + "\n" +
                "B  (bin): " + spaced64(bBits) + "\n" +
                "Op: " + op + "\n" +
                "Result (dec): " + res + "\n" +
                "Result (bin): " + spaced64(resBits) + "\n\n" +
                conciseWhy(steps.toString()) +
                (explanation.length() > 0 ? "\n" + explanation.toString() : "")
            );
        } catch (NumberFormatException ex) {
            out.setText("Error: Invalid number format");
        } catch (IllegalArgumentException ex) {
            out.setText("Error: " + ex.getMessage());
        } catch (Exception ex) {
            out.setText("Error: Unexpected exception: " + ex.getMessage());
        }
    }

    // Extract last “Result (dec): …” from output area to feed the example chain
    private static String outcomeDecimal(JTextArea out, String key) {
        for (String line : out.getText().split("\\R")) {
            if (line.startsWith(key)) {
                int i = line.indexOf(':');
                return line.substring(i+1).trim();
            }
        }
        return "0";
    }

    // Conversion helpers: decimal <-> binary64

    private static double parseDecimal(String s) { return Double.parseDouble(s.trim()); }
    // Parse a string to double, trimming whitespace

    private static String toBits(double x) {
    // Convert a double to its 64-bit binary string representation
        long raw = Double.doubleToRawLongBits(x);
        String bits = Long.toBinaryString(raw);
        return "0".repeat(64 - bits.length()) + bits;
    }

    private static double fromBits(String bits64) {
    // Convert a 64-bit binary string back to double
        if (bits64.length() != 64 || !bits64.matches("[01]{64}"))
            throw new IllegalArgumentException("64-bit binary string required.");
        long v = new BigInteger(bits64, 2).longValue();
        return Double.longBitsToDouble(v);
    }

    private static String spaced64(String bits) {
    // Format a 64-bit binary string for easier reading (split into sign, exponent, fraction)
        return bits.substring(0,1) + " " + bits.substring(1,12) + " " + bits.substring(12);
    }

    // Unpacking and packing binary64 values

    private static class Unpacked {
    // Helper class to hold unpacked binary64 fields
        int sign;           // 0/1
        int exp;            // unbiased exponent for normals; for subnormals we use 1-bias
        boolean isSub, isInf, isNaN;
        BigInteger mant;    // for normals: 53-bit (1.hhhh); for subnormals: up to 52-bit (0.hhhh)
    }

    private static Unpacked unpack(String bits64) {
    // Unpack a binary64 string into sign, exponent, mantissa, and flags for special cases
        Unpacked u = new Unpacked();
        u.sign = bits64.charAt(0) == '1' ? 1 : 0;
        long e = Long.parseLong(bits64.substring(1,12), 2);
        long f = new BigInteger(bits64.substring(12), 2).longValue();

        if (e == EXP_MAX) {
            u.isInf = (f == 0);
            u.isNaN = (f != 0);
            return u;
        }
        if (e == 0) {
            u.isSub = true;
            u.exp = 1 - EXP_BIAS;
            u.mant = BigInteger.valueOf(f); // no hidden 1
            return u;
        }
        u.exp = (int)(e - EXP_BIAS);
        long hidden1 = 1L << FRAC_BITS;
        u.mant = BigInteger.valueOf(hidden1 | f); // 53 bits
        return u;
    }

    // Pack with final normalization and round-to-nearest ties-to-even using G/R/S
    private static String pack(int sign, int unbiasedExp, BigInteger mantWithGRS, StringBuilder why) {
    // Pack sign, exponent, and mantissa (with GRS bits) into a binary64 string, with rounding and normalization
        // If mantissa is zero, return signed zero
        if (mantWithGRS.signum() == 0) return (sign==1?"1":"0") + "00000000000" + "0".repeat(52);

        // Normalize mantissa so leading 1 is at the correct position for binary64
        int targetTop = FRAC_BITS;                  // 52
        int bitLen = mantWithGRS.bitLength();       // includes GRS region
        int shift = (bitLen - 1) - (targetTop + EXT_BITS); // align so that [53 main | 3 GRS]

        if (shift > 0) {
            mantWithGRS = mantWithGRS.shiftRight(shift);
            unbiasedExp += shift;
            why.append("normalize:right(").append(shift).append(") ");
        } else if (shift < 0) {
            mantWithGRS = mantWithGRS.shiftLeft(-shift);
            unbiasedExp += shift;
            why.append("normalize:left(").append(-shift).append(") ");
        }

        // Split mantissa into main 53 bits and 3 rounding bits (guard, round, sticky)
        BigInteger main53 = mantWithGRS.shiftRight(EXT_BITS);
        int grs = mantWithGRS.and(BigInteger.valueOf(0b111)).intValue();
        boolean guard = (grs & 0b100) != 0;
        boolean round = (grs & 0b010) != 0;
        boolean sticky= (grs & 0b001) != 0;

        // Apply round-to-nearest, ties-to-even
        boolean lsbEven = !main53.testBit(0);
        boolean inc = false;
        if (guard) {
            if (round || sticky) inc = true;
            else if (!lsbEven)   inc = true; // .5 tie → to even
        }
        if (inc) {
            main53 = main53.add(BigInteger.ONE);
            if (main53.bitLength() > WORK_PRECISION) {
                main53 = main53.shiftRight(1);
                unbiasedExp += 1;
                why.append("round:carry ");
            }
        } else {
            why.append("round:keep ");
        }

        // Handle overflow and subnormal cases
        int eField = unbiasedExp + EXP_BIAS;
        if (eField >= (int)EXP_MAX) {
            // Overflow to infinity
            return (sign==1?"1":"0") + "11111111111" + "0".repeat(52);
        }
        if (eField <= 0) {
            // Subnormal packing: shift right by k and round again to 52 bits
            int k = 1 - eField; // steps below min normal exponent
            if (k >= WORK_PRECISION) {
                // Underflows to signed zero
                return (sign==1?"1":"0") + "00000000000" + "0".repeat(52);
            }
            // Shift main53 right by k with a minimal tie-even rule at the boundary
            BigInteger main = main53.shiftRight(k);
            BigInteger lost = main53.subtract(main.shiftLeft(k));
            boolean g = k>=1 && lost.testBit(k-1);
            boolean r = k>=2 && lost.testBit(k-2);
            boolean s = lost.clearBit(Math.max(0,k-1)).clearBit(Math.max(0,k-2)).signum()!=0;
            boolean lsbE = !main.testBit(0);
            boolean inc2 = false;
            if (g) {
                if (r||s) inc2 = true;
                else if (!lsbE) inc2 = true;
            }
            if (inc2) main = main.add(BigInteger.ONE);
            BigInteger frac = main.and(BigInteger.ONE.shiftLeft(FRAC_BITS).subtract(BigInteger.ONE));
            return (sign==1?"1":"0") + "00000000000" + padLeft(frac.toString(2), FRAC_BITS);
        }

        // Normal case: pack sign, exponent, and fraction into binary64 string
        BigInteger frac = main53.and(BigInteger.ONE.shiftLeft(FRAC_BITS).subtract(BigInteger.ONE));
        return (sign==1?"1":"0")
                + padLeft(Integer.toBinaryString(eField), 11)
                + padLeft(frac.toString(2), FRAC_BITS);
    }

    private static String padLeft(String s, int n) {
    // Pad a string with leading zeros to length n
        if (s.length() >= n) return s;
        return "0".repeat(n - s.length()) + s;
    }

    // Arithmetic operations: add, sub, mul, div

    private enum Op { ADD, SUB, MUL, DIV }
    // Supported arithmetic operations

    private static boolean isZero(Unpacked u) {
    // Check if an unpacked value is zero (not NaN or Inf)
        return !u.isNaN && !u.isInf && u.mant != null && u.mant.signum() == 0 && u.isSub;
    }

    private static String addOrSub(String aBits, String bBits, boolean subtractB, StringBuilder why) {
    // Addition and subtraction logic 
    // Simulate addition or subtraction of two binary64 strings, with exponent alignment and rounding
        Unpacked A = unpack(aBits);
        Unpacked B = unpack(bBits);
        if (subtractB) B.sign ^= 1;

        // Specials
    String sp = specialsAddLike(A, B);
        if (sp != null) return sp;

        // Handle zeros quickly
        if (isZero(A)) return setSign(bBits, B.sign);
        if (isZero(B)) return setSign(aBits, A.sign);

        // Normalize subnormals for the pipeline
        int eA = A.exp, eB = B.exp;
        BigInteger mA = A.isSub ? A.mant : A.mant; // if subnormal, mant has no hidden 1
        BigInteger mB = B.isSub ? B.mant : B.mant;

        // For normals, we already have 53 bits; for subnormals, bring them toward 53
        if (!A.isSub && mA.bitLength() < WORK_PRECISION) mA = mA.shiftLeft(WORK_PRECISION - mA.bitLength());
        if (!B.isSub && mB.bitLength() < WORK_PRECISION) mB = mB.shiftLeft(WORK_PRECISION - mB.bitLength());

        // Align exponents (right-shift smaller exponent’s mantissa with sticky)
        int exp = Math.max(eA, eB);
        BigInteger aAcc = mA.shiftLeft(EXT_BITS), bAcc = mB.shiftLeft(EXT_BITS);

        int dA = exp - eA, dB = exp - eB;
        if (dA > 0) aAcc = shiftRightWithSticky(aAcc, dA);
        if (dB > 0) bAcc = shiftRightWithSticky(bAcc, dB);
        why.append("align ");

        // Signed magnitude add/sub
        BigInteger res;
        int sign;
        if (A.sign == B.sign) {
            res = aAcc.add(bAcc);
            sign = A.sign;
            why.append("add ");
        } else {
            int cmp = aAcc.compareTo(bAcc);
            if (cmp == 0) {
                return (A.sign==1?"1":"0") + "00000000000" + "0".repeat(52); // exact +0 or -0 (choose +0)
            } else if (cmp > 0) {
                res = aAcc.subtract(bAcc);
                sign = A.sign;
                why.append("sub(A>B) ");
            } else {
                res = bAcc.subtract(aAcc);
                sign = B.sign;
                why.append("sub(B>A) ");
            }
        }
        if (res.signum() == 0) {
            return (sign==1?"1":"0") + "00000000000" + "0".repeat(52);
        }

        // Normalize accumulator so that after dropping EXT_BITS we have leading 1 at bit 52
        int top = res.bitLength() - 1;
        int wantTop = FRAC_BITS + EXT_BITS; // 52 + 3
        int s = top - wantTop;
        if (s > 0) { res = res.shiftRight(s); exp += s; why.append("normR(").append(s).append(") "); }
        else if (s < 0) { res = res.shiftLeft(-s); exp += s; why.append("normL(").append(-s).append(") "); }

        // Reattach GRS to feed pack()
        BigInteger main53 = res.shiftRight(EXT_BITS);
        BigInteger tail = res.subtract(main53.shiftLeft(EXT_BITS));
        BigInteger mantWithGRS = main53.shiftLeft(EXT_BITS).add(tail);

        String packed = pack(sign, exp, mantWithGRS, why);
        return packed;
    }

    private static BigInteger shiftRightWithSticky(BigInteger x, int k) {
    // Right-shift with sticky bit: if any lost bits are nonzero, set LSB
        if (k <= 0) return x;
        BigInteger main = x.shiftRight(k);
        BigInteger lost = x.subtract(main.shiftLeft(k));
        if (lost.signum() != 0) main = main.or(BigInteger.ONE); // fold sticky into LSB
        return main;
    }

    private static String specialsAddLike(Unpacked A, Unpacked B) {
    // Handle special cases for add/sub: NaN, Inf, etc.
        if (A.isNaN || B.isNaN) return qnan();
        if (A.isInf && B.isInf) {
            if (A.sign == B.sign) return inf(A.sign);
            return qnan();
        }
        if (A.isInf) return inf(A.sign);
        if (B.isInf) return inf(B.sign);
        return null;
    }

    private static String multiply(String aBits, String bBits, StringBuilder why) {
    // Multiplication logic
        Unpacked A = unpack(aBits);
        Unpacked B = unpack(bBits);

        // Specials
        if (A.isNaN || B.isNaN) return qnan();
        boolean aZero = isZero(A), bZero = isZero(B);
        if ((A.isInf && bZero) || (B.isInf && aZero)) return qnan();
        if (A.isInf || B.isInf) return inf(A.sign ^ B.sign);
        if (aZero || bZero) return signedZero(A.sign ^ B.sign);

        int sign = A.sign ^ B.sign;
        int eA = A.exp, eB = B.exp;

        BigInteger mA = A.isSub ? A.mant : A.mant;
        BigInteger mB = B.isSub ? B.mant : B.mant;
        if (!A.isSub && mA.bitLength() < WORK_PRECISION) mA = mA.shiftLeft(WORK_PRECISION - mA.bitLength());
        if (!B.isSub && mB.bitLength() < WORK_PRECISION) mB = mB.shiftLeft(WORK_PRECISION - mB.bitLength());

        int exp = eA + eB;

        // 53x53 -> up to 106 bits
        BigInteger prod = mA.multiply(mB);

        // Normalize so that after slicing we have main53|GRS
        int wantTop = (WORK_PRECISION * 2) - 1; // 105
        int top = prod.bitLength() - 1;
        int s = top - wantTop;
        if (s > 0) { prod = prod.shiftRight(s); exp += s; }
        else if (s < 0) { prod = prod.shiftLeft(-s); exp += s; }

        // Keep top 53, next 3 as GRS (fold remaining into sticky)
        int drop = (WORK_PRECISION * 2) - WORK_PRECISION; // 53
        BigInteger main53 = prod.shiftRight(drop);
        BigInteger tail = prod.subtract(main53.shiftLeft(drop));
        boolean g = tail.testBit(drop - 1);
        boolean r = drop >= 2 && tail.testBit(drop - 2);
        boolean st= tail.clearBit(drop - 1).clearBit(Math.max(0, drop - 2)).signum() != 0;

        BigInteger mantWithGRS = main53.shiftLeft(EXT_BITS)
                .add(BigInteger.valueOf((g?1:0)<<2 | (r?1:0)<<1 | (st?1:0)));

        return pack(sign, exp, mantWithGRS, why);
    }

    private static String divide(String aBits, String bBits, StringBuilder why) {
    // Division logic
        Unpacked A = unpack(aBits);
        Unpacked B = unpack(bBits);

        if (A.isNaN || B.isNaN) return qnan();
        boolean aZero = isZero(A), bZero = isZero(B);
        if (A.isInf && B.isInf) return qnan();
        if (aZero && bZero) return qnan();
        if (A.isInf) return inf(A.sign ^ B.sign);
        if (B.isInf) return signedZero(A.sign ^ B.sign);
        if (bZero) return inf(A.sign ^ B.sign);
        if (aZero) return signedZero(A.sign ^ B.sign);

        int sign = A.sign ^ B.sign;

        BigInteger mA = A.isSub ? A.mant : A.mant;
        BigInteger mB = B.isSub ? B.mant : B.mant;
        if (!A.isSub && mA.bitLength() < WORK_PRECISION) mA = mA.shiftLeft(WORK_PRECISION - mA.bitLength());
        if (!B.isSub && mB.bitLength() < WORK_PRECISION) mB = mB.shiftLeft(WORK_PRECISION - mB.bitLength());

        int exp = A.exp - B.exp;

        // Fixed-point division with headroom
        int K = WORK_PRECISION + EXT_BITS + 3;
        BigInteger numerator = mA.shiftLeft(K);
        BigInteger[] qr = numerator.divideAndRemainder(mB);
        BigInteger q = qr[0], r = qr[1];

        // Align q so that we can take main53|GRS
        int wantTop = WORK_PRECISION + EXT_BITS - 1;
        int top = q.bitLength() - 1;
        int s = top - wantTop;
        if (s > 0) { q = q.shiftRight(s); exp += s; }
        else if (s < 0) { q = q.shiftLeft(-s); exp += s; }

        BigInteger main53 = q.shiftRight(EXT_BITS);
        int grs = q.and(BigInteger.valueOf(0b111)).intValue();
        boolean g = (grs & 0b100) != 0;
        boolean rbit = (grs & 0b010) != 0;
        boolean st = (grs & 0b001) != 0 || r.signum()!=0;

        BigInteger mantWithGRS = main53.shiftLeft(EXT_BITS)
                .add(BigInteger.valueOf((g?1:0)<<2 | (rbit?1:0)<<1 | (st?1:0)));

        return pack(sign, exp, mantWithGRS, why);
    }

    // Miscellaneous utility methods

    private static String setSign(String bits, int sign) { return (sign==1?"1":"0") + bits.substring(1); }
    // Set the sign bit of a binary64 string

    private static String qnan() { return "0" + "1".repeat(11) + "1" + "0".repeat(51); }
    // Return a quiet NaN value in binary64
    private static String inf(int sign) { return (sign==1?"1":"0") + "1".repeat(11) + "0".repeat(52); }
    // Return infinity value in binary64
    private static String signedZero(int sign) { return (sign==1?"1":"0") + "0".repeat(63); }
    // Return signed zero in binary64

    // Compress verbose step logs into ≤5 lines
    private static String conciseWhy(String s) {
    // Summarize the step-by-step explanation for the output area
        // Translate technical step tokens to human-friendly explanations
        String msg = s.trim();
        if (msg.isEmpty()) return "Steps:\n- The numbers are aligned, operated on, normalized, and rounded.\n";
        String[] parts = msg.split("\\s+");
        StringBuilder b = new StringBuilder("Steps:\n");
        int lines = 0;
        StringBuilder line = new StringBuilder("- ");
        for (String p : parts) {
            String human;
            if (p.startsWith("align")) human = "The exponents are aligned so the numbers can be combined.";
            else if (p.equals("add")) human = "The mantissas are added together.";
            else if (p.equals("sub(A>B)")) human = "The first number is larger, so we subtract the second from the first.";
            else if (p.equals("sub(B>A)")) human = "The second number is larger, so we subtract the first from the second.";
            else if (p.equals("normR(1)") || p.startsWith("normR")) human = "The result is shifted right to normalize it.";
            else if (p.equals("normL(1)") || p.startsWith("normL")) human = "The result is shifted left to normalize it.";
            else if (p.startsWith("round:carry")) human = "Rounding caused a carry, so the result was adjusted.";
            else if (p.startsWith("round:keep")) human = "No rounding adjustment was needed.";
            else human = p;
            if (line.length() + human.length() + 1 > 72) {
                b.append(line).append('\n');
                lines++;
                if (lines == 4) break;
                line = new StringBuilder("- ");
            }
            line.append(human).append(' ');
        }
        if (lines < 5) b.append(line).append('\n');
        return b.toString();
    }
}
