package net.guerra24.infinity.client.core.states;

import org.lwjgl.nanovg.NVGColor;

import net.guerra24.infinity.client.core.GlobalStates;
import net.guerra24.infinity.client.core.GlobalStates.GameState;
import net.guerra24.infinity.client.core.Infinity;
import net.guerra24.infinity.client.core.State;
import net.guerra24.infinity.client.graphics.MenuRendering;
import net.guerra24.infinity.client.graphics.opengl.Display;
import net.guerra24.infinity.client.resources.GameResources;
import net.guerra24.infinity.universal.util.vector.Vector3f;

/**
 * Main Menu State
 * 
 * @author danirod
 * @category Kernel
 */
public class MainMenuState extends State {

	public MainMenuState() {
		super(1);
	}

	@Override
	public void render(Infinity voxel, GlobalStates states, float delta) {
		GameResources gm = voxel.getGameResources();
		Display display = voxel.getDisplay();
		gm.getFrustum().calculateFrustum(gm.getRenderer().getProjectionMatrix(), gm.getCamera());
		gm.getRenderer().prepare();
	}

	@Override
	public void update(Infinity voxel, GlobalStates states, float delta) {
		GameResources gm = voxel.getGameResources();

		if (gm.getMenuSystem().mainMenu.getPlayButton().pressed()) {
			gm.getMenuSystem().gameSP.load(gm);
			states.setState(GameState.LOADING_WORLD);
		} else if (gm.getMenuSystem().mainMenu.getExitButton().pressed()) {
			states.loop = false;
		} else if (gm.getMenuSystem().mainMenu.getOptionsButton().pressed()) {
			gm.getMenuSystem().optionsMenu.load(gm);
			gm.getCamera().setPosition(new Vector3f(-1.4f, -3.4f, 1.4f));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			states.setState(GameState.OPTIONS);
		}
	}

}
