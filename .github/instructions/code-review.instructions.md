---
applyTo: "**"
excludeAgent: "coding-agent"
---

# Code Review Instructions

## Focus On
- **Correctness**: Flag bugs, logic errors, and potential runtime issues
- **Design flaws**: Identify poor architectural choices or significantly better alternatives to accomplish the same goal
- **Testing**: Ensure new functionality has tests covering edge cases. Flag areas that lack unit test coverage where they should have it
- **Verify suggestions work**: Only suggest changes you're confident will compile and function correctly

## Ignore
- Purely aesthetic changes (spacing, formatting, naming) unless naming of variables/methods is misleading or non-descriptive
- Minor style preferences that don't affect maintainability
- Non-critical optimizations
