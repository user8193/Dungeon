package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.items.ItemData;
import graphic.Animation;
import graphic.hud.InputMenu;
import starter.Game;
import tools.Point;

import java.util.ArrayList;
import java.util.List;

public class Shop extends Entity{
    private List<ItemData> inventory = new ArrayList<>(4);
    private final String path = "animation/shopUI.png";

    public Shop(){
        super();
        setupAnimationComponent();
        setupPositionComponent();
    }

    private void setupAnimationComponent(){
        new AnimationComponent(this, AnimationBuilder.buildAnimation(path));
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }
}
