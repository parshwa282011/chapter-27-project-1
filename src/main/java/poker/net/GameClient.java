package poker.net;

import poker.util.ConsoleIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class GameClient {
    private final ConsoleIO io;
    private final String host;
    private final int port;
    private final String name;
    private String myCards = null;
    private String community = "(none)";

    public GameClient(ConsoleIO io, String address, String name) {
        this.io = io;
        String[] parts = address.trim().split(":", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Address must be IP:PORT");
        this.host = parts[0].trim();
        this.port = Integer.parseInt(parts[1].trim());
        this.name = name;
    }

    public void run() {
        io.println("Connecting to " + host + ":" + port + " ...");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10_000);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                out.println("HELLO " + name);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("INFO ")) {
                        io.println(line.substring(5));
                    } else if (line.startsWith("CARDS ")) {
                        myCards = line.substring(6);
                        io.println("Your cards: " + myCards);
                    } else if (line.startsWith("COMMUNITY ")) {
                        // COMMUNITY <STREET> <cards...>
                        String rest = line.substring(10).trim();
                        int sp = rest.indexOf(' ');
                        if (sp > 0) {
                            String street = rest.substring(0, sp);
                            community = rest.substring(sp + 1).trim();
                            io.println("Community (" + street + "): " + community);
                        } else {
                            community = rest;
                            io.println("Community: " + community);
                        }
                    } else if (line.startsWith("REQ_ACTION ")) {
                        // REQ_ACTION <STREET> <currentBet> <contributed> <toCall> <maxTotal>
                        String[] parts = line.split(" ");
                        if (parts.length < 6) {
                            out.println("ACTION FOLD");
                            continue;
                        }
                        String street = parts[1];
                        int currentBet = parseInt(parts[2]);
                        int toCall = parseInt(parts[4]);
                        int maxTotal = parseInt(parts[5]);

                        io.println("");
                        io.println("Street: " + street);
                        if (myCards != null) io.println("Your cards: " + myCards);
                        io.println("Community: " + community);

                        if (toCall > 0) {
                            io.println("To call: " + toCall);
                            io.println("1) Call");
                            io.println("2) Raise");
                            io.println("3) Fold");
                            int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                            if (choice == 1) out.println("ACTION CALL");
                            else if (choice == 2) {
                                int raiseTo = io.readIntInRange("Raise to (" + (currentBet + 1) + "-" + maxTotal + "): ",
                                        currentBet + 1,
                                        maxTotal);
                                out.println("ACTION RAISE_TO " + raiseTo);
                            } else out.println("ACTION FOLD");
                        } else if (currentBet == 0) {
                            io.println("1) Check");
                            io.println("2) Bet");
                            io.println("3) Fold");
                            int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                            if (choice == 1) out.println("ACTION CHECK");
                            else if (choice == 2) {
                                int betTo = io.readIntInRange("Bet amount (1-" + maxTotal + "): ", 1, maxTotal);
                                out.println("ACTION BET_TO " + betTo);
                            } else out.println("ACTION FOLD");
                        } else {
                            io.println("Current bet: " + currentBet);
                            io.println("1) Check");
                            io.println("2) Raise");
                            io.println("3) Fold");
                            int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                            if (choice == 1) out.println("ACTION CHECK");
                            else if (choice == 2) {
                                int raiseTo = io.readIntInRange("Raise to (" + (currentBet + 1) + "-" + maxTotal + "): ",
                                        currentBet + 1,
                                        maxTotal);
                                out.println("ACTION RAISE_TO " + raiseTo);
                            } else out.println("ACTION FOLD");
                        }
                    } else if (line.startsWith("ROUND_END ")) {
                        io.println(line.substring(10));
                    } else if (line.equals("GAME_OVER")) {
                        io.println("Game over.");
                        return;
                    } else {
                        io.println(line);
                    }
                }
            }
        } catch (IOException e) {
            io.println("Connection error: " + e.getMessage());
        }
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
