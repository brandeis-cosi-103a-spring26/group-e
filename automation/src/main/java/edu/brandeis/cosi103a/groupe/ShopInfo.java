package edu.brandeis.cosi103a.groupe;

public class ShopInfo {
    public int methodCount;
    public int moduleCount;
    public int frameworkCount;
    public int bitcoinCount;
    public int ethereumCount;
    public int dogecoinCount;

    /**
     * Constructs a ShopInfo object with the current inventory counts for each card
     * type.
     *
     * @param methodCount    the number of METHOD cards available
     * @param moduleCount    the number of MODULE cards available
     * @param frameworkCount the number of FRAMEWORK cards available
     * @param bitcoinCount   the number of BITCOIN cards available
     * @param ethereumCount  the number of ETHEREUM cards available
     * @param dogecoinCount  the number of DOGECOIN cards available
     */
    public ShopInfo(int methodCount, int moduleCount, int frameworkCount,
            int bitcoinCount, int ethereumCount, int dogecoinCount) {
        this.methodCount = methodCount;
        this.moduleCount = moduleCount;
        this.frameworkCount = frameworkCount;
        this.bitcoinCount = bitcoinCount;
        this.ethereumCount = ethereumCount;
        this.dogecoinCount = dogecoinCount;
    }

    /**
     * Returns a string representation of the shop inventory.
     *
     * @return a formatted string showing the count of each card type
     */
    @Override
    public String toString() {
        return "Methods: " + methodCount +
                ", Modules: " + moduleCount +
                ", Frameworks: " + frameworkCount +
                ", Bitcoin: " + bitcoinCount +
                ", Ethereum: " + ethereumCount +
                ", Dogecoin: " + dogecoinCount;
    }
}
