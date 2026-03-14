# Requirements Document

## Program Overview

The Custom 3‑Card Draw Card Game is a simple Java program that simulates
a card game. The player receives three cards and the system adds two
additional cards to form a five‑card hand. The program evaluates the
hand and tells the player what type of hand they received.

## Functional Requirements

### User Input

-   Player enters their name
-   Player chooses whether to start a new round or exit the game

### Game Behavior

-   Program creates a standard 52‑card deck
-   Deck is shuffled before dealing
-   Player receives three cards
-   Program deals two additional cards
-   The five‑card hand is evaluated

### Hand Evaluation

The program must detect: - Pair - Two Pair - Three of a Kind -
Straight - Flush - High Card

### Program Output

The program displays: - Player name - The five cards in the final hand -
The type of hand achieved

### Game Loop

-   The player can choose to play multiple rounds
-   The game continues until the player exits

## Non‑Functional Requirements

-   Program must run in the console
-   Program should be easy to understand and use
-   Program must handle invalid input without crashing
