package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.items.ItemData;
import graphic.hud.InputMenu;
import starter.Game;

import java.util.ArrayList;
import java.util.List;

public class Coin extends Entity{
    private final String path = "animation/coin.png";

    public Coin(){
        super();
        setupAnimationComponent();
        setupPositionComponent();
        setupHitBoxComponent();
    }

    private void setupAnimationComponent(){
        new AnimationComponent(this, AnimationBuilder.buildAnimation(path));
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }

    private void setupHitBoxComponent(){
        new HitboxComponent(
            this,
            (you, other, direction) -> collect(other),
            (you, other, direction) -> {});
    }

    private void collect(Entity entity){
        if(Game.getHero().isEmpty()){
            return;
        }
        else{
            if(entity.equals(Game.getHero().get())){
                setIntoWallet(entity);
                Game.removeEntity(this);
            }
        }
    }

    private void setIntoWallet(Entity entity) {
        if(entity.getComponent(WalletComponent.class).isPresent()){
            WalletComponent wc = (WalletComponent) entity.getComponent(WalletComponent.class).get();
            wc.addCoin(1);
        }
    }
}
