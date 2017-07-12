package net.luxvacuos.lightengine.demo;

import static net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem.REGISTRY;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.luxvacuos.igl.vector.Vector3d;
import net.luxvacuos.lightengine.client.core.ClientVariables;
import net.luxvacuos.lightengine.client.core.ClientWorldSimulation;
import net.luxvacuos.lightengine.client.core.subsystems.GraphicalSubsystem;
import net.luxvacuos.lightengine.client.ecs.ClientComponents;
import net.luxvacuos.lightengine.client.ecs.entities.CameraEntity;
import net.luxvacuos.lightengine.client.ecs.entities.RenderEntity;
import net.luxvacuos.lightengine.client.ecs.entities.Sun;
import net.luxvacuos.lightengine.client.input.KeyboardHandler;
import net.luxvacuos.lightengine.client.rendering.api.glfw.Window;
import net.luxvacuos.lightengine.client.rendering.api.opengl.ParticleDomain;
import net.luxvacuos.lightengine.client.rendering.api.opengl.Renderer;
import net.luxvacuos.lightengine.client.resources.AssimpResourceLoader;
import net.luxvacuos.lightengine.client.ui.windows.GameWindow;
import net.luxvacuos.lightengine.client.world.ClientPhysicsSystem;
import net.luxvacuos.lightengine.demo.ecs.entities.TPSCamera;
import net.luxvacuos.lightengine.demo.ui.PauseWindow;
import net.luxvacuos.lightengine.universal.core.states.AbstractState;
import net.luxvacuos.lightengine.universal.core.states.StateMachine;
import net.luxvacuos.lightengine.universal.util.registry.Key;
import net.luxvacuos.lightengine.universal.world.PhysicsSystem;

public class GameState extends AbstractState {

	private PhysicsSystem physicsSystem;
	private Engine engine;
	private Sun sun;
	private ClientWorldSimulation worldSimulation;
	private CameraEntity camera;
	private GameWindow gameWindow;
	private PauseWindow pauseWindow;
	
	private RenderEntity level;

	public static boolean paused = false, exitWorld = false;

	protected GameState() {
		super("mainState");
	}

	@Override
	public void start() {
		Window window = GraphicalSubsystem.getMainWindow();

		//camera = new PlayerCamera("player", UUID.randomUUID().toString());
		camera = new TPSCamera("player", UUID.randomUUID().toString());
		camera.setRotation(new Vector3d(50, 45, 0));
		camera.setPosition(new Vector3d(0,10,0));
		sun = new Sun();
		
		Renderer.setOnResize(() -> {
			ClientComponents.PROJECTION_MATRIX.get(camera)
			.setProjectionMatrix(Renderer.createProjectionMatrix(
					(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/width")),
					(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/height")),
					(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Settings/Core/fov")),
					ClientVariables.NEAR_PLANE, ClientVariables.FAR_PLANE));
		});

		AssimpResourceLoader aLoader = window.getAssimpResourceLoader();
		
		level = new RenderEntity("", aLoader.loadModel("levels/test_state/models/level.blend"));

		worldSimulation = new ClientWorldSimulation(10000);
		engine = new Engine();
		physicsSystem = new ClientPhysicsSystem();
		physicsSystem.addBox(new BoundingBox(new Vector3(-50, -1, -50), new Vector3(50, 0, 50)));
		engine.addSystem(physicsSystem);

		physicsSystem.getEngine().addEntity(camera);
		physicsSystem.getEngine().addEntity(level);
		Renderer.render(engine.getEntities(), ParticleDomain.getParticles(), camera, worldSimulation, sun, 0);
		gameWindow = new GameWindow(0, (int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/height")),
				(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/width")),
				(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/height")));
		GraphicalSubsystem.getWindowManager().addWindow(gameWindow);
		super.start();
	}

	@Override
	public void end() {
		physicsSystem.getEngine().removeAllEntities();
		super.end();
	}

	@Override
	public void update(float delta) {
		GraphicalSubsystem.getWindowManager().update(delta);
		Window window = GraphicalSubsystem.getMainWindow();
		KeyboardHandler kbh = window.getKeyboardHandler();
		if (!paused) {
			Renderer.update(delta);
			engine.update(delta);
			sun.update(camera.getPosition(), worldSimulation.update(delta), delta);
			//particleSystem.generateParticles(particlesPoint, delta);
			ParticleDomain.update(delta, camera);
			
			if (kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				kbh.ignoreKeyUntilRelease(GLFW.GLFW_KEY_ESCAPE);
				paused = true;
				float borderSize = (float) REGISTRY
						.getRegistryItem(new Key("/Light Engine/Settings/WindowManager/borderSize"));
				float titleBarHeight = (float) REGISTRY
						.getRegistryItem(new Key("/Light Engine/Settings/WindowManager/titleBarHeight"));
				int height = (int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/height"));
				pauseWindow = new PauseWindow(borderSize + 10, height - titleBarHeight - 10,
						(int) REGISTRY.getRegistryItem(new Key("/Light Engine/Display/width")) - borderSize * 2f - 20,
						height - titleBarHeight - borderSize - 50);
				GraphicalSubsystem.getWindowManager().addWindow(pauseWindow);
				GraphicalSubsystem.getWindowManager().toggleShell();
			}
		} else if (exitWorld) {
			gameWindow.closeWindow();
			pauseWindow.closeWindow();
			exitWorld = false;
			paused = false;
			StateMachine.setCurrentState("_main");
		} else {
			if (kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				kbh.ignoreKeyUntilRelease(GLFW.GLFW_KEY_ESCAPE);
				paused = false;
				pauseWindow.closeWindow();
				GraphicalSubsystem.getWindowManager().toggleShell();
			}
		}

	}

	@Override
	public void render(float alpha) {
		Renderer.render(engine.getEntities(), ParticleDomain.getParticles(), camera, worldSimulation, sun, alpha);
		Renderer.clearBuffer(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Renderer.clearColors(1, 1, 1, 1);
		GraphicalSubsystem.getWindowManager().render();
	}
}