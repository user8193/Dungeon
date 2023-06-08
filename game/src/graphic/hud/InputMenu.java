package graphic.hud;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import controller.ScreenController;

public class InputMenu <T extends Actor> extends ScreenController<T> {
    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
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
    public InputMenu(SpriteBatch spriteBatch){
        super(spriteBatch);


    }
}
