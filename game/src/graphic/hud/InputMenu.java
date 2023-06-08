package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;
import tools.Point;

public class InputMenu <T extends Actor> extends ScreenController<T> {
    private static ScreenInput screenInput;
    public static String input;
    private final String path = "animation/command.png";

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
        ScreenImage screenImage = new ScreenImage(path, new Point(0, 0));
        add((T) screenImage);

        ScreenButton screenButtonRestart = new ScreenButton(
            "Enter",
            new Point(0, 50),
            new TextButtonListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    save();
                }
            },
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.RED)
                .setDownFontColor(Color.BLUE)
                .setOverFontColor(Color.YELLOW)
                .build());
        screenButtonRestart.setPosition(
            2 * (Constants.WINDOW_WIDTH) / 3f - screenButtonRestart.getWidth(),
            (Constants.WINDOW_HEIGHT) / 2.5f + screenButtonRestart.getHeight(),
            Align.center | Align.bottom);
        add((T) screenButtonRestart);

        screenInput = new ScreenInput("Please enter here", new Point(0, 0));
        add((T) screenInput);

        hideMenu();
    }

    public static String result() {
        return input;
    }

    private void save() {
        input = screenInput.getText();
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
}
