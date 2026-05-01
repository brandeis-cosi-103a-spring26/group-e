package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RansomwareEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class RansomwareEffectTest extends ActionTestSupport {

    private static class RansomwareChoicePlayer implements Player {
        private final String name;
        private final ChooseEffectDecision.Effect preferred;
        private final boolean declineReaction;

        int totalPrompts = 0;
        int reactionPrompts = 0;
        int choicePrompts = 0;
        int discardPrompts = 0;
        int discardPhasePrompts = 0;

        RansomwareChoicePlayer(String name, ChooseEffectDecision.Effect preferred) {
            this(name, preferred, false);
        }

        RansomwareChoicePlayer(
                String name, ChooseEffectDecision.Effect preferred, boolean declineReaction) {
            this.name = name;
            this.preferred = preferred;
            this.declineReaction = declineReaction;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            totalPrompts++;
            if (state.phase() == GameState.TurnPhase.REACTION) {
                reactionPrompts++;
                if (declineReaction) {
                    for (Decision option : options) {
                        if (option instanceof EndPhaseDecision) {
                            return option;
                        }
                    }
                }
            }

            boolean sawChoosePrompt =
                    options.stream().anyMatch(option -> option instanceof ChooseEffectDecision);
            if (sawChoosePrompt) {
                choicePrompts++;
            }

            for (Decision option : options) {
                if (option instanceof ChooseEffectDecision choose) {
                    if (choose.effect() == preferred) {
                        return choose;
                    }
                }
            }

            for (Decision option : options) {
                if (option instanceof DiscardCardDecision) {
                    discardPrompts++;
                    if (state.phase() == GameState.TurnPhase.DISCARD) {
                        discardPhasePrompts++;
                    }
                    return option;
                }
            }

            return options.get(0);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<GameObserver> getObserver() {
            return Optional.empty();
        }
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    public void testRansomware_DiscardBranch_ForcesUpToTwoDiscards() throws Exception {
        Setup s =
                new Setup(
                        2, ImmutableSet.of(Card.Type.RANSOMWARE), List.of(new RansomwareEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        PlayerInfo opponent = s.players.get(1);
        RansomwareChoicePlayer chooser =
                new RansomwareChoicePlayer(
                        "DiscardChooser", ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO);
        setField(opponent, "player", chooser);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        opponentHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        opponentHand.addToUnplayed(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        opponentHand.addToUnplayed(new Card(Card.Type.DOGECOIN, counter.getAndIncrement()));

        int initialActiveHand = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(initialActiveHand + 2, s.active.getUnplayedCards().size());
        assertEquals(1, chooser.choicePrompts);
        assertEquals(2, chooser.discardPrompts);
        assertEquals(2, chooser.discardPhasePrompts);
        assertEquals(1, opponent.getUnplayedCards().size());
    }

    @Test
    public void testRansomware_GainBugBranch_GainsBug() throws Exception {
        Setup s =
                new Setup(
                        2, ImmutableSet.of(Card.Type.RANSOMWARE), List.of(new RansomwareEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        PlayerInfo opponent = s.players.get(1);
        RansomwareChoicePlayer chooser =
                new RansomwareChoicePlayer(
                        "GainChooser", ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG);
        setField(opponent, "player", chooser);

        List<Card> opponentDiscard = getField(opponent, "discard");
        int discardBefore = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(1, chooser.choicePrompts);
        assertEquals(0, chooser.discardPrompts);
        assertEquals(discardBefore + 1, opponentDiscard.size());
        assertEquals(Card.Type.BUG, opponentDiscard.get(opponentDiscard.size() - 1).type());
    }

    @Test
    public void testRansomware_MonitoringBlocksChoicePrompt() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.RANSOMWARE, Card.Type.MONITORING),
                        List.of(new RansomwareEffect(), new MonitoringEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        PlayerInfo opponent = s.players.get(1);
        RansomwareChoicePlayer chooser =
                new RansomwareChoicePlayer(
                        "MonitoringChooser", ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG);
        setField(opponent, "player", chooser);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        opponentHand.addToUnplayed(new Card(Card.Type.MONITORING, counter.getAndIncrement()));
        opponentHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(0, chooser.choicePrompts);
        assertEquals(1, chooser.totalPrompts);
        assertEquals(1, chooser.reactionPrompts);
        assertTrue(
                s.eventSink.getEvents().stream()
                        .map(n -> n.getEvent().getDescription())
                        .anyMatch(desc -> desc.contains("revealed Monitoring")));
    }

    @Test
    public void testRansomware_MonitoringCanDeclineReveal_ThenGetsChoicePrompt() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.RANSOMWARE, Card.Type.MONITORING),
                        List.of(new RansomwareEffect(), new MonitoringEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        PlayerInfo opponent = s.players.get(1);
        RansomwareChoicePlayer chooser =
                new RansomwareChoicePlayer(
                        "DeclineReactionChooser",
                        ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG,
                        true);
        setField(opponent, "player", chooser);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        opponentHand.addToUnplayed(new Card(Card.Type.MONITORING, counter.getAndIncrement()));
        List<Card> opponentDiscard = getField(opponent, "discard");
        int discardBefore = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(1, chooser.reactionPrompts);
        assertEquals(1, chooser.choicePrompts);
        assertEquals(discardBefore + 1, opponentDiscard.size());
        assertEquals(Card.Type.BUG, opponentDiscard.get(opponentDiscard.size() - 1).type());
    }

    @Test
    public void testRansomware_DiscardBranch_AllowsOneOrZeroCardsInHand() throws Exception {
        Setup s =
                new Setup(
                        3, ImmutableSet.of(Card.Type.RANSOMWARE), List.of(new RansomwareEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        PlayerInfo oneCardPlayer = s.players.get(1);
        RansomwareChoicePlayer oneCardChooser =
                new RansomwareChoicePlayer(
                        "OneCardChooser", ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO);
        setField(oneCardPlayer, "player", oneCardChooser);
        MutableHand oneCardHand = getField(oneCardPlayer, "hand");
        oneCardHand.clear();
        oneCardHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));

        PlayerInfo zeroCardPlayer = s.players.get(2);
        RansomwareChoicePlayer zeroCardChooser =
                new RansomwareChoicePlayer(
                        "ZeroCardChooser", ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO);
        setField(zeroCardPlayer, "player", zeroCardChooser);
        MutableHand zeroCardHand = getField(zeroCardPlayer, "hand");
        zeroCardHand.clear();

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(1, oneCardChooser.choicePrompts);
        assertEquals(1, oneCardChooser.discardPrompts);
        assertEquals(1, oneCardChooser.discardPhasePrompts);
        assertEquals(0, oneCardPlayer.getUnplayedCards().size());

        assertEquals(1, zeroCardChooser.choicePrompts);
        assertEquals(0, zeroCardChooser.discardPrompts);
        assertEquals(0, zeroCardPlayer.getUnplayedCards().size());
    }

    @Test
    public void testRansomware_GainBugBranch_EmptyBugSupply_NoCrashNoGain() throws Exception {
        Setup s =
                new Setup(
                        2, ImmutableSet.of(Card.Type.RANSOMWARE), List.of(new RansomwareEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ransomware = new Card(Card.Type.RANSOMWARE, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 3) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        activeHand.addToUnplayed(ransomware);

        MutableCardStacks stacks = getField(s.shop, "stacks");
        while (stacks.getNumAvailable(Card.Type.BUG) > 0) {
            stacks.decrementCount(Card.Type.BUG);
        }

        PlayerInfo opponent = s.players.get(1);
        RansomwareChoicePlayer chooser =
                new RansomwareChoicePlayer(
                        "NoBugSupplyChooser", ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG);
        setField(opponent, "player", chooser);
        List<Card> opponentDiscard = getField(opponent, "discard");
        int discardBefore = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(ransomware));

        assertEquals(1, chooser.choicePrompts);
        assertEquals(0, chooser.discardPrompts);
        assertEquals(0, stacks.getNumAvailable(Card.Type.BUG));
        assertEquals(discardBefore, opponentDiscard.size());
    }
}
