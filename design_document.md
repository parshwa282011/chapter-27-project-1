# Design Document

## Program Architecture
The program will follow an object-oriented structure using multiple interacting classes.

## Classes

### Main
Responsibilities:
- Contains the `main()` method
- Controls the game loop
- Loads and saves player data
- Starts new rounds

---

### Person (Superclass)

Properties:
- name
- coins
- hand (ArrayList of Card)

Methods:
- addCard()
- placeBet()
- getCoins()
- removeCoins()

Purpose:
Represents any participant in the game.

---

### Player (Subclass of Person)

Responsibilities:
- Interacts with the user
- Handles betting decisions
- Saves and loads player data

Methods:
- loadData()
- saveData()
- chooseBet()

Data is stored in `data.txt`.

---

### Bot (Subclass of Person)

Characteristics:
- Automated behavior
- Starts with **2000 coins**

Methods:
- makeBet()
- chooseAction()

Behavior:
- Bots automatically place bets.
- Bots leave the game when coins reach **0**.

---

### Card

Properties:
- rank
- suit

Methods:
- getRank()
- getSuit()
- toString()

Purpose:
Represents a single playing card.

---

### Deck

Properties:
- ArrayList<Card> cards

Methods:
- createDeck()
- shuffle()
- drawCard()

Purpose:
Manages card storage and dealing.

---

### HandEvaluator

Responsibilities:
- Analyze card combinations
- Determine winning hands

Methods:
- checkPair()
- checkTwoPair()
- checkFlush()
- checkStraight()
- determineWinner()

---

## Data Structures

ArrayList<Card>
- Stores cards in the deck and player hands.

ArrayList<Person>
- Stores all players and bots in the game.

Arrays
- Used for counting card ranks during hand evaluation.

---

## Program Flow

1. Start program
2. Load player data from `data.txt`
3. Ask how many bots to play against
4. Create bot players with 2000 coins
5. Create and shuffle the deck
6. Deal cards to all participants
7. Begin betting phase
8. Evaluate hands
9. Determine winner and distribute coins
10. Remove bots with zero coins
11. Save player data
12. Ask player if they want to play another round
