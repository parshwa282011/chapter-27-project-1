package poker.model;

import java.util.Objects;

public final class Card {
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    private final int rank; // 2..14, where 14 = Ace
    private final Suit suit;

    public Card(int rank, Suit suit) {
        if (rank < 2 || rank > 14) throw new IllegalArgumentException("rank must be 2..14");
        this.rank = rank;
        this.suit = Objects.requireNonNull(suit, "suit");
    }

    public int rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    public String getRank() {
        return switch (rank) {
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            case 14 -> "A";
            default -> Integer.toString(rank);
        };
    }

    public String getSuit() {
        return switch (suit) {
            case CLUBS -> "C";
            case DIAMONDS -> "D";
            case HEARTS -> "H";
            case SPADES -> "S";
        };
    }

    @Override
    public String toString() {
        return getRank() + getSuit();
    }
}

