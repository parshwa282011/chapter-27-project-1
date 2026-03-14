# Requirements Document

## Program Overview
The program is a console-based poker-style card game. A human player competes against computer-controlled bots. Each player receives cards and places bets using coins. The program evaluates hands and determines a winner each round.

Player information such as coin balance is stored in a file called `data.txt` so progress is saved between sessions.

## Functional Requirements

### Player Data System
- Player enters their name when the game starts.
- The program loads saved data from `data.txt`.
- If the player does not exist in the file, a new player profile is created.
- The file stores:
  - Player name
  - Coin balance
- Player data is saved automatically after each round.

Example data format:
PlayerName,1500

### Money System
- The player has coins used for betting.
- Coins increase when the player wins rounds.
- Coins decrease when the player loses bets.

### Bot Players
- Bots act as computer opponents.
- The player chooses how many bots to play against.
- Bots always start with **2000 coins**.
- Bots automatically place bets.
- Bots leave the game when their coins reach **0**.

### Deck System
- A standard 52-card deck is created.
- The deck is shuffled each round.

### Card Dealing
- Each participant receives **3 cards**.
- The system adds **2 additional cards** to complete a 5-card hand.

### Betting Phase
- Players place bets before the winner is decided.
- The player chooses how many coins to bet.
- Bots automatically place bets.
- All bets are added to a shared pot.

### Hand Evaluation
The program determines the best hand.

Possible hands include:
- Pair
- Two Pair
- Three of a Kind
- Straight
- Flush
- Full House
- High Card

### Game Loop
- The game runs multiple rounds.
- Bots are removed when they run out of coins.
- The game ends when:
  - The player quits
  - The player runs out of coins

## Non-Functional Requirements
- Program must run in a console environment.
- Program must handle invalid input safely.
- Player data must be saved using file storage.
- Program should follow object-oriented design principles.
