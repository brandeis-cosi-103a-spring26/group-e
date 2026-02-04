package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.Scanner;

import edu.brandeis.cosi103a.groupe.ConsolePlayer;
import edu.brandeis.cosi103a.groupe.AIPlayer;
import edu.brandeis.cosi103a.groupe.OurEngine;
import edu.brandeis.cosi.atg.player.Player;

public class Harness {
    
    /**
     * Prompts user for number/type of players
     * Creates Player objects
     * Creates Engine object and runs Engine.play(List<Player>);
     */
    public static void main(String[] args) {

        ArrayList<Player> players = new ArrayList<Player>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Automation: The Game");
        System.out.println("This is a 2-4 player game, with Human and AI players.");
        
        // Get number of human players with validation
        int numHuman = getValidPlayerCount(scanner, "How many Human players? (0-4)", 0, 4);
        for (int i = 0; i < numHuman; i++) {
            // System.out.println("Setting up Human player " + (i + 1));
            players.add(new ConsolePlayer());
        }
        
        // Get number of AI players with validation
        int maxAI = 4 - numHuman;
        int numAI = getValidPlayerCount(scanner, "How many AI players? (0-" + maxAI + ")", 0, maxAI);
        for (int i = 0; i < numAI; i++) {
            // System.out.println("Setting up AI player " + (i + 1));
            players.add(new AIPlayer());
        }
        
        // Ensure at least 2 players total
        while (numHuman + numAI < 2) {
            System.out.println("You need at least 2 players total. Please add more players.");
            int remaining = 4 - numHuman - numAI;
            int additionalPlayers = getValidPlayerCount(scanner, 
                "How many additional players? (1-" + remaining + ")", 1, remaining);
            
            int additionalHuman = getValidPlayerCount(scanner, 
                "How many of these should be Human? (0-" + additionalPlayers + ")", 
                0, additionalPlayers);
            for (int i = 0; i < additionalHuman; i++) {
                players.add(new ConsolePlayer());
            }
            numHuman += additionalHuman;
            
            int additionalAI = additionalPlayers - additionalHuman;
            for (int i = 0; i < additionalAI; i++) {
                players.add(new AIPlayer());
            }
            numAI += additionalAI;
        }

        OurEngine engine = new OurEngine(players);
        engine.play();

    }
    
    /**
     * Prompts the user for a valid integer within the specified range.
     * Reprompts on invalid input (non-integers or out of range).
     * 
     * @param scanner The Scanner to read input from
     * @param prompt The message to display to the user
     * @param min Minimum valid value (inclusive)
     * @param max Maximum valid value (inclusive)
     * @return A valid integer within the specified range
     */
    private static int getValidPlayerCount(Scanner scanner, String prompt, int min, int max) {
        int value = -1;
        boolean valid = false;
        
        while (!valid) {
            System.out.println(prompt);
            
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value >= min && value <= max) {
                    valid = true;
                } else {
                    System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // consume the invalid input
            }
        }
        
        return value;
    }
}
