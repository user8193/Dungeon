package ecs.systems;

import graphic.hud.InputMenu;

public class DialogSystem extends ECS_System{
    @Override
    public void update() {
        InputMenu.result();
    }
}
