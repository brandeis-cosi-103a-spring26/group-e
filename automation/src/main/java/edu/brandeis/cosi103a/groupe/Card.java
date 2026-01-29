package edu.brandeis.cosi103a.groupe;

public class Card {
    public enum Type {
        AUTOMATION, CRYPTOCURRENCY
    }

    public enum Name {
        METHOD, MODULE, FRAMEWORK, BITCOIN, ETHEREUM, DOGECOIN
    }

    int value;
    int cost;
    Type type;
    Name name;

    /**
     * Constructs a Card with the specified cost, value, type, and name.
     *
     * @param cost  the cost in coins to purchase this card
     * @param value the value this card provides (coins for cryptocurrency, points
     *              for automation)
     * @param type  the type of card (AUTOMATION or CRYPTOCURRENCY)
     * @param name  the specific name of the card
     */
    public Card(int cost, int value, Type type, Name name) {
        this.value = value;
        this.cost = cost;
        this.type = type;
        this.name = name;
    }

    /**
     * Constructs a Card with predefined attributes based on the card name.
     * Each card type has preset values:
     * - METHOD: cost 2, value 1, AUTOMATION
     * - MODULE: cost 5, value 3, AUTOMATION
     * - FRAMEWORK: cost 8, value 6, AUTOMATION
     * - BITCOIN: cost 0, value 1, CRYPTOCURRENCY
     * - ETHEREUM: cost 3, value 2, CRYPTOCURRENCY
     * - DOGECOIN: cost 6, value 3, CRYPTOCURRENCY
     *
     * @param name the name of the card to create
     */
    public Card(Name name) {
        this.name = name;
        switch (name) {
            case METHOD -> {
                this.cost = 2;
                this.value = 1;
                this.type = Type.AUTOMATION;
            }
            case MODULE -> {
                this.cost = 5;
                this.value = 3;
                this.type = Type.AUTOMATION;
            }
            case FRAMEWORK -> {
                this.cost = 8;
                this.value = 6;
                this.type = Type.AUTOMATION;
            }
            case BITCOIN -> {
                this.cost = 0;
                this.value = 1;
                this.type = Type.CRYPTOCURRENCY;
            }
            case ETHEREUM -> {
                this.cost = 3;
                this.value = 2;
                this.type = Type.CRYPTOCURRENCY;
            }
            case DOGECOIN -> {
                this.cost = 6;
                this.value = 3;
                this.type = Type.CRYPTOCURRENCY;
            }
        }
    }

    /**
     * Gets the value of this card.
     * For cryptocurrency cards, this is the number of coins it generates.
     * For automation cards, this is the number of points it's worth.
     *
     * @return the value of the card
     */
    int getValue() {
        return value;
    }

    /**
     * Gets the cost in coins to purchase this card.
     *
     * @return the cost of the card
     */
    int getCost() {
        return cost;
    }

    /**
     * Gets the type of this card.
     *
     * @return the card type (AUTOMATION or CRYPTOCURRENCY)
     */
    Type getType() {
        return type;
    }

    /**
     * Gets the name of this card.
     *
     * @return the card name (METHOD, MODULE, FRAMEWORK, BITCOIN, ETHEREUM, or
     *         DOGECOIN)
     */
    Name getName() {
        return name;
    }

    /**
     * Gets the colloquial name of the card as a string.
     *
     * @return the colloquial name of the card
     */
    String getColloquialName() {
        switch (name) {
            case METHOD -> {
                return "Method";
            }
            case MODULE -> {
                return "Module";
            }
            case FRAMEWORK -> {
                return "Framework";
            }
            case BITCOIN -> {
                return "Bitcoin";
            }
            case ETHEREUM -> {
                return "Ethereum";
            }
            case DOGECOIN -> {
                return "Dogecoin";
            }
        }
        return "Unknown";
    }
}