package poker.game;

import poker.model.Person;
import poker.util.ConsoleIO;

import java.util.List;

public final class BettingRound {
    private BettingRound() {}

    /**
     * Runs one betting street (preflop/flop/turn/river).
     *
     * Simplifications:
     * - No side pots / all-in handling. Bets are capped so every active player can match.
     * - Multiple raises are allowed, using a simple "passes until stable" loop.
     */
    public static int run(ConsoleIO io, String street, List<? extends Person> players) {
        int n = players.size();
        int[] contributed = new int[n];

        int currentBet = 0;
        int pot = 0;

        while (true) {
            boolean changed = false;
            int capTotal = computeCapTotal(players, contributed);

            for (int i = 0; i < n; i++) {
                Person p = players.get(i);
                if (p.isFolded() || p.getCoins() <= 0) continue;

                int toCall = Math.max(0, currentBet - contributed[i]);

                BettingRoundContext ctx = new BettingRoundContext(
                        street,
                        currentBet,
                        contributed[i],
                        toCall,
                        capTotal
                );

                BetDecision decision = p.decideBet(io, ctx);
                if (decision == null) decision = BetDecision.fold();

                switch (decision.action()) {
                    case FOLD -> {
                        p.fold();
                    }
                    case CHECK -> {
                        if (toCall != 0) {
                            // Can't check when facing a bet; treat as call.
                            int paid = pay(p, toCall);
                            contributed[i] += paid;
                            pot += paid;
                        }
                    }
                    case CALL -> {
                        int paid = pay(p, toCall);
                        contributed[i] += paid;
                        pot += paid;
                    }
                    case BET -> {
                        if (currentBet != 0) {
                            // Can't bet into an existing bet; treat as call.
                            int paid = pay(p, toCall);
                            contributed[i] += paid;
                            pot += paid;
                            break;
                        }
                        int desired = Math.max(1, decision.amount());
                        int targetTotal = clamp(desired, 1, capTotal);
                        int add = targetTotal - contributed[i];
                        if (add <= 0) break;
                        int paid = pay(p, add);
                        contributed[i] += paid;
                        pot += paid;
                        currentBet = Math.max(currentBet, contributed[i]);
                        changed = true;
                    }
                    case RAISE -> {
                        if (currentBet == 0) {
                            // Raising from 0 is a bet.
                            int desired = Math.max(1, decision.amount());
                            int targetTotal = clamp(desired, 1, capTotal);
                            int add = targetTotal - contributed[i];
                            if (add <= 0) break;
                            int paid = pay(p, add);
                            contributed[i] += paid;
                            pot += paid;
                            currentBet = Math.max(currentBet, contributed[i]);
                            changed = true;
                            break;
                        }

                        int desiredTotal = currentBet + Math.max(1, decision.amount());
                        int targetTotal = clamp(desiredTotal, currentBet + 1, capTotal);
                        int add = targetTotal - contributed[i];
                        if (add <= 0) break;
                        int paid = pay(p, add);
                        contributed[i] += paid;
                        pot += paid;
                        if (contributed[i] > currentBet) {
                            currentBet = contributed[i];
                            changed = true;
                        }
                    }
                }

                if (countActive(players) <= 1) return pot;
            }

            if (currentBet == 0 && !changed) return pot; // everyone checked
            if (!changed && allMatched(players, contributed, currentBet)) return pot;
            // Otherwise, another pass to allow calls/folds after a raise.
        }
    }

    private static int computeCapTotal(List<? extends Person> players, int[] contributed) {
        int cap = Integer.MAX_VALUE;
        for (int i = 0; i < players.size(); i++) {
            Person p = players.get(i);
            if (p.isFolded() || p.getCoins() <= 0) continue;
            cap = Math.min(cap, contributed[i] + p.getCoins());
        }
        return cap == Integer.MAX_VALUE ? 0 : cap;
    }

    private static boolean allMatched(List<? extends Person> players, int[] contributed, int currentBet) {
        for (int i = 0; i < players.size(); i++) {
            Person p = players.get(i);
            if (p.isFolded() || p.getCoins() <= 0) continue;
            if (contributed[i] != currentBet) return false;
        }
        return true;
    }

    private static int countActive(List<? extends Person> players) {
        int c = 0;
        for (Person p : players) {
            if (!p.isFolded() && p.getCoins() > 0) c++;
        }
        return c;
    }

    private static int pay(Person p, int amount) {
        int paid = Math.max(0, Math.min(amount, p.getCoins()));
        p.removeCoins(paid);
        return paid;
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}

