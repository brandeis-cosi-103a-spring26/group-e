package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.DeploymentPipelineEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class DeploymentPipelineEffectTest extends ActionTestSupport {

    @Test
    public void testDeploymentPipeline_AddsBuyAndMoney() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.DEPLOYMENT_PIPELINE),
                        List.of(new DeploymentPipelineEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card pipeline = new Card(Card.Type.DEPLOYMENT_PIPELINE, counter.getAndIncrement());
        hand.addToUnplayed(pipeline);

        int buysBefore = s.active.numBuys();
        int moneyBefore = s.active.numMoney();

        s.runner.processPlayCardDecision(new PlayCardDecision(pipeline));

        assertEquals(buysBefore + 1, s.active.numBuys());
        assertEquals(moneyBefore + 1, s.active.numMoney());
    }

    @Test
    public void testDeploymentPipeline_EnablesShopDiscount() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.DEPLOYMENT_PIPELINE),
                        List.of(new DeploymentPipelineEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card pipeline = new Card(Card.Type.DEPLOYMENT_PIPELINE, counter.getAndIncrement());
        hand.addToUnplayed(pipeline);

        MutableCardStacks stacks = getField(s.shop, "stacks");
        assertFalse(stacks.discounted());

        s.runner.processPlayCardDecision(new PlayCardDecision(pipeline));

        assertTrue(stacks.discounted());
    }

    @Test
    public void testDeploymentPipeline_MakesOneHigherCostCardAffordable() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.DEPLOYMENT_PIPELINE),
                        List.of(new DeploymentPipelineEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card pipeline = new Card(Card.Type.DEPLOYMENT_PIPELINE, counter.getAndIncrement());
        hand.addToUnplayed(pipeline);

        int preDiscountThreshold = Card.Type.FRAMEWORK.cost() - 1;
        assertFalse(s.shop.availableForCost(preDiscountThreshold).contains(Card.Type.FRAMEWORK));

        s.runner.processPlayCardDecision(new PlayCardDecision(pipeline));

        assertTrue(s.shop.availableForCost(preDiscountThreshold).contains(Card.Type.FRAMEWORK));
    }

    @Test
    public void testDeploymentPipeline_StillAllowsZeroCostCardsAtCostZero() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.DEPLOYMENT_PIPELINE),
                        List.of(new DeploymentPipelineEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card pipeline = new Card(Card.Type.DEPLOYMENT_PIPELINE, counter.getAndIncrement());
        hand.addToUnplayed(pipeline);

        s.runner.processPlayCardDecision(new PlayCardDecision(pipeline));

        assertTrue(s.shop.availableForCost(0).contains(Card.Type.BUG));
    }
}
