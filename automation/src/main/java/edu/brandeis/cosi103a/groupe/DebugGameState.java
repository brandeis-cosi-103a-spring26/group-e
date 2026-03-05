package edu.brandeis.cosi103a.groupe;

import java.util.List;
import java.util.Map;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;

/**
 * Debug information container for rendering extended game state.
 * Contains the regular game state plus internal engine details like player decks and discards.
 * 
 * @param state The current public game state
 * @param decks Map of each player's deck (cards not yet drawn)
 * @param discards Map of each player's discard pile
 */
public record DebugGameState(
    GameState state,
    Map<Player, List<Card>> decks,
    Map<Player, List<Card>> discards
) {}
