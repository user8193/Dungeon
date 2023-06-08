package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import ecs.entities.Boss;
import ecs.entities.Entity;
import starter.Game;
import tools.Constants;
import tools.Point;

public class InputMenu <T extends Actor> extends ScreenController<T> {
    private static final String path = "animation/command.png";
    private static ScreenInput screenInput = new ScreenInput("Please enter here", new Point(0, 0));

    public static String input;
    private static ScreenImage screenImage = new ScreenImage(path, new Point(0, 0));;
    private T outputText;
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

    public String result() {
        return input;
    }

    public void save() {
        input = screenInput.getText();
    }

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
    }

    public void removeOutputText(){
        this.remove(outputText);
    }

    /**
     * shows the Menu
     */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    /**
     * hides the Menu
     */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
    }

    public void input(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            save();
        }
        if (result() != null) {
            boss();
            shop();
        }
    }

    private void boss(){
        if (result().matches(".*Boss.*")) {
            removeOutputText();
            for (int x = 0; x < Game.getEntities().toArray().length; x++){
                if(Game.getEntities().toArray()[x] instanceof Boss){
                    createOutput("The Boss will despawn if you tell the LevelNumber!");
                    boss = true;
                }
            }
        } else if (boss && result().matches("" + Game.getLevel())) {
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


    private void shop(){
        if (result().matches(".*Shop.*")) {
            removeOutputText();

        } else if (boss && result().matches("" + Game.getLevel())) {
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
