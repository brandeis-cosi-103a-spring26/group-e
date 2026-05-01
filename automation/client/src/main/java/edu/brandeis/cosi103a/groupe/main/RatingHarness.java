package edu.brandeis.cosi103a.groupe.main;

import edu.brandeis.cosi.atg.player.Player;
import java.util.List;
import java.util.Scanner;

public class RatingHarness {

    public static void main(String[] args) {
        System.out.println("This is the player rating harness, it's still a work in progress!");
        Scanner scanner = new Scanner(System.in);
        List<Player> players = RatingInput.getPlayersFromUserInput(scanner);
        // in loop: get 10 random action cards, create engine with players and cards, run game,
        // update ratings based on result
        // after loop: print ratings
    }
}
