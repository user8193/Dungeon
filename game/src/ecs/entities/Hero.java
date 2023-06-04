package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.components.stats.StatsComponent;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import graphic.Animation;
import ecs.components.OnDeathFunctions.EndGame;
import ecs.components.quests.Quest;
import ecs.components.quests.QuestComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to
 * setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity implements Serializable {

    private final int explosivePebbleCoolDown = 1;
    private final int stabCoolDown = 1;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;
    private final int maxHealth = 100;
    private final int maxMana = 100;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";
    private final String pathToGetHit = "knight/getHit";
    private final String pathToDie = "knight/die";

    private Skill firstSkill;
    private Skill secondSkill;
    private Skill thirdSkill;
    private Skill fourthSkill;
    private Skill fifthSkill;
    private Skill sixthSkill;

    private XPComponent xPComponent;

    /** Entity with Components */
    public Hero() {
        super();
        setupStatsComponent();
        setupXPComponent();
        setupComponents(maxHealth, maxHealth, new ArrayList<>(), maxMana, maxMana);
    }

    /** Maybe this will let me load */
    public void setupComponents(int maxHealth, int currentHealth, ArrayList<Quest> questLog, int maxMana,
            int currentMana) {
        new PositionComponent(this);
        setupManaComponent(maxMana, currentMana);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent(maxHealth, currentHealth);
        PlayableComponent pc = new PlayableComponent(this);
        setupSkillComponent();
        pc.setSkillSlot1(firstSkill);
        pc.setSkillSlot2(secondSkill);
        pc.setSkillSlot3(thirdSkill);
        pc.setSkillSlot4(fourthSkill);
        pc.setSkillSlot5(fifthSkill);
        setupQuestComponent(questLog);
        new InventoryComponent(this, 2);
        setupDamageComponent();
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupExplosiveSkill() {
        firstSkill = new Skill(
                new ExplosivePebbleSkill(SkillTools::getCursorPositionAsPoint, this), explosivePebbleCoolDown);
    }

    private void setupStabSkill() {
        secondSkill = new Skill(
                new StabSkill(SkillTools::getCursorPositionAsPoint, this), stabCoolDown);
    }

    private void setupRageSkill() {
        thirdSkill = new DurationSkill(new Rage(this), 10);
    }

    private void setupWisdomSkill() {
        fourthSkill = new DurationSkill(new Wisdom(this), 180);
    }

    private void setupSwiftnessSkill() {
        fifthSkill = new DurationSkill(new Swiftness(this), 20);
    }

    private void setupSkills() {
        setupExplosiveSkill();
        setupStabSkill();
        setupRageSkill();
        setupWisdomSkill();
        setupSwiftnessSkill();
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    private void setupHealthComponent(int maxHealth, int currentHealth) {
        Animation getHit = AnimationBuilder.buildAnimation(pathToGetHit);
        Animation die = AnimationBuilder.buildAnimation(pathToDie);
        IOnDeathFunction gameOver = new EndGame();
        HealthComponent hc = new HealthComponent(this, maxHealth, gameOver, getHit, die);
        hc.setCurrentHealthpoints(currentHealth);
    }

    private void setupSkillComponent() {
        SkillComponent sc = new SkillComponent(this);
        setupSkills();
        sc.addSkill(firstSkill);
        sc.addSkill(secondSkill);
        sc.addSkill(thirdSkill);
        sc.addSkill(fourthSkill);
        sc.addSkill(fifthSkill);
    }

    private void setupQuestComponent(ArrayList<Quest> questLog) {
        new QuestComponent(this, questLog);
    }

    private void setupManaComponent(int maxMana, int currentMana) {
        new ManaComponent(this, maxMana, currentMana);
    }

    private void setupStatsComponent() {
        new StatsComponent(this);
    }

    private void setupXPComponent() {
        xPComponent = new XPComponent(this, new ILevelUp() {

            @Override
            public void onLevelUp(long nexLevel) {
                HealthComponent health = (HealthComponent) getComponent(HealthComponent.class).get();
                health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.1f));
                health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                ManaComponent mana = (ManaComponent) getComponent(ManaComponent.class).get();
                mana.setMaxMana((int) (maxMana * 1.05f));
                mana.setRegenerationRatePerSecond((int) (mana.getRegenerationRatePerSecond() * 1.1f));
                ((DamageComponent) getComponent(DamageComponent.class).get()).setDamage(calcDamage());
            }

        });
    }

    private int calcDamage() {
        return (int) (Math.log10(xPComponent.getCurrentLevel())) * (int) (Math.sqrt(xPComponent.getCurrentLevel())) + 10
                + (int) xPComponent.getCurrentLevel();
    }

    public void setupDamageComponent() {
        new DamageComponent(this, calcDamage());
    }
}
