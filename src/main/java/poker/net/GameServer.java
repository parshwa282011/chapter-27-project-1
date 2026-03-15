package poker.net;

import poker.game.HandEvaluator;
import poker.game.HandResult;
import poker.game.BetDecision;
import poker.game.BettingRoundContext;
import poker.game.BettingRound;
import poker.game.HandEvaluator.BestHand;
import poker.model.Card;
import poker.model.Deck;
import poker.model.Person;
import poker.util.ConsoleIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class GameServer {
    private final ConsoleIO io;
    private final int port;
    private final int startingCoins;
    private final int expectedPlayers;

    public GameServer(ConsoleIO io, int port, int startingCoins, int expectedPlayers) {
        this.io = io;
        this.port = port;
        this.startingCoins = startingCoins;
        this.expectedPlayers = expectedPlayers;
    }

    public void run() {
        String hostName = io.readNonEmptyLine("Host player name: ");
        List<NetPlayer> players = new ArrayList<>();
        players.add(NetPlayer.local(hostName, startingCoins));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (players.size() < expectedPlayers) {
                Socket socket = serverSocket.accept();
                NetPlayer remote = NetPlayer.fromSocket(socket, startingCoins);
                players.add(remote);
                broadcast(players, "INFO " + remote.getName() + " joined (" + players.size() + "/" + expectedPlayers + ")");
            }

            broadcast(players, "INFO All players connected. Starting game.");
            runGame(players);
        } catch (IOException e) {
            io.println("Server error: " + e.getMessage());
        } finally {
            for (NetPlayer p : players) p.closeQuietly();
        }
    }

    private void runGame(List<NetPlayer> players) {
        boolean keepPlaying = true;
        while (keepPlaying) {
            players.removeIf(p -> p.getCoins() <= 0);
            if (players.size() < 2) {
                broadcast(players, "INFO Not enough players to continue.");
                broadcast(players, "GAME_OVER");
                return;
            }

            Deck deck = new Deck();
            deck.shuffle();
            for (NetPlayer p : players) p.resetForRound();

            // Deal 3 private cards.
            for (int i = 0; i < 3; i++) for (NetPlayer p : players) p.addCard(deck.drawCard());

            for (NetPlayer p : players) {
                p.send("CARDS " + p.handToString());
            }
            // Host sees their cards on the server console.
            io.println("");
            io.println("New round:");
            io.println("Your cards: " + players.get(0).handToString());
            io.println("Community (PREFLOP): (none)");

            List<Card> community = new ArrayList<>(5);

            int pot = 0;
            pot += BettingRound.run(io, "PREFLOP", players);
            NetPlayer earlyWinner = soleActive(players);
            if (earlyWinner != null) {
                earlyWinner.addCoins(pot);
                broadcast(players, "ROUND_END Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
                keepPlaying = io.readYesNo("Play another round? (y/n): ");
                if (!keepPlaying) broadcast(players, "GAME_OVER");
                continue;
            }

            // Flop reveal (3).
            for (int i = 0; i < 3; i++) community.add(deck.drawCard());
            String flop = cardsToString(community);
            broadcast(players, "COMMUNITY FLOP " + flop);
            io.println("Community (FLOP): " + flop);
            io.println("Your cards: " + players.get(0).handToString());
            pot += BettingRound.run(io, "FLOP", players);
            earlyWinner = soleActive(players);
            if (earlyWinner != null) {
                earlyWinner.addCoins(pot);
                broadcast(players, "ROUND_END Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
                keepPlaying = io.readYesNo("Play another round? (y/n): ");
                if (!keepPlaying) broadcast(players, "GAME_OVER");
                continue;
            }

            // Turn reveal (1).
            community.add(deck.drawCard());
            String turn = cardsToString(community);
            broadcast(players, "COMMUNITY TURN " + turn);
            io.println("Community (TURN): " + turn);
            io.println("Your cards: " + players.get(0).handToString());
            pot += BettingRound.run(io, "TURN", players);
            earlyWinner = soleActive(players);
            if (earlyWinner != null) {
                earlyWinner.addCoins(pot);
                broadcast(players, "ROUND_END Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
                keepPlaying = io.readYesNo("Play another round? (y/n): ");
                if (!keepPlaying) broadcast(players, "GAME_OVER");
                continue;
            }

            // River reveal (1).
            community.add(deck.drawCard());
            String river = cardsToString(community);
            broadcast(players, "COMMUNITY RIVER " + river);
            io.println("Community (RIVER): " + river);
            io.println("Your cards: " + players.get(0).handToString());
            pot += BettingRound.run(io, "RIVER", players);
            earlyWinner = soleActive(players);
            if (earlyWinner != null) {
                earlyWinner.addCoins(pot);
                broadcast(players, "ROUND_END Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
                keepPlaying = io.readYesNo("Play another round? (y/n): ");
                if (!keepPlaying) broadcast(players, "GAME_OVER");
                continue;
            }

            List<Person> showdown = new ArrayList<>();
            for (NetPlayer p : players) if (!p.isFolded()) showdown.add(p);

            List<Person> winners = HandEvaluator.determineWinnersWithCommunity(showdown, community);
            BestHand winning = bestHandFor(winners.get(0), community);

            if (winners.size() == 1) {
                Person winner = winners.get(0);
                winner.addCoins(pot);
                broadcast(players, "ROUND_END Winner: " + winner.getName()
                        + " with " + winning.result()
                        + " using " + cardsToString(winning.cards())
                        + " (+" + pot + ")");
            } else {
                int share = pot / winners.size();
                int remainder = pot % winners.size();
                for (int i = 0; i < winners.size(); i++) {
                    int payout = share + (i == 0 ? remainder : 0);
                    winners.get(i).addCoins(payout);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("ROUND_END Tie: ").append(joinNames(winners))
                        .append(" with ").append(winning.result())
                        .append(" (split ").append(pot).append("). ");
                sb.append("Winning cards: ");
                for (int i = 0; i < winners.size(); i++) {
                    Person w = winners.get(i);
                    BestHand bh = bestHandFor(w, community);
                    if (i > 0) sb.append(" | ");
                    sb.append(w.getName()).append(": ").append(cardsToString(bh.cards()));
                }
                broadcast(players, sb.toString());
            }

            broadcast(players, "INFO Balances: " + balances(players));
            keepPlaying = io.readYesNo("Play another round? (y/n): ");
            if (!keepPlaying) broadcast(players, "GAME_OVER");
        }
    }

    private static void broadcast(List<NetPlayer> players, String msg) {
        for (NetPlayer p : players) p.send(msg);
    }

    private static NetPlayer soleActive(List<NetPlayer> players) {
        NetPlayer active = null;
        for (NetPlayer p : players) {
            if (p.isFolded()) continue;
            if (active != null) return null;
            active = p;
        }
        return active;
    }

    private static String cardsToString(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(cards.get(i));
        }
        return sb.toString();
    }

    private static BestHand bestHandFor(Person p, List<Card> community) {
        List<Card> all = new ArrayList<>(p.getHand().size() + community.size());
        all.addAll(p.getHand());
        all.addAll(community);
        return HandEvaluator.bestHand(all);
    }

    private static String joinNames(List<Person> people) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < people.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(people.get(i).getName());
        }
        return sb.toString();
    }

    private static String balances(List<NetPlayer> players) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) sb.append(" | ");
            NetPlayer p = players.get(i);
            sb.append(p.getName()).append("=").append(p.getCoins());
        }
        return sb.toString();
    }

    private static final class NetPlayer extends Person {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private final BlockingQueue<String> inbox;
        private final boolean local;

        private NetPlayer(String name, int coins, Socket socket, BufferedReader in, PrintWriter out, BlockingQueue<String> inbox, boolean local) {
            super(name, coins);
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.inbox = inbox;
            this.local = local;
        }

        static NetPlayer local(String name, int coins) {
            return new NetPlayer(name, coins, null, null, null, null, true);
        }

        static NetPlayer fromSocket(Socket socket, int coins) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            socket.setSoTimeout(0);
            String hello = in.readLine();
            if (hello == null || !hello.startsWith("HELLO ")) {
                throw new IOException("Client did not send HELLO");
            }
            String name = hello.substring(6).trim();
            if (name.isEmpty()) throw new IOException("Client name empty");

            BlockingQueue<String> inbox = new ArrayBlockingQueue<>(64);
            NetPlayer p = new NetPlayer(name, coins, socket, in, out, inbox, false);
            Thread t = new Thread(() -> p.readLoop(), "client-" + name);
            t.setDaemon(true);
            t.start();
            p.send("INFO Connected as " + name);
            return p;
        }

        void send(String msg) {
            if (local) return;
            out.println(msg);
        }

        void closeQuietly() {
            if (local) return;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        @Override
        protected int chooseBet(ConsoleIO io, int maxBet) {
            if (local) {
                io.println(getName() + " coins: " + getCoins());
                return io.readIntInRange("Place your bet (0-" + maxBet + "): ", 0, maxBet);
            }
            send("REQ_BET " + maxBet);
            String msg;
            try {
                msg = inbox.poll(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 0;
            }
            if (msg == null) return 0;
            if (!msg.startsWith("BET ")) return 0;
            try {
                int bet = Integer.parseInt(msg.substring(4).trim());
                return Math.max(0, Math.min(bet, maxBet));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        public BetDecision decideBet(ConsoleIO io, BettingRoundContext ctx) {
            if (isFolded() || getCoins() <= 0) return BetDecision.fold();

            if (local) {
                io.println("");
                io.println("Street: " + ctx.street());
                io.println("Coins: " + getCoins());

                if (ctx.toCall() > 0) {
                    io.println("To call: " + ctx.toCall());
                    io.println("1) Call");
                    io.println("2) Raise");
                    io.println("3) Fold");
                    int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                    if (choice == 1) return BetDecision.call(ctx.toCall());
                    if (choice == 2) {
                        int maxRaiseTo = Math.max(ctx.currentBet() + 1, ctx.maxTotalBetThisStreet());
                        int raiseTo = io.readIntInRange("Raise to (" + (ctx.currentBet() + 1) + "-" + maxRaiseTo + "): ",
                                ctx.currentBet() + 1,
                                maxRaiseTo);
                        return BetDecision.raise(raiseTo - ctx.currentBet());
                    }
                    return BetDecision.fold();
                }

                if (ctx.currentBet() == 0) {
                    io.println("1) Check");
                    io.println("2) Bet");
                    io.println("3) Fold");
                    int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                    if (choice == 1) return BetDecision.check();
                    if (choice == 2) {
                        int betTo = io.readIntInRange("Bet amount (1-" + ctx.maxTotalBetThisStreet() + "): ",
                                1,
                                ctx.maxTotalBetThisStreet());
                        return BetDecision.bet(betTo);
                    }
                    return BetDecision.fold();
                }

                io.println("Current bet: " + ctx.currentBet());
                io.println("1) Check");
                io.println("2) Raise");
                io.println("3) Fold");
                int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
                if (choice == 1) return BetDecision.check();
                if (choice == 2) {
                    int maxRaiseTo = Math.max(ctx.currentBet() + 1, ctx.maxTotalBetThisStreet());
                    int raiseTo = io.readIntInRange("Raise to (" + (ctx.currentBet() + 1) + "-" + maxRaiseTo + "): ",
                            ctx.currentBet() + 1,
                            maxRaiseTo);
                    return BetDecision.raise(raiseTo - ctx.currentBet());
                }
                return BetDecision.fold();
            }

            // Remote: request an action from the client.
            send("REQ_ACTION " + ctx.street() + " " + ctx.currentBet() + " " + ctx.contributedThisStreet()
                    + " " + ctx.toCall() + " " + ctx.maxTotalBetThisStreet());

            String msg;
            try {
                msg = inbox.poll(90, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return BetDecision.fold();
            }
            if (msg == null) return BetDecision.fold();
            msg = msg.trim();
            if (!msg.startsWith("ACTION ")) return BetDecision.fold();

            String[] parts = msg.split(" ");
            if (parts.length < 2) return BetDecision.fold();

            String action = parts[1];
            return switch (action) {
                case "FOLD" -> BetDecision.fold();
                case "CHECK" -> BetDecision.check();
                case "CALL" -> BetDecision.call(ctx.toCall());
                case "BET_TO" -> {
                    int betTo = (parts.length >= 3) ? parseInt(parts[2]) : 0;
                    yield BetDecision.bet(betTo);
                }
                case "RAISE_TO" -> {
                    int raiseTo = (parts.length >= 3) ? parseInt(parts[2]) : ctx.currentBet() + 1;
                    yield BetDecision.raise(Math.max(1, raiseTo - ctx.currentBet()));
                }
                default -> BetDecision.fold();
            };
        }

        private void readLoop() {
            Objects.requireNonNull(in);
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    inbox.offer(line);
                }
            } catch (IOException ignored) {
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
}
