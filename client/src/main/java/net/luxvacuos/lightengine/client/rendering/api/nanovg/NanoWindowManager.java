/*
 * This file is part of Light Engine
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.lightengine.client.rendering.api.nanovg;

import static net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem.REGISTRY;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.luxvacuos.lightengine.client.core.ClientVariables;
import net.luxvacuos.lightengine.client.input.Mouse;
import net.luxvacuos.lightengine.client.rendering.api.glfw.Sync;
import net.luxvacuos.lightengine.client.rendering.api.glfw.Window;
import net.luxvacuos.lightengine.client.rendering.api.glfw.WindowManager;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.effects.Final;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.effects.GaussianH;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.effects.GaussianV;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.effects.MaskBlur;
import net.luxvacuos.lightengine.client.rendering.api.nanovg.themes.Theme;
import net.luxvacuos.lightengine.client.rendering.api.opengl.GLUtil;
import net.luxvacuos.lightengine.universal.core.TaskManager;
import net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem;
import net.luxvacuos.lightengine.universal.util.registry.Key;

public class NanoWindowManager implements IWindowManager {

	private List<IWindow> windows;
	private Window window;
	private Composite composite;
	private int width, height;
	private IWindow focused;
	private boolean running = true;
	private double lastLoopTime;
	private IShell shell;

	public NanoWindowManager(Window win) {
		this.window = win;
		windows = new ArrayList<>();
		width = (int) (win.getWidth() * win.getPixelRatio());
		height = (int) (win.getHeight() * win.getPixelRatio());

		if (width > GLUtil.getTextureMaxSize())
			width = GLUtil.getTextureMaxSize();
		if (height > GLUtil.getTextureMaxSize())
			height = GLUtil.getTextureMaxSize();
		composite = new Composite(win, width, height);
		composite.addEffect(new MaskBlur(width, height));
		composite.addEffect(new GaussianV(width, height));
		composite.addEffect(new GaussianH(width, height));
		composite.addEffect(new Final(width, height));

		Thread th = new Thread(() -> {
			lastLoopTime = WindowManager.getTime();
			int ups = (int) REGISTRY.getRegistryItem(new Key("/Light Engine/Settings/Core/ups"));
			float delta = 0;
			float accumulator = 0f;
			float interval = 1f / ups;
			Sync sync = new Sync();
			while (running) {
				delta = getDelta();
				accumulator += delta;
				while (accumulator >= interval) {
					updateThread(interval);
					accumulator -= interval;
				}
				sync.sync(ups);
			}
		});
		th.setName("Nano Window Manager");
		// th.start();
		REGISTRY.register(new Key("/Light Engine/Settings/WindowManager/shellHeight"), 0f);
	}

	@Override
	public void render() {
		for (IWindow window : windows) {
			window.render(this.window, this);
		}
		glDisable(GL_BLEND);
		for (IWindow window : windows) {
			if (!window.isHidden() && !window.isMinimized())
				composite.render(window, this.window);
		}
		window.beingNVGFrame();
		Theme.renderImage(this.window.getNVGID(), 0, 0, window.getWidth(), window.getHeight(),
				composite.getFbos()[0].image(), 1f);
		if (ClientVariables.debug) {
			Timers.renderDebugDisplay(5, 24, 200, 55);
			Theme.renderText(window.getNVGID(), "Light Engine " + " (" + ClientVariables.version + ")", "Roboto-Bold",
					NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE, 5, 12, 20, Theme.rgba(220, 220, 220, 255, Theme.colorA));
			Theme.renderText(window.getNVGID(),
					"Used VRam: " + WindowManager.getUsedVRAM() + "KB " + " UPS: " + CoreSubsystem.ups, "Roboto-Bold",
					NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE, 5, 95, 20, Theme.rgba(220, 220, 220, 255, Theme.colorA));
			Theme.renderText(window.getNVGID(), "Used RAM: " + Runtime.getRuntime().totalMemory() / 1028 + "KB ",
					"Roboto-Bold", NVG_ALIGN_LEFT | NVG_ALIGN_MIDDLE, 5, 110, 20,
					Theme.rgba(220, 220, 220, 255, Theme.colorA));
		}
		this.window.endNVGFrame();
	}

	private void updateThread(float delta) {
		List<IWindow> tmp = new ArrayList<>();
		IWindow toTop = null;
		for (IWindow window : windows) {
			if (window.shouldClose()) {
				notifyClose(window);
				TaskManager.addTask(() -> {
					window.dispose(this.window);
				});
				tmp.add(window);
				continue;
			}
			if (window.insideWindow() && !window.isBackground() && !window.isHidden() && !window.isMinimized()
					&& (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !focused.isDragging()
					&& !focused.isResizing()) {
				toTop = window;
			}
		}
		windows.removeAll(tmp);
		tmp.clear();
		if (toTop != null) {
			bringToFront(toTop);
		}
		tmp.addAll(windows);
		Collections.reverse(tmp);
		for (IWindow window : tmp) {
			if (window.insideWindow() && !window.isHidden() && !window.isMinimized() && !focused.isDragging()
					&& !focused.isResizing()) {
				focused = window;
				break;
			}
		}
		if (focused != null)
			focused.update(delta, window, this);
		for (IWindow window : tmp) {
			window.alwaysUpdate(delta, this.window, this);
		}
		tmp.clear();
		if (window.getKeyboardHandler().isKeyPressed(GLFW.GLFW_KEY_F1))
			ClientVariables.debug = !ClientVariables.debug;
	}

	@Override
	public void update(float delta) {
		updateThread(delta);
	}

	@Override
	public void dispose() {
		running = false;
		composite.dispose(window);
		for (IWindow window : windows) {
			window.dispose(this.window);
		}
		windows.clear();
	}

	public List<IWindow> getWindows() {
		return windows;
	}

	@Override
	public void addWindow(IWindow window) {
		TaskManager.addTask(() -> {
			window.init(this.window);
			window.update(0, this.window, this);
			window.alwaysUpdate(0, this.window, this);
			this.windows.add(window);
			this.focused = window;
			notifyAdd(window);
		});
	}

	@Override
	public void addWindow(int ord, IWindow window) {
		TaskManager.addTask(() -> {
			window.init(this.window);
			window.update(0, this.window, this);
			window.alwaysUpdate(0, this.window, this);
			this.windows.add(ord, window);
			this.focused = window;
			notifyAdd(window);
		});
	}

	@Override
	public void removeWindow(IWindow window) {
		TaskManager.addTask(() -> {
			window.closeWindow();
		});
	}

	@Override
	public void bringToFront(IWindow window) {
		IWindow top = windows.get(windows.size() - 1);
		if (top != window)
			if (!top.isAlwaysOnTop() && !top.isHidden()) {
				windows.remove(window);
				windows.add(window);
			}
	}

	@Override
	public boolean isOnTop(IWindow window) {
		IWindow top = windows.get(windows.size() - 1);
		return top == window;
	}

	@Override
	public void notifyClose(IWindow window) {
		if (this.shell != null)
			this.shell.notifyClose(window);
	}

	@Override
	public void notifyAdd(IWindow window) {
		if (this.shell != null)
			if (window.hasDecorations() && !window.isHidden())
				this.shell.notifyAdd(window);
	}

	@Override
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	@Override
	public void toggleShell() {
		if (shell != null)
			shell.toggleShell();
	}

	@Override
	public boolean isShellEnabled() {
		if (shell != null)
			return shell.isEnabled();
		return false;
	}

	public float getDelta() {
		double time = WindowManager.getTime();
		float delta = (float) (time - this.lastLoopTime);
		this.lastLoopTime = time;
		return delta;
	}

}