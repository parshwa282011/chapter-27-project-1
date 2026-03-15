## Multiplayer System

The game supports multiplayer functionality in addition to singleplayer mode with bots. Players can either host a game or join an existing game using a server address.

### Multiplayer Options

When the game starts, the player can choose one of the following options:

- Singleplayer (play against bots)
- Host Multiplayer Game
- Join Multiplayer Game

### Hosting a Game

If the player chooses to host a game:

- The program creates a server using a chosen port.
- Other players can connect using the host’s IP address and port.
- The host selects the starting coin amount for all players.
- This starting coin amount is stored and managed on the host side.

Responsibilities of the host:
- Manage the game state
- Store player coin data
- Handle betting and round logic
- Evaluate hands and determine winners

The host acts as the central authority for the game.

### Joining a Game

If a player chooses to join a game:

- The player enters the server address in the format:

IP:PORT

Example:
192.168.1.45:8080

The program then connects to the host server and joins the game.

### Multiplayer Data Management

- Player coin balances are stored and updated on the host computer.
- Clients do not directly modify coin values.
- The host sends updates to all connected players after each round.

### Multiplayer Classes

Additional classes may be used to support networking.

Server
- Runs on the host computer
- Accepts incoming player connections
- Manages game state

Client
- Connects to a host server
- Sends player actions
- Receives game updates

ConnectionHandler
- Handles communication between players and the server

### Multiplayer Program Flow

1. Player starts program
2. Player selects game mode
3. If hosting:
   - server starts
   - host chooses starting coins
4. If joining:
   - player enters server IP and port
   - client connects to host
5. Players join lobby
6. Game starts when host begins round
7. Cards are dealt and betting begins
8. Host evaluates hands
9. Results are sent to all players
10. Coins are updated on host side
11. Next round begins
