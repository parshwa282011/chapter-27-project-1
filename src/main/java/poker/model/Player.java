package poker.model;

import poker.game.BetDecision;
import poker.game.BettingAction;
import poker.game.BettingRoundContext;
import poker.util.ConsoleIO;

public final class Player extends Person {
    public Player(String name, int coins) {
        super(name, coins);
    }

    public void loadData() {
        // Data loading is handled by poker.io.PlayerDataStore.
    }

    public void saveData() {
        // Data saving is handled by poker.io.PlayerDataStore.
    }

    @Override
    protected int chooseBet(ConsoleIO io, int maxBet) {
        io.println(getName() + " coins: " + getCoins());
        return io.readIntInRange("Place your bet (0-" + maxBet + "): ", 0, maxBet);
    }

    @Override
    public BetDecision decideBet(ConsoleIO io, BettingRoundContext ctx) {
        io.println("");
        io.println("Street: " + ctx.street());
        io.println("Coins: " + getCoins());

        if (ctx.toCall() > 0) {
            io.println("To call: " + ctx.toCall());
            io.println("1) Call");
            io.println("2) Raise");
            io.println("3) Fold");
            int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
            return switch (choice) {
                case 1 -> BetDecision.call(ctx.toCall());
                case 2 -> {
                    int maxTotal = ctx.maxTotalBetThisStreet();
                    int maxRaiseTo = Math.max(ctx.currentBet() + 1, maxTotal);
                    int raiseTo = io.readIntInRange("Raise to ("
                                    + (ctx.currentBet() + 1) + "-" + maxRaiseTo + "): ",
                            ctx.currentBet() + 1,
                            maxRaiseTo);
                    yield new BetDecision(BettingAction.RAISE, raiseTo - ctx.currentBet());
                }
                case 3 -> BetDecision.fold();
                default -> BetDecision.fold();
            };
        }

        if (ctx.currentBet() == 0) {
            io.println("1) Check");
            io.println("2) Bet");
            io.println("3) Fold");
            int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
            return switch (choice) {
                case 1 -> BetDecision.check();
                case 2 -> {
                    int betTo = io.readIntInRange("Bet amount (1-" + ctx.maxTotalBetThisStreet() + "): ",
                            1,
                            ctx.maxTotalBetThisStreet());
                    yield BetDecision.bet(betTo);
                }
                case 3 -> BetDecision.fold();
                default -> BetDecision.check();
            };
        }

        // Current bet > 0, but we're already matched.
        io.println("Current bet: " + ctx.currentBet());
        io.println("1) Check");
        io.println("2) Raise");
        io.println("3) Fold");
        int choice = io.readIntInRange("Choose (1-3): ", 1, 3);
        return switch (choice) {
            case 1 -> BetDecision.check();
            case 2 -> {
                int maxTotal = ctx.maxTotalBetThisStreet();
                int maxRaiseTo = Math.max(ctx.currentBet() + 1, maxTotal);
                int raiseTo = io.readIntInRange("Raise to ("
                                + (ctx.currentBet() + 1) + "-" + maxRaiseTo + "): ",
                        ctx.currentBet() + 1,
                        maxRaiseTo);
                yield new BetDecision(BettingAction.RAISE, raiseTo - ctx.currentBet());
            }
            case 3 -> BetDecision.fold();
            default -> BetDecision.check();
        };
    }
}
