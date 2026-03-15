package poker.game;

import poker.io.PlayerDataStore;
import poker.game.HandEvaluator.BestHand;
import poker.model.Card;
import poker.model.Bot;
import poker.model.Deck;
import poker.model.Person;
import poker.model.Player;
import poker.util.ConsoleIO;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GameEngine {
    private static final Path DATA_FILE = Path.of("data.txt");

    private final ConsoleIO io;
    private final PlayerDataStore dataStore;

    public GameEngine(ConsoleIO io) {
        this.io = io;
        this.dataStore = new PlayerDataStore(DATA_FILE);
    }

    public void runSingleplayer() {
        String name = io.readNonEmptyLine("Enter your name: ");
        Player player = dataStore.loadOrCreate(name);

        int botCount = io.readIntInRange("How many bots (0-7)? ", 0, 7);
        List<Person> participants = new ArrayList<>();
        participants.add(player);
        for (int i = 1; i <= botCount; i++) {
            participants.add(new Bot("Bot" + i));
        }

        io.println("Welcome, " + player.getName() + ". Coins: " + player.getCoins());

        boolean keepPlaying = true;
        while (keepPlaying) {
            if (player.getCoins() <= 0) {
                io.println("You have no coins left. Game over.");
                break;
            }
            if (participants.size() == 1) {
                io.println("No bots remain. You win by default.");
                break;
            }

            runRound(participants);

            // Remove busted bots.
            participants.removeIf(p -> (p instanceof Bot) && p.getCoins() <= 0);

            dataStore.save(player);
            keepPlaying = io.readYesNo("Play another round? (y/n): ");
        }

        dataStore.save(player);
        io.println("Thanks for playing.");
    }

    private void runRound(List<Person> participants) {
        Deck deck = new Deck();
        deck.shuffle();

        for (Person p : participants) {
            p.resetForRound();
        }

        // Deal 3 private cards to each player.
        for (int i = 0; i < 3; i++) {
            for (Person p : participants) {
                p.addCard(deck.drawCard());
            }
        }

        List<Card> community = new ArrayList<>(5);

        io.println("");
        io.println("New round:");
        io.println("Your cards: " + participants.get(0).handToString());

        int pot = 0;
        io.println("Community (PREFLOP): (none)");
        pot += BettingRound.run(io, "PREFLOP", participants);
        Person earlyWinner = soleActive(participants);
        if (earlyWinner != null) {
            earlyWinner.addCoins(pot);
            io.println("Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
            return;
        }

        // Flop: reveal 3 community cards.
        for (int i = 0; i < 3; i++) community.add(deck.drawCard());
        io.println("Your cards: " + participants.get(0).handToString());
        io.println("Community (FLOP): " + cardsToString(community));
        pot += BettingRound.run(io, "FLOP", participants);
        earlyWinner = soleActive(participants);
        if (earlyWinner != null) {
            earlyWinner.addCoins(pot);
            io.println("Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
            return;
        }

        // Turn: reveal 1.
        community.add(deck.drawCard());
        io.println("Your cards: " + participants.get(0).handToString());
        io.println("Community (TURN): " + cardsToString(community));
        pot += BettingRound.run(io, "TURN", participants);
        earlyWinner = soleActive(participants);
        if (earlyWinner != null) {
            earlyWinner.addCoins(pot);
            io.println("Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
            return;
        }

        // River: reveal 1.
        community.add(deck.drawCard());
        io.println("Your cards: " + participants.get(0).handToString());
        io.println("Community (RIVER): " + cardsToString(community));
        pot += BettingRound.run(io, "RIVER", participants);
        earlyWinner = soleActive(participants);
        if (earlyWinner != null) {
            earlyWinner.addCoins(pot);
            io.println("Everyone else folded. Winner: " + earlyWinner.getName() + " (+" + pot + ")");
            return;
        }

        List<Person> showdown = new ArrayList<>();
        for (Person p : participants) {
            if (!p.isFolded()) showdown.add(p);
        }

        List<Person> winners = HandEvaluator.determineWinnersWithCommunity(showdown, community);
        BestHand winning = bestHandFor(winners.get(0), community);

        io.println("Pot: " + pot);
        io.println("Winning hand: " + winning.result());

        if (winners.size() == 1) {
            Person winner = winners.get(0);
            winner.addCoins(pot);
            io.println("Winning cards: " + cardsToString(winning.cards()));
            io.println("Winner: " + winner.getName() + " (+" + pot + " coins)");
        } else {
            int share = pot / winners.size();
            int remainder = pot % winners.size();
            for (int i = 0; i < winners.size(); i++) {
                int payout = share + (i == 0 ? remainder : 0);
                winners.get(i).addCoins(payout);
            }
            io.println("Winning cards:");
            for (Person w : winners) {
                BestHand bh = bestHandFor(w, community);
                io.println("- " + w.getName() + ": " + cardsToString(bh.cards()) + " (" + bh.result() + ")");
            }
            io.println("Tie between: " + names(winners) + " (split pot)");
        }

        io.println("Balances:");
        for (Person p : participants) {
            io.println("- " + p.getName() + ": " + p.getCoins());
        }
    }

    private static Person soleActive(List<Person> participants) {
        Person active = null;
        for (Person p : participants) {
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

    private static String names(List<Person> people) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < people.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(people.get(i).getName());
        }
        return sb.toString();
    }
}
