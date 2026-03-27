package poker.game;

import java.util.List;
import java.util.Objects;

public final class HandResult implements Comparable<HandResult> {
    private final HandRank rank;
    private final List<Integer> tieBreakers;

    public HandResult(HandRank rank, List<Integer> tieBreakers) {
        this.rank = Objects.requireNonNull(rank, "rank");
        this.tieBreakers = List.copyOf(Objects.requireNonNull(tieBreakers, "tieBreakers"));
    }

    public HandRank rank() {
        return rank;
    }

    public List<Integer> tieBreakers() {
        return tieBreakers;
    }

    @Override
    public int compareTo(HandResult other) {
        int byRank = Integer.compare(this.rank.ordinal(), other.rank.ordinal());
        if (byRank != 0) return byRank;
        int n = Math.min(this.tieBreakers.size(), other.tieBreakers.size());
        for (int i = 0; i < n; i++) {
            int cmp = Integer.compare(this.tieBreakers.get(i), other.tieBreakers.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(this.tieBreakers.size(), other.tieBreakers.size());
    }

    @Override
    public String toString() {
        return rank + " " + tieBreakers;
    }
}

