# Testing Plan

## Overview
This testing plan describes how the poker-style card game will be tested to verify that all features work correctly. The tests will check gameplay logic, player data storage, betting behavior, bots, and multiplayer networking. Testing will be performed using manual testing and repeated game runs.

## Testing Environment

Software
- Java Runtime Environment
- Java IDE or command line compiler

Hardware
- One computer for singleplayer testing
- Two or more computers or terminals for multiplayer testing

Files
- data.txt located in the same directory as the program

---

## Test Categories

- Program Startup
- Player Data System
- Card and Deck System
- Betting System
- Bot Behavior
- Hand Evaluation
- Multiplayer Networking
- Game Loop and Edge Cases

---

## Test Cases

### Test 1: Program Startup

Objective
Verify that the program starts correctly and displays the game mode menu.

Steps
- Run the program
- Observe the startup menu

Expected Result
The program displays options:
- Singleplayer
- Host Game
- Join Game

---

### Test 2: New Player Data Creation

Objective
Verify that a new player profile is created.

Steps
- Delete data.txt if it exists
- Start the program
- Enter a new player name

Expected Result
- Player profile is created
- Player starts with default coins
- data.txt file is created

---

### Test 3: Player Data Saving

Objective
Verify that player coin data is saved.

Steps
- Play one round
- Win or lose coins
- Exit the program
- Open data.txt

Expected Result
The player's updated coin amount appears in the file.

Example
PlayerName,1450

---

### Test 4: Player Data Loading

Objective
Verify that saved player data loads correctly.

Steps
- Start program with an existing data.txt
- Enter the saved player name

Expected Result
The program loads the saved coin balance.

---

### Test 5: Deck Creation

Objective
Verify that the deck contains 52 unique cards.

Steps
- Create a new deck
- Count all cards

Expected Result
- Deck contains 52 cards
- No duplicates exist

---

### Test 6: Deck Shuffle

Objective
Verify that cards are randomized.

Steps
- Create a deck
- Shuffle it
- Draw several cards

Expected Result
Cards appear in random order.

---

### Test 7: Card Dealing

Objective
Verify that each player receives cards.

Steps
- Start a round
- Deal cards to players

Expected Result
Each player receives exactly 5 cards.

---

### Test 8: Betting System

Objective
Verify that betting works correctly.

Steps
- Start a round
- Player places a bet
- Bots place bets

Expected Result
- Coins decrease correctly
- Pot increases correctly

---

### Test 9: Pot Distribution

Objective
Verify that the pot is awarded to the winner.

Steps
- Complete a round
- Determine winner

Expected Result
Winner receives all coins in the pot.

---

### Test 10: Bot Behavior

Objective
Verify that bots act automatically.

Steps
- Start a game with bots
- Observe their actions

Expected Result
- Bots place bets automatically
- Bots lose coins when losing rounds
- Bots leave the game when coins reach 0

---

### Test 11: Hand Evaluation

Objective
Verify correct detection of poker hands.

Steps
- Manually construct test hands

Example Hands
- Pair
- Two Pair
- Three of a Kind
- Straight
- Flush
- Full House

Expected Result
Program correctly identifies each hand.

---

### Test 12: Multiplayer Host

Objective
Verify that the game server starts correctly.

Steps
- Choose "Host Game"
- Enter a port number

Expected Result
Server starts and waits for connections.

---

### Test 13: Multiplayer Join

Objective
Verify that another player can join the game.

Steps
- Start host
- Start second instance of program
- Choose "Join Game"
- Enter IP:PORT

Expected Result
Second player connects to host.

---

### Test 14: Multiplayer Gameplay

Objective
Verify multiplayer game functionality.

Steps
- Start multiplayer game
- Play several rounds

Expected Result
- All players receive cards
- Betting works
- Coins update correctly
- Host controls game state

---

### Test 15: Invalid Input Handling

Objective
Verify that incorrect input does not crash the program.

Steps
- Enter letters instead of numbers for bets
- Enter invalid menu options

Expected Result
Program handles errors and asks for input again.

---

### Test 16: Game Exit

Objective
Verify that the program exits safely.

Steps
- Choose exit option

Expected Result
- Player data is saved
- Program closes without errors

---

## Testing Summary

Successful testing means:

- All game features work correctly
- Player data is saved and loaded
- Bots behave properly
- Multiplayer connections function correctly
- No crashes occur during normal gameplay
