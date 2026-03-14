# Design Document

## Program Structure

The program will follow object‑oriented design and use multiple classes
to represent cards, decks, and player hands.

## Classes

### Main Class

Responsibilities - Contains the main() method - Controls the game loop -
Handles user input - Displays results

### Card Class

Properties - rank - suit

Methods - getRank() - getSuit() - toString()

Purpose - Represents a single playing card

### Deck Class

Properties - ArrayList`<Card>`{=html} deck

Methods - createDeck() - shuffleDeck() - drawCard()

Purpose - Stores and manages the deck of cards

### Hand Class (Superclass)

Properties - ArrayList`<Card>`{=html} cards

Methods - addCard() - showHand()

Purpose - Represents a general card hand

### PlayerHand Class (Subclass)

Inherits from Hand

Additional Methods - evaluateHand()

Purpose - Evaluates the player's five‑card hand

## Inheritance Relationship

Hand → superclass\
PlayerHand → subclass

This allows polymorphism if other types of hands are added later.

## Data Structures

-   ArrayList`<Card>`{=html}
    -   used to store deck and hand
-   Arrays
    -   used to count card ranks for hand evaluation

## Program Flow

1.  Start program
2.  Ask for player name
3.  Shuffle deck
4.  Deal three cards to player
5.  Deal two additional cards
6.  Combine cards into a five‑card hand
7.  Evaluate hand
8.  Display results
9.  Ask if player wants to play again
10. End program when player exits
