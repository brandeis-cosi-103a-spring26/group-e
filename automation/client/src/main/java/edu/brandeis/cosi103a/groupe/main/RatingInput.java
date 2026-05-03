package edu.brandeis.cosi103a.groupe.main;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import edu.brandeis.cosi103a.groupe.player.AutoPlayerPlus;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupe.player.ClientPlayer;
import edu.brandeis.cosi103a.groupe.player.HandEVPlayer;
import edu.brandeis.cosi103a.groupe.player.PlayerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Helper for building a list of automated players based on user input. */
public class RatingInput {

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
}
