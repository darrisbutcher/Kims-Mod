package com.kimbersmod.securitycams.client;

import com.kimbersmod.securitycams.CameraBlockEntity;
import com.kimbersmod.securitycams.LinkedCamera;
import com.kimbersmod.securitycams.ModComponents;
import com.kimbersmod.securitycams.ModItems;
import com.kimbersmod.securitycams.TabletActionPayload;
import com.kimbersmod.securitycams.client.mixin.CameraAccessor;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The tablet. While it is open the game's own camera is moved onto the selected security camera, so the
 * whole world view is rendered from there (entities, weather, lighting and all). The buttons along the
 * bottom switch cameras, aim them and rename them.
 */
public class TabletScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GREY = 0xFFB0B8C0;
	private static final int RED = 0xFFE03030;

	private final InteractionHand hand;
	private List<LinkedCamera> cameras = List.of();
	private int selected;
	private int ticks;
	private int lastHurtTime;

	private @Nullable Marker viewEntity;
	private @Nullable CameraType previousCameraType;
	private boolean viewing;
	private @Nullable Component status;
	private boolean hudHiddenByUs;
	private @Nullable Component alert;
	private int alertTicks;

	private @Nullable EditBox nameBox;
	private @Nullable Button sweepButton;

	public TabletScreen(InteractionHand hand) {
		super(Component.translatable("securitycams.tablet.title"));
		this.hand = hand;
	}

	// ---- setup ---------------------------------------------------------------------------------

	@Override
	protected void init() {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		if (ticks == 0) {
			lastHurtTime = player.hurtTime;
		}
		String pendingName = nameBox != null ? nameBox.getValue() : null;
		cameras = readCameras();

		int buttonY = height - 26;
		int x = Math.max(4, (width - 370) / 2);

		addRenderableWidget(Button.builder(Component.literal("<<"), b -> cycle(-1)).bounds(x, buttonY, 24, 20).build());
		x += 28;
		addRenderableWidget(Button.builder(Component.literal(">>"), b -> cycle(1)).bounds(x, buttonY, 24, 20).build());
		x += 28;
		addRenderableWidget(Button.builder(Component.translatable("securitycams.tablet.button.left"), b -> aim(-1)).bounds(x, buttonY, 20, 20).build());
		x += 24;
		addRenderableWidget(Button.builder(Component.translatable("securitycams.tablet.button.right"), b -> aim(1)).bounds(x, buttonY, 20, 20).build());
		x += 24;
		sweepButton = addRenderableWidget(
			Button.builder(Component.translatable("securitycams.tablet.button.sweep_off"), b -> toggleSweep()).bounds(x, buttonY, 64, 20).build());
		x += 68;
		nameBox = addRenderableWidget(new EditBox(font, x, buttonY, 100, 20, Component.translatable("securitycams.tablet.name")));
		nameBox.setMaxLength(LinkedCamera.MAX_NAME_LENGTH);
		x += 104;
		addRenderableWidget(Button.builder(Component.translatable("securitycams.tablet.button.remove"), b -> removeSelected()).bounds(x, buttonY, 50, 20).build());
		x += 54;
		addRenderableWidget(Button.builder(Component.translatable("securitycams.tablet.button.exit"), b -> onClose()).bounds(x, buttonY, 40, 20).build());

		if (cameras.isEmpty()) {
			status = Component.translatable("securitycams.tablet.no_cameras");
			nameBox.setEditable(false);
		} else {
			selectCamera(Mth.clamp(selected, 0, cameras.size() - 1));
			if (pendingName != null && !pendingName.isEmpty()) {
				nameBox.setValue(pendingName);
			}
		}
	}

	private List<LinkedCamera> readCameras() {
		LocalPlayer player = minecraft.player;
		ItemStack stack = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
		if (!stack.is(ModItems.TABLET)) {
			return List.of();
		}
		return stack.getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of());
	}

	// ---- choosing and viewing a camera ---------------------------------------------------------

	private void cycle(int step) {
		if (cameras.isEmpty()) {
			return;
		}
		commitName();
		selectCamera(Math.floorMod(selected + step, cameras.size()));
	}

	private void selectCamera(int index) {
		selected = index;
		if (nameBox != null) {
			nameBox.setValue(cameras.get(index).name());
		}
		updateView();
	}

	private boolean inRange(BlockPos pos) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return false;
		}
		double limit = (minecraft.options.getEffectiveRenderDistance() - 1) * 16.0;
		double dx = pos.getX() + 0.5 - player.getX();
		double dz = pos.getZ() + 0.5 - player.getZ();
		return dx * dx + dz * dz <= limit * limit;
	}

	/** The selected camera's block entity if it can be seen right now; otherwise sets {@link #status}. */
	private @Nullable CameraBlockEntity resolveSelected() {
		ClientLevel level = minecraft.level;
		if (level == null || cameras.isEmpty()) {
			return null;
		}
		LinkedCamera camera = cameras.get(selected);
		BlockPos pos = camera.pos().pos();
		if (!camera.pos().dimension().equals(level.dimension())) {
			status = Component.translatable("securitycams.tablet.other_dimension");
		} else if (!inRange(pos) || !level.hasChunkAt(pos)) {
			status = Component.translatable("securitycams.tablet.out_of_range");
		} else if (level.getBlockEntity(pos) instanceof CameraBlockEntity be) {
			status = null;
			return be;
		} else {
			status = Component.translatable("securitycams.tablet.missing");
		}
		return null;
	}

	private void updateView() {
		CameraBlockEntity camera = resolveSelected();
		if (camera == null) {
			stopViewing();
		} else {
			startViewing();
			placeViewEntity(camera, true);
		}
		updateSweepLabel(camera);
	}

	private void startViewing() {
		if (viewEntity == null) {
			viewEntity = new Marker(EntityTypes.MARKER, minecraft.level);
		}
		if (!viewing) {
			previousCameraType = minecraft.options.getCameraType();
			minecraft.options.setCameraType(CameraType.FIRST_PERSON);
			// Hide the hotbar, crosshair and the hand holding the tablet so the feed is clean.
			if (!minecraft.gui.hud.isHidden()) {
				minecraft.gui.hud.toggle();
				hudHiddenByUs = true;
			}
			viewing = true;
		}
		minecraft.setCameraEntity(viewEntity);
		setCameraEyeHeight(viewEntity.getEyeHeight());
	}

	private void stopViewing() {
		if (!viewing) {
			return;
		}
		viewing = false;
		if (hudHiddenByUs) {
			if (minecraft.gui.hud.isHidden()) {
				minecraft.gui.hud.toggle();
			}
			hudHiddenByUs = false;
		}
		if (previousCameraType != null) {
			minecraft.options.setCameraType(previousCameraType);
		}
		LocalPlayer player = minecraft.player;
		if (player != null) {
			minecraft.setCameraEntity(player);
			setCameraEyeHeight(player.getEyeHeight());
		}
	}

	private void setCameraEyeHeight(float eyeHeight) {
		CameraAccessor accessor = (CameraAccessor) minecraft.gameRenderer.mainCamera();
		accessor.securitycams$setEyeHeight(eyeHeight);
		accessor.securitycams$setEyeHeightOld(eyeHeight);
	}

	/** Moves the stand-in camera entity onto the security camera, and aims it. */
	private void placeViewEntity(CameraBlockEntity camera, boolean snap) {
		if (viewEntity == null || minecraft.level == null) {
			return;
		}
		// The lens moves as the head turns, so both position and rotation follow the camera each tick.
		float time = (float) minecraft.level.getGameTime();
		Vec3 eye = camera.eyePosition(time);
		if (!snap) {
			viewEntity.setOldPosAndRot();
		}
		viewEntity.setPos(eye.x, eye.y, eye.z);
		viewEntity.setYRot(camera.viewYaw(time));
		viewEntity.setXRot(camera.viewPitch());
		if (snap) {
			viewEntity.setOldPosAndRot();
		}
	}

	private void updateSweepLabel(@Nullable CameraBlockEntity camera) {
		if (sweepButton == null) {
			return;
		}
		boolean on = camera != null && camera.isSweeping();
		sweepButton.setMessage(Component.translatable(on ? "securitycams.tablet.button.sweep_on" : "securitycams.tablet.button.sweep_off"));
		sweepButton.active = camera != null;
	}

	/** Shows a motion alert banner on the feed for a few seconds. */
	public void showAlert(Component message) {
		alert = message;
		alertTicks = 80;
	}

	// ---- actions -------------------------------------------------------------------------------

	private void send(int action, int value, String text) {
		if (cameras.isEmpty()) {
			return;
		}
		ClientPlayNetworking.send(new TabletActionPayload(action, selected, value, text, hand == InteractionHand.OFF_HAND));
	}

	private void aim(int direction) {
		send(TabletActionPayload.ACTION_AIM, direction, "");
	}

	private void toggleSweep() {
		CameraBlockEntity camera = resolveSelected();
		if (camera != null) {
			send(TabletActionPayload.ACTION_SWEEP, camera.isSweeping() ? 0 : 1, "");
		}
	}

	private void removeSelected() {
		if (cameras.isEmpty()) {
			return;
		}
		send(TabletActionPayload.ACTION_REMOVE, 0, "");
		stopViewing();
		// The server's updated tablet arrives within a tick or two; until then show nothing stale.
		status = Component.translatable("securitycams.tablet.removing");
	}

	private void commitName() {
		if (nameBox == null || cameras.isEmpty() || selected >= cameras.size()) {
			return;
		}
		String typed = nameBox.getValue().strip();
		if (!typed.isEmpty() && !typed.equals(cameras.get(selected).name())) {
			send(TabletActionPayload.ACTION_RENAME, 0, typed);
		}
	}

	// ---- screen lifecycle ----------------------------------------------------------------------

	@Override
	public void tick() {
		super.tick();
		ticks++;
		if (alertTicks > 0) {
			alertTicks--;
		}
		LocalPlayer player = minecraft.player;
		if (player == null || !player.isAlive()) {
			onClose();
			return;
		}
		// Getting hurt drops you out of the feed so you can react.
		if (player.hurtTime > lastHurtTime) {
			onClose();
			return;
		}
		lastHurtTime = player.hurtTime;

		List<LinkedCamera> latest = readCameras();
		if (!latest.equals(cameras)) {
			boolean sameSelection = !cameras.isEmpty() && selected < cameras.size() && selected < latest.size()
				&& cameras.get(selected).pos().equals(latest.get(selected).pos());
			cameras = latest;
			if (cameras.isEmpty()) {
				stopViewing();
				status = Component.translatable("securitycams.tablet.no_cameras");
				return;
			}
			selected = Mth.clamp(selected, 0, cameras.size() - 1);
			if (!sameSelection) {
				selectCamera(selected);
			}
		}
		if (cameras.isEmpty()) {
			return;
		}

		CameraBlockEntity camera = resolveSelected();
		if (camera == null) {
			stopViewing();
		} else {
			if (!viewing) {
				startViewing();
				placeViewEntity(camera, true);
			} else {
				placeViewEntity(camera, false);
			}
		}
		updateSweepLabel(camera);
	}

	@Override
	public void removed() {
		commitName();
		stopViewing();
		super.removed();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (nameBox != null && nameBox.isFocused() && event.isConfirmation()) {
			commitName();
			nameBox.setFocused(false);
			return true;
		}
		return super.keyPressed(event);
	}

	// ---- drawing -------------------------------------------------------------------------------

	/** The world behind the screen is the camera feed, so don't blur or darken it. */
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int w = width;
		int h = height;

		if (viewing) {
			drawFeedOverlay(graphics, w, h);
		} else {
			graphics.fill(0, 0, w, h, 0xF0101418);
			if (status != null) {
				graphics.centeredText(font, status, w / 2, h / 2 - 4, GREY);
			}
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private void drawFeedOverlay(GuiGraphicsExtractor graphics, int w, int h) {
		// Scanlines.
		for (int y = 0; y < h; y += 4) {
			graphics.fill(0, y, w, y + 1, 0x16000000);
		}
		// Top and bottom bars.
		graphics.fill(0, 0, w, 18, 0xA0000000);
		graphics.fill(0, h - 32, w, h, 0xB0000000);
		// Corner brackets.
		int m = 24;
		int len = 14;
		int top = 24;
		int bottom = h - 38;
		bracket(graphics, m, top, len, 1, 1);
		bracket(graphics, w - m, top, len, -1, 1);
		bracket(graphics, m, bottom, len, 1, -1);
		bracket(graphics, w - m, bottom, len, -1, -1);

		// Recording light and labels.
		if ((ticks / 10) % 2 == 0) {
			graphics.fill(8, 5, 14, 11, RED);
		}
		graphics.text(font, Component.translatable("securitycams.tablet.rec"), 18, 5, WHITE);
		LinkedCamera camera = cameras.get(Mth.clamp(selected, 0, cameras.size() - 1));
		graphics.centeredText(font, Component.literal(camera.name() + "  (" + (selected + 1) + "/" + cameras.size() + ")"), w / 2, 5, WHITE);
		long dayTime = minecraft.level == null ? 0 : minecraft.level.getOverworldClockTime();
		long minutesOfDay = ((dayTime + 6000L) % 24000L) * 1440L / 24000L;
		String clock = String.format("%02d:%02d", minutesOfDay / 60, minutesOfDay % 60);
		graphics.text(font, clock, w - 8 - font.width(clock), 5, WHITE);

		// Motion alert banner, with a flashing red frame.
		if (alertTicks > 0 && alert != null) {
			graphics.fill(0, 18, w, 32, 0xC0801010);
			graphics.centeredText(font, alert, w / 2, 21, WHITE);
			if ((alertTicks / 5) % 2 == 0) {
				graphics.outline(0, 18, w, h - 18 - 32, 0xE0E03030);
				graphics.outline(1, 19, w - 2, h - 18 - 32 - 2, 0xE0E03030);
			}
		}

		// Power-on flicker.
		int flicker = 200 - ticks * 28;
		if (flicker > 0) {
			graphics.fill(0, 0, w, h, (flicker << 24) | 0x202830);
		}
	}

	private static void bracket(GuiGraphicsExtractor graphics, int x, int y, int len, int dx, int dy) {
		int x2 = x + dx * len;
		int y2 = y + dy * len;
		graphics.fill(Math.min(x, x2), y, Math.max(x, x2) + 1, y + 1, 0xC0FFFFFF);
		graphics.fill(x, Math.min(y, y2), x + 1, Math.max(y, y2) + 1, 0xC0FFFFFF);
	}
}
