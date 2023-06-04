package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import tools.Point;

public class StabSkill extends DamageMeleeSkill {

    public StabSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/Stab",
                0.5f,
                new Damage(20, DamageType.PHYSICAL, entity),
                new Point(10, 10),
                targetSelection);
    }

}
