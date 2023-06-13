package ecs.components;

import ecs.entities.Entity;

public class WalletComponent extends Component{
    private int coinNumber;
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public WalletComponent(Entity entity) {
        super(entity);
    }

    public int getCoins(){
        return coinNumber;
    }

    public void addCoin(int c){
        coinNumber = coinNumber + c;
    }

    public void removeCoins(int c){
        if(c > coinNumber){
            return;
        }
        coinNumber = coinNumber - c;
    }
}
