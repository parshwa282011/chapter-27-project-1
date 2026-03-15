package poker.util;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleIO {
    private final Scanner scanner = new Scanner(System.in);

    public void println(String s) {
        System.out.println(s);
    }

    public void print(String s) {
        System.out.print(s);
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            print(prompt);
            String line = readLineSafe();
            if (line == null) return "";
            line = line.trim();
            if (!line.isEmpty()) return line;
            println("Please enter a value.");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            print(prompt);
            String line = readLineSafe();
            if (line == null) return false;
            line = line.trim().toLowerCase(Locale.ROOT);
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            println("Please enter y or n.");
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            print(prompt);
            String line = readLineSafe();
            if (line == null) return min;
            line = line.trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                println("Enter a valid number.");
            }
        }
    }

    private String readLineSafe() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}

