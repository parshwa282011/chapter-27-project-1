package poker.game;

public record BetDecision(BettingAction action, int amount) {
    public BetDecision {
        if (action == null) throw new IllegalArgumentException("action is required");
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
    }

    public static BetDecision fold() {
        return new BetDecision(BettingAction.FOLD, 0);
    }

    public static BetDecision check() {
        return new BetDecision(BettingAction.CHECK, 0);
    }

    public static BetDecision call(int amount) {
        return new BetDecision(BettingAction.CALL, amount);
    }

    public static BetDecision bet(int amount) {
        return new BetDecision(BettingAction.BET, amount);
    }

    public static BetDecision raise(int amount) {
        return new BetDecision(BettingAction.RAISE, amount);
    }
}

