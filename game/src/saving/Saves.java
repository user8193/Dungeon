package saving;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.ClassNotFoundException;
import java.util.Optional;
import java.util.logging.Logger;

import ecs.components.HealthComponent;
import ecs.components.InventoryComponent;
import ecs.components.ManaComponent;
import ecs.components.quests.QuestComponent;
import ecs.entities.Hero;

public class Saves {

    @SuppressWarnings("unchecked")
    private Optional<GameData>[] saves = (Optional<GameData>[]) new Optional<?>[8];
    private Optional<GameData> autoSave;
    private final Logger savesLogger = Logger.getLogger(Saves.class.getName());

    public boolean save() {
        try {
            for (int i = 0; i < saves.length; i++) {
                if (saves[i] != null)
                    GameData.save(saves[i].get(), "saves/" + (int) (1 + i) + ".txt");
            }
            GameData.save(autoSave.get(), "saves/autosave.txt");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void load() {
        for (int i = 0; i < saves.length; i++) {
            try {
                saves[i] = GameData.load("saves" + 1 + i + ".txt");
                setupHero(saves[i].get());
            } catch (IOException | ClassNotFoundException e) {
                savesLogger.info(e.getLocalizedMessage() + '\n' + exceptionToString(e));
            }
        }
        try {
            autoSave = GameData.load("saves/autosave.txt");
            setupHero(autoSave.get());
        } catch (IOException | ClassNotFoundException e) {
            savesLogger.info(e.getLocalizedMessage() + '\n' + exceptionToString(e));
        }

    }

    private void setupHero(GameData gameData) {
        HealthComponent health = (HealthComponent) gameData.hero().getComponent(HealthComponent.class).get();
        ManaComponent mana = (ManaComponent) gameData.hero().getComponent(ManaComponent.class).get();
        QuestComponent quest = (QuestComponent) gameData.hero().getComponent(QuestComponent.class).get();

        InventoryComponent inventory = (InventoryComponent) gameData.hero().getComponent(InventoryComponent.class)
                .get();
        ((Hero) gameData.hero()).setupComponents(health.getMaximalHealthpoints(), health.getCurrentHealthpoints(),
                quest.getQuestLog(), mana.getMaxMana(), mana.getCurrentMana());
        InventoryComponent ic = (InventoryComponent) gameData.hero().getComponent(InventoryComponent.class).get();
        inventory.getItems().stream()
                .filter(i -> i != null)
                .forEach(ic::addItem);
        quest.getQuestLog().stream()
                .map(q -> q.getTask())
                // Get the ITask of the Quest
                .forEach(t -> t.load(gameData.hero()));
        // Load the ITask so it uses the new components
    }

    public Optional<GameData> getAutoSave() {
        return autoSave;
    }

    public Optional<GameData>[] getSaves() {
        return saves;
    }

    public void setAutoSave(Optional<GameData> autoSave) {
        this.autoSave = autoSave;
    }

    public void setSaves(Optional<GameData>[] saves) {
        this.saves = saves;
    }

    public void deleteAutoSave() {
        try {
            Files.deleteIfExists(Paths.get("saves/autosave.txt"));
        } catch (Exception e) {
            //
        }
    }

    private static String exceptionToString(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder(stackTrace.length * 20);
        sb.append(throwable.getLocalizedMessage()).append("\n");
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("\tat " + stackTraceElement + "\n");
        }
        for (Throwable t : throwable.getSuppressed()) {
            sb.append("\t" + exceptionToString(t) + "\n");
        }
        if (throwable.getCause() != null)
            sb.append(throwable.getCause());
        return sb.toString();
    }

}
