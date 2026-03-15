package poker.game;

import poker.model.Card;
import poker.model.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class HandEvaluator {
    private HandEvaluator() {}

    // Design-document helpers (thin wrappers around evaluate()).
    public static boolean checkPair(List<Card> hand) {
        return evaluate(hand).rank() == HandRank.PAIR;
    }

    public static boolean checkTwoPair(List<Card> hand) {
        return evaluate(hand).rank() == HandRank.TWO_PAIR;
    }

    public static boolean checkFlush(List<Card> hand) {
        HandRank r = evaluate(hand).rank();
        return r == HandRank.FLUSH || r == HandRank.STRAIGHT_FLUSH;
    }

    public static boolean checkStraight(List<Card> hand) {
        HandRank r = evaluate(hand).rank();
        return r == HandRank.STRAIGHT || r == HandRank.STRAIGHT_FLUSH;
    }

    /**
     * Returns the winning person; if there is a tie, returns the first winner.
     * Use {@link #determineWinners(List)} when you need to handle ties.
     */
    public static Person determineWinner(List<Person> participants) {
        List<Person> winners = determineWinners(participants);
        return winners.isEmpty() ? null : winners.get(0);
    }

    public static HandResult evaluate(List<Card> hand) {
        if (hand.size() != 5) {
            throw new IllegalArgumentException("Hand must be 5 cards, got: " + hand.size());
        }

        int[] rankCounts = new int[15]; // index 2..14 (Ace high)
        Map<Card.Suit, Integer> suitCounts = new EnumMap<>(Card.Suit.class);
        for (Card.Suit s : Card.Suit.values()) suitCounts.put(s, 0);

        List<Integer> ranks = new ArrayList<>(5);
        for (Card c : hand) {
            int r = c.rank();
            rankCounts[r]++;
            suitCounts.put(c.suit(), suitCounts.get(c.suit()) + 1);
            ranks.add(r);
        }
        ranks.sort(Comparator.naturalOrder());

        boolean flush = suitCounts.values().stream().anyMatch(v -> v == 5);
        boolean straight = isStraight(ranks);

        // Group ranks by count, then by rank descending for tie-breakers.
        List<Group> groups = new ArrayList<>();
        for (int r = 14; r >= 2; r--) {
            if (rankCounts[r] > 0) groups.add(new Group(r, rankCounts[r]));
        }
        groups.sort((a, b) -> {
            int byCount = Integer.compare(b.count, a.count);
            if (byCount != 0) return byCount;
            return Integer.compare(b.rank, a.rank);
        });

        // Determine hand category with tie-breakers.
        if (straight && flush) {
            return new HandResult(HandRank.STRAIGHT_FLUSH, List.of(highCardForStraight(ranks)));
        }
        if (groups.get(0).count == 4) {
            int quadRank = groups.get(0).rank;
            int kicker = groups.get(1).rank;
            return new HandResult(HandRank.FOUR_OF_A_KIND, List.of(quadRank, kicker));
        }
        if (groups.get(0).count == 3 && groups.get(1).count == 2) {
            return new HandResult(HandRank.FULL_HOUSE, List.of(groups.get(0).rank, groups.get(1).rank));
        }
        if (flush) {
            return new HandResult(HandRank.FLUSH, highCardsDesc(ranks));
        }
        if (straight) {
            return new HandResult(HandRank.STRAIGHT, List.of(highCardForStraight(ranks)));
        }
        if (groups.get(0).count == 3) {
            int trips = groups.get(0).rank;
            List<Integer> kickers = new ArrayList<>();
            for (int i = 1; i < groups.size(); i++) kickers.add(groups.get(i).rank);
            return new HandResult(HandRank.THREE_OF_A_KIND, concat(List.of(trips), kickers));
        }
        if (groups.get(0).count == 2 && groups.get(1).count == 2) {
            int highPair = Math.max(groups.get(0).rank, groups.get(1).rank);
            int lowPair = Math.min(groups.get(0).rank, groups.get(1).rank);
            int kicker = groups.get(2).rank;
            return new HandResult(HandRank.TWO_PAIR, List.of(highPair, lowPair, kicker));
        }
        if (groups.get(0).count == 2) {
            int pair = groups.get(0).rank;
            List<Integer> kickers = new ArrayList<>();
            for (int i = 1; i < groups.size(); i++) kickers.add(groups.get(i).rank);
            return new HandResult(HandRank.PAIR, concat(List.of(pair), kickers));
        }

        return new HandResult(HandRank.HIGH_CARD, highCardsDesc(ranks));
    }

    /**
     * Computes the best possible 5-card hand from the given cards.
     * Used for variants where players have private cards plus community cards.
     */
    public static HandResult evaluateBest(List<Card> cards) {
        return bestHand(cards).result();
    }

    public record BestHand(HandResult result, List<Card> cards) {
        public BestHand {
            if (result == null) throw new IllegalArgumentException("result is required");
            if (cards == null || cards.size() != 5) throw new IllegalArgumentException("cards must be 5 cards");
            cards = List.copyOf(cards);
        }
    }

    public static BestHand bestHand(List<Card> cards) {
        if (cards.size() < 5) {
            throw new IllegalArgumentException("Need at least 5 cards, got: " + cards.size());
        }
        HandResult best = null;
        List<Card> bestCards = null;
        int n = cards.size();
        for (int a = 0; a < n - 4; a++) {
            for (int b = a + 1; b < n - 3; b++) {
                for (int c = b + 1; c < n - 2; c++) {
                    for (int d = c + 1; d < n - 1; d++) {
                        for (int e = d + 1; e < n; e++) {
                            List<Card> combo = List.of(cards.get(a), cards.get(b), cards.get(c), cards.get(d), cards.get(e));
                            HandResult r = evaluate(combo);
                            if (best == null || r.compareTo(best) > 0) {
                                best = r;
                                bestCards = combo;
                            }
                        }
                    }
                }
            }
        }
        return new BestHand(best, bestCards);
    }

    public static List<Person> determineWinners(List<Person> participants) {
        if (participants.isEmpty()) return List.of();

        List<Scored> scored = new ArrayList<>(participants.size());
        for (Person p : participants) {
            scored.add(new Scored(p, evaluate(p.getHand())));
        }

        scored.sort((a, b) -> b.result.compareTo(a.result));
        HandResult best = scored.get(0).result;

        List<Person> winners = new ArrayList<>();
        for (Scored s : scored) {
            if (s.result.compareTo(best) == 0) winners.add(s.person);
            else break;
        }
        return Collections.unmodifiableList(winners);
    }

    public static List<Person> determineWinnersWithCommunity(List<Person> participants, List<Card> community) {
        if (participants.isEmpty()) return List.of();

        List<Scored> scored = new ArrayList<>(participants.size());
        for (Person p : participants) {
            List<Card> all = new ArrayList<>(p.getHand().size() + community.size());
            all.addAll(p.getHand());
            all.addAll(community);
            scored.add(new Scored(p, bestHand(all).result()));
        }

        scored.sort((a, b) -> b.result.compareTo(a.result));
        HandResult best = scored.get(0).result;

        List<Person> winners = new ArrayList<>();
        for (Scored s : scored) {
            if (s.result.compareTo(best) == 0) winners.add(s.person);
            else break;
        }
        return Collections.unmodifiableList(winners);
    }

    private static boolean isStraight(List<Integer> ranksAsc) {
        // Handle wheel A-2-3-4-5.
        if (ranksAsc.equals(List.of(2, 3, 4, 5, 14))) return true;
        for (int i = 1; i < ranksAsc.size(); i++) {
            if (ranksAsc.get(i) != ranksAsc.get(i - 1) + 1) return false;
        }
        return true;
    }

    private static int highCardForStraight(List<Integer> ranksAsc) {
        // Wheel A-2-3-4-5 counts as 5-high straight.
        if (ranksAsc.equals(List.of(2, 3, 4, 5, 14))) return 5;
        return ranksAsc.get(ranksAsc.size() - 1);
    }

    private static List<Integer> highCardsDesc(List<Integer> ranksAsc) {
        List<Integer> out = new ArrayList<>(ranksAsc);
        out.sort(Comparator.reverseOrder());
        return out;
    }

    private static List<Integer> concat(List<Integer> a, List<Integer> b) {
        List<Integer> out = new ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    private record Group(int rank, int count) {}

    private record Scored(Person person, HandResult result) {}
}
