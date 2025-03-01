package ecs.components.skill;

import ecs.components.ManaComponent;
import ecs.components.MissingComponentException;
import ecs.components.stats.StatsComponent;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import tools.Constants;

import java.util.logging.Logger;

/**
 * Duration skill that increases the physical damage
 */
public class Rage implements IDurationSkillFunction {

    private float durationInFrames, currentDurationInFrames = 0.0f, damageMultiplier = 1.0f, originalDamageMultiplier;
    private int manaCost;
    private StatsComponent stats;
    private transient final Logger rangeLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new Rage skill
     * <p/>
     * The Rage skill increases the Physical damage the wielder deals
     *
     * @param durationIneconds time how long the skill lasts in seconds
     * @param damageMultiplier multiplier to increase the physical damage
     * @param entity           the entity that owns this Skill
     * @param manaCost         the amount of mana needed to activate this Skill
     *
     */
    public Rage(float durationIneconds, float damageMultiplier, Entity entity, int manaCost) {
        if (!entity.getComponent(StatsComponent.class).isPresent())
            throw new MissingComponentException("StatsComponent");
        durationInFrames = durationIneconds * Constants.FRAME_RATE;
        this.damageMultiplier = damageMultiplier > 1.0f ? damageMultiplier : 1.0f;
        stats = (StatsComponent) entity.getComponent(StatsComponent.class).get();
        originalDamageMultiplier = stats.getDamageModifiers().getMultiplier(DamageType.PHYSICAL);
        this.manaCost = manaCost;
    }

    /**
     * Creates a new Rage skill
     * <p/>
     * The Rage skill increases the Physical damage the wielder deals
     * <p/>
     * The standard duration is {@code 10} seconds
     * <p/>
     * The standard multiplier is {@code 2.0f}
     * <p/>
     * The standard manaCost is {@code 10}
     *
     * @param entity the entity that owns this Skill
     *
     */
    public Rage(Entity entity) {
        this(10, 2.0f, entity, 10);
    }

    @Override
    public void execute(Entity entity) {
        if (!entity.getComponent(ManaComponent.class).isPresent())
            throw new MissingComponentException("ManaComponent");
        if (entity.getComponent(ManaComponent.class).map(ManaComponent.class::cast).get().spendMana(manaCost)) {
            stats.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, originalDamageMultiplier * damageMultiplier);
            activateDuration();
            rangeLogger.info("RangeSkill was executed");
        }
    }

    /**
     * Decreases the {@code currentDurationInFrames} and resets multiplier if the
     * time is over
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void reduceDuration() {
        if (--currentDurationInFrames <= 0)
            stats.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, originalDamageMultiplier);
    }

    @Override
    public boolean isActive() {
        return currentDurationInFrames > 0;
    }

    @Override
    public void activateDuration() {
        currentDurationInFrames = durationInFrames;
    }

}
