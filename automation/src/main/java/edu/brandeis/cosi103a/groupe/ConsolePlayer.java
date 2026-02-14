package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;

import com.google.common.collect.ImmutableList;

public class ConsolePlayer extends AbstractPlayer {
    /**
     * Makes a decision based on user input from the console.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        // Render the current game state to the console
        Renderer.renderGame(state);

        // If the only option is EndPhaseDecision, prompt player to press enter to continue
        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            System.out.print("No choices available. Press enter to continue...");
            scanner.nextLine(); // Wait for enter key
            return options.get(0);
        }

        // Print available options
        for (int i = 0; i < options.size(); i++) {
            System.out.println("Option " + (i + 1) + ": " + options.get(i).getDescription());
        }

        // Prompt user for input until a valid choice is made
        System.out.print("Enter the number of your chosen option: ");
        int choice = -1;
        while (choice < 1 || choice > options.size()) {
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the leftover newline character
                if (choice < 1 || choice > options.size()) {
                    System.out.print("Invalid choice. Please enter a number between 1 and " + options.size() + ": ");
                }
            } else {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next(); // Consume the invalid input
            }
        }

        return options.get(choice - 1);
    }
}
