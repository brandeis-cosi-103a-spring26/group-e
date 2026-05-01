package edu.brandeis.cosi103a.groupe.main;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.ConsolePlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.fusesource.jansi.AnsiConsole;

public class Harness {

    /** Prompts user for number/type of players, creates Engine, and runs the game. */
    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        Scanner scanner = new Scanner(System.in);

        // Welcome banner
        System.out.println(ansi().eraseScreen());
        System.out.println(
                ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(
                ansi().fg(YELLOW).bold().a("    Welcome to Automation: The Game").reset());
        System.out.println(
                ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());
        System.out.println(
                ansi().fg(WHITE)
                        .a("This is a 2-4 player game, with Human and AI players.\n")
                        .reset());

        List<Player> players = new GameCreator(scanner).createPlayers();

        System.out.println(
                ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(ansi().fg(YELLOW).bold().a("    Starting Game...").reset());
        System.out.println(
                ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());

        // Instructions
        System.out.println(
                ansi().fg(CYAN).a("── How to Play ─────────────────────────────────").reset());
        System.out.println(
                ansi().fg(RED)
                        .a("  *")
                        .fg(WHITE)
                        .a(" indicates a card that can be acted upon")
                        .reset());
        System.out.println(ansi().fg(WHITE).a("    (played, bought, gained, or trashed)").reset());
        System.out.println();
        System.out.println(
                ansi().fg(YELLOW)
                        .a("  ❯")
                        .fg(WHITE)
                        .a(" To select a card, type its name or any unique")
                        .reset());
        System.out.println(
                ansi().fg(WHITE)
                        .a("    substring  (e.g. type ")
                        .fg(CYAN)
                        .a("bit")
                        .fg(WHITE)
                        .a(" for Bitcoin)")
                        .reset());
        System.out.println(
                ansi().fg(CYAN).a("────────────────────────────────────────────────\n").reset());

        System.out.print(ansi().fg(WHITE).a("Press enter to start the game...").reset());
        scanner.nextLine();
        ImmutableSet<Type> actionCards =
                ImmutableSet.of(
                        Type.CODE_REVIEW,
                        Type.REFACTOR,
                        Type.EVERGREEN_TEST,
                        Type.IPO,
                        Type.MONITORING,
                        Type.HACK,
                        Type.PARALLELIZATION,
                        Type.TECH_DEBT,
                        Type.BACKLOG,
                        Type.DAILY_SCRUM,
                        Type.SPRINT_PLANNING,
                        Type.RANSOMWARE,
                        Type.MERGE_CONFLICT,
                        Type.UNIT_TEST,
                        Type.DEPLOYMENT_PIPELINE);
        List<Type> randomActions = new ArrayList<>(actionCards);
        Collections.shuffle(randomActions);
        randomActions = randomActions.subList(0, 10);

        OurEngine engine = new OurEngine(players, randomActions);

        try {
            engine.play();
        } catch (Exception e) {
            System.out.println(
                    ansi().fg(RED)
                            .bold()
                            .a("\n✗ An error occurred during the game: ")
                            .a(e.getMessage())
                            .reset());
            e.printStackTrace();
            System.out.println();
        } finally {
            AnsiConsole.systemUninstall();
            scanner.close();
        }
    }
}
