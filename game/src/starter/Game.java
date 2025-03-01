package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import configuration.Configuration;
import configuration.KeyboardConfig;
import controller.AbstractController;
import controller.SystemController;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.quests.QuestComponent;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.*;
import ecs.items.ItemData;
import ecs.systems.*;
import ecs.tools.Flags.Flag;
import saving.GameData;
import saving.Saves;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.GameOverMenu;
import graphic.hud.MinigameScreen;
import graphic.hud.PauseMenu;
import graphic.hud.QuestLogMenu;
import graphic.hud.QuestMenu;
import minigame.Minigame;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelElement;
import level.tools.LevelSize;
import tools.Constants;
import tools.Point;
import java.lang.Math;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader {

    private final LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw
     * need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contains all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doSetup = true;
    private static boolean paused = false;

    /** All entities that are currently active in the dungeon */
    private static final Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    private static final Set<Entity> entitiesToRemove = new HashSet<>();
    /** All entities to be added from the dungeon in the next frame */
    private static final Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu<Actor> pauseMenu;
    private static GameOverMenu<Actor> gameOverMenu;
    private static Entity hero;
    private Logger gameLogger;
    private static int level = 0;

    private static Saves saves = new Saves();

    public static QuestMenu<Actor> questMenu;
    public static QuestLogMenu<Actor> questLogMenu;
    public static int questDisplayTime = 0;
    private static boolean inQuestLog = false;
    private static boolean minigameIsActive = false;
    public static Minigame minigame;
    public static MinigameScreen<Actor> minigameScreen;

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Our way to end the game
        try {
            DesktopLauncher.run(new Game());
        } catch (Flag flag) {
            // Game end
            return;
        }

    }

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation
     * (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
        super.render(delta);
        if (minigameIsActive && minigame != null) {
            minigameRender(delta);
        } else {
            gameRender(delta);
        }
    }

    private void gameRender(float delta) {
        if (doSetup)
            prepareSetup();
        batch.setProjectionMatrix(camera.combined);
        frame();
        clearScreen();
        levelAPI.update();
        controller.forEach(AbstractController::update);
        camera.update();
        if (questDisplayTime > 0)
            questDisplayTime--;
        else if (questMenu != null)
            questMenu.hideMenu();
    }

    private void minigameRender(float delta) {
        // TODO Minigame implementation
        batch.setProjectionMatrix(camera.combined);
        clearScreen();
        levelAPI.update();
        controller.forEach(AbstractController::update);
        camera.update();
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            minigame.up();
            minigameScreen.updateScreen(minigame);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            minigame.down();
            minigameScreen.updateScreen(minigame);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!minigame.push() || minigame.isFinished()) {
                minigameScreen.hideMenu();
                toggleMinigame();
                return;
            }
            minigameScreen.updateScreen(minigame);
        }
    }

    /** Checks for saves */
    protected void prepareSetup() {
        saves.load();
        if (saves.getAutoSave() != null && saves.getAutoSave().isPresent()) {
            setup(saves.getAutoSave().get());
            // setup();
        } else {
            setup();
        }
    }

    /** Called once at the beginning of the game. */
    protected void setup() {
        level = 0;
        doSetup = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        systems = new SystemController();
        controller.add(systems);
        minigameScreen = new MinigameScreen();
        controller.add(minigameScreen);
        questMenu = new QuestMenu();
        controller.add(questMenu);
        questLogMenu = new QuestLogMenu();
        controller.add(questLogMenu);
        pauseMenu = new PauseMenu<>();
        controller.add(pauseMenu);
        gameOverMenu = new GameOverMenu(this);
        controller.add(gameOverMenu);
        hero = new Hero();
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);
        createSystems();
    }

    protected void setup(GameData gameData) {
        level = gameData.level();
        doSetup = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        systems = new SystemController();
        controller.add(systems);
        minigameScreen = new MinigameScreen();
        controller.add(minigameScreen);
        questMenu = new QuestMenu();
        controller.add(questMenu);
        questLogMenu = new QuestLogMenu();
        controller.add(questLogMenu);
        pauseMenu = new PauseMenu<>();
        controller.add(pauseMenu);
        gameOverMenu = new GameOverMenu(this);
        controller.add(gameOverMenu);
        hero = gameData.hero();
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);
        createSystems();
    }

    /**
     * Called at the beginning of each frame. Before the controllers call
     * <code>update</code>.
     */
    protected void frame() {
        setCameraFocus();
        manageEntitiesSets();
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P))
            togglePause();
        // "K" the Suicide Button (You'll probably want to press it)
        if (Gdx.input.isKeyJustPressed(Input.Keys.K))
            ((HealthComponent) hero.getComponent(HealthComponent.class).get())
                    .receiveHit(new Damage(100 * ((HealthComponent) hero.getComponent(HealthComponent.class).get())
                            .getMaximalHealthpoints(), DamageType.PHYSICAL, hero));
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            throw new Flag();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            toggleQuestLog();
        }
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clear();
        getHero().ifPresent(this::placeOnLevelStart);
        levelSetup();
        level++;
        GameData data = new GameData(hero, level);
        saves.setAutoSave(Optional.of(data));
        saves.save();
        gameLogger.info("Level: " + level);
        gameLogger.info("PlayerLevel: " + Long.toString(((Entity) hero).getComponent(XPComponent.class).isPresent()
                ? ((Entity) hero).getComponent(XPComponent.class).map(XPComponent.class::cast).get().getCurrentLevel()
                : 0));
        new Mimic(Game.getLevel());
    }

    private void manageEntitiesSets() {
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        for (Entity entity : entitiesToAdd) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was added.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
    }

    private void setCameraFocus() {
        if (getHero().isPresent()) {
            PositionComponent pc = (PositionComponent) getHero()
                    .get()
                    .getComponent(PositionComponent.class)
                    .orElseThrow(
                            () -> new MissingComponentException(
                                    "PositionComponent"));
            camera.setFocusPoint(pc.getPosition());

        } else
            camera.setFocusPoint(new Point(0, 0));
    }

    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero))
            levelAPI.loadLevel(LEVELSIZE);
    }

    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
        return currentTile.equals(currentLevel.getEndTile());
    }

    private void placeOnLevelStart(Entity hero) {
        entities.add(hero);
        PositionComponent pc = (PositionComponent) hero.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /** Toggle between pause and run */
    public static void togglePause() {
        paused = !paused;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (pauseMenu != null) {
            if (paused)
                pauseMenu.showMenu();
            else
                pauseMenu.hideMenu();
        }
    }

    /** Toggle between questLog and run */
    public static void toggleQuestLog() {
        inQuestLog = !inQuestLog;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (questLogMenu != null && hero != null) {
            if (inQuestLog) {
                StringBuilder sb = new StringBuilder();
                sb.append("QuestLog:\n");
                ((QuestComponent) hero.getComponent(QuestComponent.class).get())
                        .getQuestLog().stream()
                        .filter(q -> q != null)
                        .forEach(q -> sb.append(q).append('\n'));
                int questAmount = (int) ((QuestComponent) hero
                        .getComponent(QuestComponent.class).get())
                        .getQuestLog().stream()
                        .filter(q -> q != null)
                        .count();
                questLogMenu.display(sb.toString(), questAmount);
            } else
                questLogMenu.hideMenu();
        }
    }

    /** Opens Game Over Screen */
    public static void gameOver() {
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        deleteAutoSave();
        gameOverMenu.showMenu();
    }

    /** Toggle minigame */
    public static void toggleMinigame() {
        minigameIsActive = !minigameIsActive;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (minigameIsActive)
            minigameScreen.showMenu();
        else {
            minigameScreen.hideMenu();
        }
    }

    /**
     * Given entity will be added to the game in the next frame
     *
     * @param entity will be added to the game next frame
     */
    public static void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Given entity will be removed from the game in the next frame
     *
     * @param entity will be removed from the game next frame
     */
    public static void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    /**
     * @return Set with all entities currently in game
     */
    public static Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return Set with all entities that will be added to the game next frame
     */
    public static Set<Entity> getEntitiesToAdd() {
        return entitiesToAdd;
    }

    /**
     * @return Set with all entities that will be removed from the game next frame
     */
    public static Set<Entity> getEntitiesToRemove() {
        return entitiesToRemove;
    }

    /**
     * @return the player character, can be null if not initialized
     */
    public static Optional<Entity> getHero() {
        return Optional.ofNullable(hero);
    }

    /**
     * set the reference of the playable character careful: old hero will not be
     * removed from the
     * game
     *
     * @param hero new reference of hero
     */
    public static void setHero(Entity hero) {
        Game.hero = hero;
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    private void createSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new PlayerSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();
        new QuestSystem();
        new ManaSystem();
    }

    /** returns current level of the dungeon */
    public static int getLevel() {
        return level;
    }

    /** restarts the game by redoing the setup */
    public void restart() {
        setup();
    }

    // spawns both monsters and taps accordingly to the size of the floor
    private void levelSetup() {
        spawnItems();
        for (int i = 0; i < (level * currentLevel.getFloorTiles().size()) / 100; i++) {
            spawnMonster();
        }
        for (int i = 0; i < level; i++) {
            if (i % 5 == 0)
                spawnTraps();
        }
        addEntity(new Tombstone(new Ghost()));
        addEntity(new QuestButton());
        List<ItemData> items = new ArrayList<>(4);
        items.add(SpeedPotion.getItemData());
        items.add(Cake.getItemData());
        items.add(MonsterPotion.getItemData());
        items.add(Bag.getItemData());
        addEntity(
                new Chest(items, Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint(), false));
    }

    // Monster spawn mechanics
    private void spawnMonster() {
        int random = (int) (Math.random() * 11);
        if (random < 3)
            addEntity(new Imp(level));
        else if (random < 6)
            addEntity(new Chort(level));
        else if (random < 9)
            addEntity(new DarkKnight(level));
        else
            addEntity(new Boss(level));
    }

    // Trap spawn mechanics
    private void spawnTraps() {
        int random = (int) (Math.random() * 3);
        if (random == 0)
            addEntity(new TeleportationTrap());
        else if (random == 1)
            addEntity(new SummoningTrap());
        else
            addEntity(new DamageTrap());
    }

    // Item spawn mechanics
    private void spawnItems() {
        addEntity(new Bag());
        addEntity(new Cake());
        addEntity(new SpeedPotion());
        addEntity(new MonsterPotion());
    }

    /** removes the save of the last level */
    private static void deleteAutoSave() {
        saves.setAutoSave(Optional.empty());
        saves.deleteAutoSave();
    }
}
