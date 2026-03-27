package poker;

import poker.game.GameEngine;
import poker.net.GameClient;
import poker.net.GameServer;
import poker.util.ConsoleIO;

public final class Main {
    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO();

        io.println("Poker-Style Card Game");
        io.println("1) Singleplayer (vs bots)");
        io.println("2) Host multiplayer game");
        io.println("3) Join multiplayer game");

        int mode = io.readIntInRange("Choose mode (1-3): ", 1, 3);
        switch (mode) {
            case 1 -> new GameEngine(io).runSingleplayer();
            case 2 -> host(io);
            case 3 -> join(io);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        }
    }

    private static void host(ConsoleIO io) {
        int port = io.readIntInRange("Port to host on (1024-65535): ", 1024, 65535);
        int startingCoins = io.readIntInRange("Starting coins for all players (1-100000): ", 1, 100000);
        int expectedPlayers = io.readIntInRange("Number of total players (including host) (2-8): ", 2, 8);

        io.println("Hosting. Share your IP and port with others (format IP:PORT).");
        io.println("If they're on the same Wi-Fi/LAN: share your local IP (usually 192.168.x.x or 10.x.x.x) + the port.");
        io.println("If they're NOT on the same Wi-Fi/LAN: you'll need to port-forward this port on your router and share your public IP + the port.");
        io.println("Waiting for players to connect...");

        GameServer server = new GameServer(io, port, startingCoins, expectedPlayers);
        server.run();
    }

    private static void join(ConsoleIO io) {
        String name = io.readNonEmptyLine("Your name: ");
        String address = io.readNonEmptyLine("Server address (IP:PORT): ");
        GameClient client = new GameClient(io, address, name);
        client.run();
    }
}
