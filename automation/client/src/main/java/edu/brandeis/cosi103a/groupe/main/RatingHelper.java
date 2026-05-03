package edu.brandeis.cosi103a.groupe.main;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerStats;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import edu.brandeis.cosi103a.groupe.player.AutoPlayerPlus;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupe.player.ClientPlayer;
import edu.brandeis.cosi103a.groupe.player.HandEVPlayer;
import edu.brandeis.cosi103a.groupe.player.PlayerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Helper methods for rating harness input and score aggregation. */
public class RatingHelper {

    /**
     * Prompts the user to select automated player types, enforcing uniqueness and a minimum of two
     * selections.
     *
     * @param scanner the input source for user responses
     * @return the list of instantiated players in the chosen order
     */
    public static List<Player> getPlayersFromUserInput(Scanner scanner) {
        List<Player> players = new ArrayList<>();
        List<PlayerType> types = new ArrayList<>(List.of(PlayerType.values()));
        while (types.size() > 0) {
            System.out.println();
            System.out.print(
                    "Select a player type by entering its name or any unique substring (duplicates are not permitted)");
            if (players.size() >= 2) {
                System.out.print(",\nor enter 'done' to finish adding players");
            }
            System.out.println(".");
            if (players.size() == 0) {
                System.out.print("Options: ");
            } else {
                System.out.print("Remaining options: ");
            }
            for (PlayerType type : types) {
                if (types.indexOf(type) > 0) {
                    System.out.print(", ");
                }
                System.out.print(type.name());
            }
            System.out.println();
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DONE")) {
                if (players.size() >= 2) {
                    break;
                } else {
                    System.out.println("Please select at least 2 players.");
                    continue;
                }
            }
            PlayerType selectedType = null;
            boolean ambiguous = false;

            for (PlayerType type : types) {
                if (type.name().equalsIgnoreCase(input)) {
                    selectedType = type;
                    break;
                }
            }

            if (selectedType == null) {
                for (PlayerType type : types) {
                    if (type.name().contains(input)) {
                        if (selectedType != null) {
                            System.out.println(
                                    "Input is ambiguous, matches multiple options. Please be more specific.");
                            selectedType = null;
                            ambiguous = true;
                            break;
                        }
                        selectedType = type;
                    }
                }
            }
            if (selectedType == null) {
                if (!ambiguous) {
                    System.out.println("Invalid input, please try again.");
                }
            } else {
                System.out.println("Selected " + selectedType.name());
                types.remove(selectedType);
                switch (selectedType) {
                    case AUTO -> players.add(new AutoPlayer());
                    case AUTO_PLUS -> players.add(new AutoPlayerPlus());
                    case BIG_MONEY -> players.add(new BigMoneyPlayer());
                    case CLIENT -> players.add(new ClientPlayer());
                    case HAND_EV -> players.add(new HandEVPlayer());
                }
            }
        }
        System.out.println("Selected " + players.size() + " players.");
        return players;
    }

    /**
     * Updates player stats based on the results of a completed game.
     *
     * @param result the completed game result
     * @param stats the map of player names to stats to update
     */
    public static void updateStatsFromResult(GameResult result, Map<String, PlayerStats> stats) {
        List<PlayerResult> results = result.playerResults();

        Map<Integer, Integer> scoreToPlacement = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            int score = results.get(i).score();
            scoreToPlacement.putIfAbsent(score, i + 1);
        }

        int topScore = results.get(0).score();
        int winningMargin = topScore - results.get(1).score();

        for (PlayerResult pResult : results) {
            String pName = pResult.playerName();
            PlayerStats pStats = stats.get(pName);
            if (pStats == null) {
                continue;
            }

            int placement = scoreToPlacement.get(pResult.score());
            pStats.trackScore(pResult.score());
            if (placement == 1) {
                pStats.incrementWins();
                pStats.trackWinMargin(winningMargin);
            } else if (placement == 2) {
                pStats.incrementSecond();
            } else if (placement == 3) {
                pStats.incrementThird();
            } else {
                pStats.incrementFourth();
            }
        }
    }
}
