package ecs.components.skill;

/**
 * Interface for the things a duration skill can do
 */
public interface IDurationSkillFunction extends ISkillFunction {

    /**
     * This is to be used to reduce the duration
     */
    public void reduceDuration();

    /**
     * Returns {@code true} if the skill is active
     * 
     * @return {@code true} if the skill is active
     */
    public boolean isActive();

    /**
     * Activate the skill
     * 
     */
    public void activateDuration();

}
