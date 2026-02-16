# Copilot Instructions

## Project Context
Software Engineering class project with 4 team members building a Dominion-like deck building card game in Java using the ATG API created by the professor. Prioritize learning over quick fixes—explain concepts and guide toward better approaches when requests seem inexperienced.

## ATG API Reference
Maven dependency: `io.github.brandeis-cosi-103a:atg-api` (see `automation/pom.xml` for the current version)

### Core Packages
- `edu.brandeis.cosi.atg.engine` - Game engine (Engine interface, PlayerViolationException)
- `edu.brandeis.cosi.atg.state` - Game state (GameState, Hand, CardStacks, GameResult, TurnPhase)
- `edu.brandeis.cosi.atg.cards` - Card definitions (Card, Card.Type)
- `edu.brandeis.cosi.atg.decisions` - Player decisions (Decision interface and implementations)
- `edu.brandeis.cosi.atg.player` - Player abstraction (Player interface)

### Key Classes
- **GameState**: Immutable snapshot of current game state (hands, stacks, turn phase, current player)
- **Engine**: Orchestrates game flow, validates moves, processes decisions
- **Card**: Represents game cards (has name, cost, types like TREASURE/ACTION/VICTORY)
- **Decision**: Player choices (BuyDecision, PlayCardDecision, GainCardDecision, TrashCardDecision, EndPhaseDecision)
- **Player**: Interface for making decisions given current GameState

### Common Patterns
- GameState is immutable - engine creates new states after each decision
- Players receive GameState and return Decision objects
- Engine validates decisions and throws PlayerViolationException on invalid moves
- TurnPhase enum: ACTION, MONEY, BUY, GAIN, CLEANUP phases in card games

## Communication
Technical and concise. Always explain *why*, not just *how*. Show trade-offs and suggest multiple approaches when relevant.

## Code & Solutions
- Write readable, maintainable code over clever solutions
- Flag edge cases, gotchas, and potential issues upfront
- Suggest best practices and improvements beyond the immediate ask
- Consider testability and future extensibility

## Testing & Documentation
- Recommend test cases (including edge cases) for new functionality
- Add inline comments for complex logic
- Explain non-obvious design decisions