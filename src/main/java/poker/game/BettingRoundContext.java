package poker.game;

public record BettingRoundContext(
        String street,
        int currentBet,
        int contributedThisStreet,
        int toCall,
        int maxTotalBetThisStreet
) {}

