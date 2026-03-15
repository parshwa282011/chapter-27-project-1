# Poker-Style Card Game (Java Console)

This is a Java console poker-style card game with:
- Singleplayer vs bots
- Multiplayer host/join over a network (IP:PORT)
- Persistent player coins stored in `data.txt`

## Requirements
- Java 17+ (JDK, not just JRE)

## Where The Files Are

Source code:
- `src/main/java/poker/Main.java`: program entry point (mode select: singleplayer/host/join)
- `src/main/java/poker/game/`: game flow + betting + hand evaluation
- `src/main/java/poker/model/`: card/deck/player/bot model classes
- `src/main/java/poker/net/`: multiplayer server/client networking
- `src/main/java/poker/io/`: file persistence (`data.txt`)
- `src/main/java/poker/util/`: safe console input helpers

Documents:
- `design_document.md`
- `requirements_document.md`
- `project_plan.md`

Runtime data:
- `data.txt` (created/updated when you play singleplayer)

## How To Compile And Run (No Maven)

Compile:
```sh
rm -rf out && mkdir -p out
javac -d out $(find src/main/java -name "*.java")
```

Run:
```sh
java -cp out poker.Main
```

Build a runnable JAR (optional):
```sh
jar --create --file poker-game.jar --main-class poker.Main -C out .
java -jar poker-game.jar
```

## How To Compile And Run (With Maven)

If Maven is installed, this project includes `pom.xml`.

Build:
```sh
mvn package
```

Run:
```sh
java -jar target/poker-game-1.0-SNAPSHOT.jar
```

## Game Rules Implemented

Each round:
- Each player gets 3 private cards.
- There are 5 community cards revealed as:
  - FLOP: 3 cards
  - TURN: 1 card
  - RIVER: 1 card
- There is a betting round after each reveal (including preflop).
- Players can fold during betting.
- Winner is determined by the best 5-card hand from (private 3 + community 5).

At the end of a round, the game prints the winning hand rank and the exact 5 cards that made it.

## Multiplayer Notes (Host/Join)

In `poker.Main` choose:
- Host: choose a port and wait for players
- Join: enter `IP:PORT`

Networking tips:
- Same Wi-Fi/LAN: share your local IP (often `192.168.x.x` or `10.x.x.x`) and the port.
- Different networks: you usually need to port-forward that port on the host router and share the host public IP and the port.

## Excluding Build Files From Git Push

This repo includes `.gitignore` to exclude build outputs like `out/`, `target/`, `*.class`, and `*.jar`.
# Poker-Style Card Game (Java Console)

This is a Java console poker-style card game with:
- Singleplayer vs bots
- Multiplayer host/join over a network (IP:PORT)
- Persistent player coins stored in `data.txt`

## Requirements
- Java 17+ (JDK, not just JRE)

## Where The Files Are

Source code:
- `src/main/java/poker/Main.java`: program entry point (mode select: singleplayer/host/join)
- `src/main/java/poker/game/`: game flow + betting + hand evaluation
- `src/main/java/poker/model/`: card/deck/player/bot model classes
- `src/main/java/poker/net/`: multiplayer server/client networking
- `src/main/java/poker/io/`: file persistence (`data.txt`)
- `src/main/java/poker/util/`: safe console input helpers

Documents:
- `design_document.md`
- `requirements_document.md`
- `project_plan.md`

Runtime data:
- `data.txt` (created/updated when you play singleplayer)

## How To Compile And Run (No Maven)

Compile:
```sh
rm -rf out && mkdir -p out
javac -d out $(find src/main/java -name "*.java")
```

Run:
```sh
java -cp out poker.Main
```

Build a runnable JAR (optional):
```sh
jar --create --file poker-game.jar --main-class poker.Main -C out .
java -jar poker-game.jar
```

## How To Compile And Run (With Maven)

If Maven is installed, this project includes `pom.xml`.

Build:
```sh
mvn package
```

Run:
```sh
java -jar target/poker-game-1.0-SNAPSHOT.jar
```

## Game Rules Implemented

Each round:
- Each player gets 3 private cards.
- There are 5 community cards revealed as:
  - FLOP: 3 cards
  - TURN: 1 card
  - RIVER: 1 card
- There is a betting round after each reveal (including preflop).
- Players can fold during betting.
- Winner is determined by the best 5-card hand from (private 3 + community 5).

At the end of a round, the game prints the winning hand rank and the exact 5 cards that made it.

## Multiplayer Notes (Host/Join)

In `poker.Main` choose:
- Host: choose a port and wait for players
- Join: enter `IP:PORT`

Networking tips:
- Same Wi-Fi/LAN: share your local IP (often `192.168.x.x` or `10.x.x.x`) and the port.
- Different networks: you usually need to port-forward that port on the host router and share the host public IP and the port.

## Excluding Build Files From Git Push

This repo includes `.gitignore` to exclude build outputs like `out/`, `target/`, `*.class`, and `*.jar`.
