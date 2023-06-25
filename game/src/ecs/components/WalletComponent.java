package ecs.components;

import ecs.entities.Entity;

import java.util.logging.Logger;

/** Allows an Entity to carry coins */
public class WalletComponent extends Component{
    private transient final Logger walletLogger = Logger.getLogger(this.getClass().getName());
    private int coinNumber;
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public WalletComponent(Entity entity) {
        super(entity);
    }

    /**
     * Get the number of collected coin
     * @return number of collected coins
     */
    public int getCoins(){
        return coinNumber;
    }

    /**
     * Adding a new Coin
     * @param c is the Coin, that you want to collect
     */
    public void addCoin(int c){
        coinNumber = coinNumber + c;
        walletLogger.info("Coin was collected");
    }

    /**
     * Removes one coin
     * @param c is the Coin, that you want to remove
     */
    public void removeCoins(int c){
        if(c > coinNumber){
            return;
        }
        coinNumber = coinNumber - c;
        walletLogger.info("Coin was removed");
    }
}
