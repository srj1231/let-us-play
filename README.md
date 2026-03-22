# let-us-play

**Multiplayer board game platform with pluggable rules, bot strategies, and production-ready infrastructure.**

---

## What this is

`let-us-play` is a game engine built around one idea: **the game rules and the game infrastructure should never know about each other.**

Want to add Connect4? Implement `GameFlow` and `BoardFeatures`. Done. The matchmaking, real-time push, persistence, and bot framework work out of the box. Want a smarter bot? Implement `MoveStrategy`. No other class changes.

Currently ships with **Tic Tac Toe**. Designed for everything else.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Game Management                      │
│                                                         │
│  GameFlow (interface)                                   │
│      └── Game (orchestrator)                            │
│            ├── BoardFeatures      (win/draw detection)  │
│            ├── GameBoardFeatures  (setup & display)     │
│            └── Player             (abstract)            │
│                  ├── HumanPlayer                        │
│                  └── BotPlayer                          │
│                        └── MoveStrategy (interface)     │
│                              ├── RandomStrategy         │
│                              ├── MinimaxStrategy        │
│                              └── HeuristicStrategy      │
└─────────────────────────────────────────────────────────┘
          │ GameResultEvent (Kafka)
          ▼
┌─────────────────────────────────────────────────────────┐
│                   User Management                       │
│                                                         │
│  User → PersonalDetails, Statistics                     │
│  StatsService (consumes GameResultEvent)                │
└─────────────────────────────────────────────────────────┘
```

Two bounded contexts. They share nothing except a Kafka event. `Game Management` never imports a `User` class. `User Management` never imports a `Game` class.

---

## Design principles

Every design decision in this codebase maps to a principle. If you're reading a class and wondering *why it's structured this way*, the answer is here.

**`Game` holds interfaces, not concrete classes.**
`boardFeatures: BoardFeatures`, `gameBoardFeat: GameBoardFeatures`, `moveStrategy: MoveStrategy` — all injected. `Game` is the conductor. It calls others at the right time but owns none of their logic.

**`User ≠ Player`.**
`User` is a persistent account. `Player` is a transient game-session role. One user can play a hundred games as a hundred different `Player` instances. `HumanPlayer` stores `userId: String` as a foreign key — never a `User` object reference. Bounded context boundary enforced at the field level.

**`GameState` and `Game` are different things.**
`Game` is behaviour — in memory, stateful, not serializable. `GameState` is data — immutable, serializable, the thing that goes to the DB, over Kafka, and to reconnecting clients. This separation is what makes persistence, reconnection, and cross-server distribution possible without hacks.

**Every move is an event.**
`Move` carries a timestamp. `GameState` carries a full `moveHistory`. You can reconstruct any game at any point in time by replaying its moves. This isn't over-engineering — it's what lets you do audit logs, dispute resolution, and anti-cheat detection without a second thought.

---

## L6-ready infrastructure

The domain model is clean OOP. The infrastructure layer is built for production:

| Concern | Solution |
|---|---|
| Concurrent moves | `GameActor` — single-threaded queue per game, no locks |
| Persistence | `GameRepository` interface — pluggable, event-sourced |
| Real-time push | WebSocket per player, `GameEventPublisher` broadcasts |
| Cross-server | Kafka — all moves for a game routed to the same partition |
| Reconnection | Player rejoins → receives latest `GameState` snapshot |
| Disconnection | Per-game timeout → auto-forfeit or pause |
| Stats update | `GameResultEvent` on Kafka → `StatsService` in User Management |
| Deduplication | `eventId` UUID on every event — idempotent consumer |
| Observability | Structured logging, metrics on move latency, game throughput |

---

## Project structure

```
src/main/java/com/game/
├── common/                     # Shared value objects & enums
│   ├── Symbol.java
│   ├── Move.java
│   ├── GameEvent.java
│   ├── GameState.java
│   ├── GameResultEvent.java
│   └── GameExceptions.java
├── domain/
│   ├── board/                  # Cell, Board
│   ├── player/                 # Player, HumanPlayer, BotPlayer
│   ├── strategy/               # MoveStrategy, Random, Minimax, Heuristic
│   └── game/                   # Game, GameFlow, BoardFeatures, GameBoardFeatures
├── infrastructure/
│   ├── persistence/            # GameRepository, InMemoryGameRepository
│   ├── messaging/              # Kafka publisher & consumer
│   └── websocket/              # GameEventPublisher, WebSocket handlers
└── application/
    ├── matchmaking/            # MatchmakingService
    └── stats/                  # StatsService (User Management boundary)

src/main/java/com/usermanagement/
├── domain/                     # User, PersonalDetails, Statistics, GenderEnum
└── service/                    # StatsService, UserService
```

---

## Getting started

```bash
git clone https://github.com/yourusername/let-us-play.git
cd let-us-play
./mvnw spring-boot:run
```

Requires Java 17+, Spring Boot 3.x, Kafka (for cross-boundary events).

---

## Adding a new game

1. Implement `GameFlow` — define `startGame()` and `playTurn()`
2. Implement `BoardFeatures` — define your win and draw conditions
3. Implement `GameBoardFeatures` — define board setup for your game
4. Wire them up in a Spring `@Configuration`

That's it. Matchmaking, bots, persistence, real-time push — all inherited.

---

## Adding a new bot strategy

1. Implement `MoveStrategy` — one method: `decideMove(board): Move`
2. Inject it into `BotPlayer` at construction

Zero changes to any existing class.

---

## What's next

- [ ] Chess implementation
- [ ] Connect4 implementation  
- [ ] Tournament mode via `GameRunner`
- [ ] ELO rating system in `Statistics`
- [ ] Spectator mode via WebSocket broadcast
- [ ] Replay viewer from `moveHistory`

---

## License

MIT
