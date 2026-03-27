package poker.model;

import poker.game.BetDecision;
import poker.game.BettingRoundContext;
import poker.util.ConsoleIO;

import java.util.Random;

public final class Bot extends Person {
    private static final int STARTING_COINS = 2000;
    private final Random random = new Random();

    public Bot(String name) {
        super(name, STARTING_COINS);
    }

    public int makeBet(int maxBet) {
        // Simple heuristic: bet up to 10% of coins, at least 1.
        int cap = Math.max(1, Math.min(maxBet, Math.max(1, getCoins() / 10)));
        return random.nextInt(cap + 1);
    }

    public String chooseAction() {
        // Placeholder for future expansion (fold/call/raise). Current game uses one bet per round.
        return "BET";
    }

    @Override
    protected int chooseBet(ConsoleIO io, int maxBet) {
        int bet = makeBet(maxBet);
        io.println(getName() + " bets " + bet);
        return bet;
    }

    @Override
    public BetDecision decideBet(ConsoleIO io, BettingRoundContext ctx) {
        if (isFolded() || getCoins() <= 0) return BetDecision.fold();

        // Very simple bot: mostly calls/checks; sometimes raises small; occasionally folds if the call is big.
        if (ctx.toCall() > 0) {
            int call = ctx.toCall();
            if (call > Math.max(10, getCoins() / 5) && random.nextInt(100) < 25) {
                io.println(getName() + " folds");
                return BetDecision.fold();
            }
            if (random.nextInt(100) < 10 && ctx.maxTotalBetThisStreet() > ctx.currentBet() + 1) {
                int raiseBy = 1 + random.nextInt(Math.min(25, Math.max(1, ctx.maxTotalBetThisStreet() - ctx.currentBet())));
                io.println(getName() + " raises");
                return BetDecision.raise(raiseBy);
            }
            io.println(getName() + " calls");
            return BetDecision.call(call);
        }

        if (ctx.currentBet() == 0) {
            if (random.nextInt(100) < 20 && ctx.maxTotalBetThisStreet() >= 5) {
                int bet = 1 + random.nextInt(Math.min(25, ctx.maxTotalBetThisStreet()));
                io.println(getName() + " bets " + bet);
                return BetDecision.bet(bet);
            }
            io.println(getName() + " checks");
            return BetDecision.check();
        }

        // Matched a bet; sometimes raises.
        if (random.nextInt(100) < 10 && ctx.maxTotalBetThisStreet() > ctx.currentBet() + 1) {
            int raiseBy = 1 + random.nextInt(Math.min(25, Math.max(1, ctx.maxTotalBetThisStreet() - ctx.currentBet())));
            io.println(getName() + " raises");
            return BetDecision.raise(raiseBy);
        }
        io.println(getName() + " checks");
        return BetDecision.check();
    }
}
