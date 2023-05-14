package ecs.components.skill;

import ecs.components.skill.ITargetSelection;
import ecs.damage.*;
import tools.Point;

public class PiercingArrowSkill extends PiercingProjectileSkill {
    
    public PiercingArrowSkill(ITargetSelection targetSelection) {
        super(
                "skills/arrow/arrow_down",
                0.5f,
                new Damage(15, DamageType.PHYSICAL, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
