package edu.brandeis.cosi103a;

public class EnginePlayCardTest {
    
    /*
    Methods to test:
    processPlayCardDecision(PlayCardDecision decision)
    - action, money, error on other

    processPlayActionCard(Card.Type)
    - error on <=0 actions | Not true!! Bc throne room
    - exec for each type
    - error on other
    - subtrack one action always

    processPlayMoneyCard(Card.Type)
    - Bitcoin, etherium, dogecoin, error on other
    - make sure numMoney goes up right amount

    executeActionCodeReview
    - Gives +1 card +2 actions
    - if no cards to draw doesn't draw
    
    executeActionRefactor
    - If any cards in hand generates decisions for all cards (no endphase)
    - If no cards in hand, generates only endphase
    - if no card trashed, ends early
    - after card trashed, generates gain decision for all applicable cards (no endphase)
    - if no applicable cards (rare) only endphase

    executeActionEvergreenTest
    - Draws two cards
    - Gives bug to all other players
    - if not enough bugs, gives to all it can
    */


}
