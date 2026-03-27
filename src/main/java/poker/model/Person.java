package poker.model;

import poker.game.BetDecision;
import poker.game.BettingRoundContext;
import poker.util.ConsoleIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class Person {
    private final String name;
    private int coins;
    private final ArrayList<Card> hand = new ArrayList<>(5);
    private boolean folded;

    protected Person(String name, int coins) {
        this.name = Objects.requireNonNull(name, "name");
        this.coins = Math.max(0, coins);
    }

    public String getName() {
        return name;
    }

    public int getCoins() {
        return coins;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public void addCard(Card card) {
        if (hand.size() >= 5) throw new IllegalStateException("Hand already has 5 cards");
        hand.add(Objects.requireNonNull(card, "card"));
    }

    public void clearHand() {
        hand.clear();
    }

    public boolean isFolded() {
        return folded;
    }

    public void fold() {
        folded = true;
    }

    public void resetForRound() {
        folded = false;
        clearHand();
    }

    public void removeCoins(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        coins = Math.max(0, coins - amount);
    }

    public void addCoins(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        coins += amount;
    }

    public String handToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(hand.get(i));
        }
        return sb.toString();
    }

    public final int placeBet(ConsoleIO io) {
        if (coins <= 0) return 0;
        int bet = chooseBet(io, coins);
        bet = Math.max(0, Math.min(bet, coins));
        removeCoins(bet);
        return bet;
    }

    protected abstract int chooseBet(ConsoleIO io, int maxBet);

    /**
     * Decision used by the multi-street betting rounds.
     */
    public abstract BetDecision decideBet(ConsoleIO io, BettingRoundContext ctx);
}
