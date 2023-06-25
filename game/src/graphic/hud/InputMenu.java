package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import ecs.components.WalletComponent;
import ecs.entities.Boss;
import ecs.entities.Entity;
import ecs.entities.Item;
import ecs.entities.Shop;
import ecs.items.ItemData;
import starter.Game;
import tools.Constants;
import tools.Point;

import java.util.List;
import java.util.logging.Logger;

/**
 * let you make inputs
 * <p>
 * with inputs, you can interact with the Game
 */
public class InputMenu <T extends Actor> extends ScreenController<T> {
    private transient final Logger inputLogger = Logger.getLogger(this.getClass().getName());
    private static final String path = "animation/command.png";
    private static ScreenInput screenInput = new ScreenInput("Please enter here", new Point(0, 0));

    private static String input = "";
    private static ScreenImage screenImage = new ScreenImage(path, new Point(0, 0));;
    private T outputText;
    private T coins;
    private boolean boss = false;

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     */
    public InputMenu() {
        this(new SpriteBatch());
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param spriteBatch the batch which should be used to draw with
     */
    public InputMenu(SpriteBatch spriteBatch) {
        super(spriteBatch);
        createOutput("");
        add((T) screenImage);
        add((T) screenInput);
        hideMenu();
    }

    /**
     * Returns the input
     * @return is a String, wih the input
     */
    public String result() {
        return input;
    }

    /**
     * Creates output
     * @param output the String you want to print
     */
    public void createOutput(String output){
        ScreenText screenText = new ScreenText(
            output,
            new Point(0, 30),
            3,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.GREEN)
                .build());
        add((T) screenText);
        outputText = (T) screenText;
        inputLogger.info("Text was printed");
    }

    /**
     * Removes the last output
     */
    public void removeOutputText(){
        this.remove(outputText);
        inputLogger.info("InputMenu is open");
    }

    /**
     * shows the Menu
     */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
        inputLogger.info("InputMenu is closed");
    }

    /**
     * hides the Menu
     */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
    }

    /**
     * saves the input
     */
    public void input(){
        input = screenInput.getText();
        showCoins();
        if (result() != null) {
            boss();
        }
    }

    private void showCoins(){
        if(Game.getHero().isPresent()) {
            if (Game.getHero().get().getComponent(WalletComponent.class).isPresent()) {
                if(coins != null){
                    this.remove(this.coins);
                }
                WalletComponent wc = (WalletComponent) Game.getHero().get().getComponent(WalletComponent.class).get();
                ScreenText screenText = new ScreenText(
                    wc.getCoins() + " Coins",
                    new Point(200, 0),
                    3,
                    new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontcolor(Color.GREEN)
                        .build());
                add((T) screenText);
                coins = (T) screenText;
            }
        }
    }
    private void boss(){
        if (result().matches(".*Boss.*") && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            removeOutputText();
            for (int x = 0; x < Game.getEntities().toArray().length; x++){
                if(Game.getEntities().toArray()[x] instanceof Boss){
                    createOutput("The Boss will despawn if you tell the LevelNumber!");
                    boss = true;
                }
            }
        } else if (boss && result().matches("" + Game.getLevel()) && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            for (int x = 0; x < Game.getEntities().toArray().length; x++){
                if(Game.getEntities().toArray()[x] instanceof Boss){
                    Game.removeEntity((Entity) Game.getEntities().toArray()[x]);
                    boss = false;
                }
            }
            removeOutputText();
            createOutput("Boss despawned");
        }
    }
}
