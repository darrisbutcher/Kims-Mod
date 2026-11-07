package com.kimbersmod.securitycams.test;

import com.kimbersmod.securitycams.CameraBlockEntity;
import com.kimbersmod.securitycams.LinkedCamera;
import com.kimbersmod.securitycams.ModComponents;
import com.kimbersmod.securitycams.ModItems;
import com.kimbersmod.securitycams.follower.FollowerEntity;
import com.kimbersmod.securitycams.follower.LittleVillager;
import com.kimbersmod.securitycams.follower.VeiledChameleon;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.entity.EntityTypeTest;

/** Walks through the whole feature in a real client: place a camera, link a tablet, watch it, sweep it, get an alert. */
public class SecurityCamsClientTest implements FabricClientGameTest {
	private static final BlockPos CAMERA_POS = new BlockPos(4, -58, 0);
	/** Looking east from the player's spot straight at the camera block (eye height -58.38). */
	private static final String AIM_AT_CAMERA = "tp @p 0.5 -60 0.5 -90 -12.4";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			context.waitTicks(20);

			server.runCommand("gamemode creative @p");
			server.runCommand("difficulty normal");
			// A stone wall with a camera on it, looking west over open ground.
			server.runCommand("fill 5 -60 -3 5 -54 3 minecraft:stone");
			server.runCommand("setblock 4 -58 0 securitycams:camera[facing=west]");
			server.runCommand("data merge block 4 -58 0 {base_yaw:90f}");
			// Coloured pillars so a turning camera is visible: red to the north, green ahead, blue to the south.
			server.runCommand("fill -6 -60 -5 -6 -58 -5 minecraft:red_wool");
			server.runCommand("fill -6 -60 0 -6 -58 0 minecraft:lime_wool");
			server.runCommand("fill -6 -60 5 -6 -58 5 minecraft:blue_wool");
			server.runCommand("item replace entity @p weapon.mainhand with securitycams:tablet");

			// A close look at the camera model from the front.
			server.runCommand("tp @p 1.5 -60 0.5 -90 -8");
			context.waitTicks(20);
			context.takeScreenshot("1_camera_closeup");

			// The head should visibly turn when the camera is aimed away from straight ahead.
			server.runCommand("data merge block 4 -58 0 {aim_offset:55f}");
			context.waitTicks(10);
			context.takeScreenshot("1b_head_turned_right");
			server.runCommand("data merge block 4 -58 0 {aim_offset:-55f}");
			context.waitTicks(10);
			context.takeScreenshot("1c_head_turned_left");
			server.runCommand("data merge block 4 -58 0 {aim_offset:0f}");
			context.waitTicks(10);

			// Right-click the camera with the tablet: it should link.
			server.runCommand(AIM_AT_CAMERA);
			context.waitTicks(10);
			String aimedAt = context.computeOnClient(mc -> String.valueOf(mc.hitResult));
			context.getInput().pressKey(options -> options.keyUse);
			context.waitTicks(10);
			int linked = context.computeOnClient(mc ->
				mc.player.getItemInHand(InteractionHand.MAIN_HAND).getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of()).size());
			if (linked != 1) {
				throw new AssertionError("Expected the tablet to be linked to 1 camera, found " + linked + " (aimed at " + aimedAt + ")");
			}

			// Look at the sky and right-click: the tablet screen should open and show the camera's view.
			server.runCommand("tp @p 0.5 -60 0.5 0 -40");
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitFor(mc -> mc.gui.screen() != null && mc.gui.screen().getClass().getSimpleName().equals("TabletScreen"));
			context.waitTicks(30);
			context.takeScreenshot("2_tablet_feed");
			boolean hudHidden = context.computeOnClient(mc -> mc.gui.hud.isHidden());
			if (!hudHidden) {
				throw new AssertionError("The HUD should be hidden while watching a camera");
			}

			// Turn on sweeping and let it swing.
			context.clickScreenButton("securitycams.tablet.button.sweep_off");
			context.waitTicks(10);
			boolean sweeping = server.computeOnServer(s ->
				s.overworld().getBlockEntity(CAMERA_POS) instanceof CameraBlockEntity be && be.isSweeping());
			if (!sweeping) {
				throw new AssertionError("The camera should be sweeping after pressing the sweep button");
			}
			context.waitTicks(50);
			context.takeScreenshot("3_sweeping_a");
			context.waitTicks(60);
			context.takeScreenshot("4_sweeping_b");

			// Something walks into view: the tablet should raise an alert on the feed.
			server.runCommand("summon minecraft:zombie -3 -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
			context.waitTicks(40);
			context.takeScreenshot("5_motion_alert");

			// The camera gets destroyed while we're watching: the feed should drop out and say so.
			server.runCommand("setblock 4 -58 0 minecraft:air");
			context.waitTicks(20);
			context.takeScreenshot("5b_camera_destroyed");
			boolean stillViewing = context.computeOnClient(mc -> mc.getCameraEntity() != mc.player);
			if (stillViewing) {
				throw new AssertionError("The view should return to the player once the camera is destroyed");
			}

			// Remove the dead camera from the tablet.
			context.clickScreenButton("securitycams.tablet.button.remove");
			context.waitTicks(20);
			int remaining = context.computeOnClient(mc ->
				mc.player.getItemInHand(InteractionHand.MAIN_HAND).getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of()).size());
			if (remaining != 0) {
				throw new AssertionError("Expected the camera to be removed from the tablet, found " + remaining);
			}
			context.takeScreenshot("5c_camera_removed");

			// Close the tablet with its Close button: the normal view and HUD should come back.
			context.clickScreenButton("securitycams.tablet.button.exit");
			context.waitFor(mc -> mc.gui.screen() == null);
			context.waitTicks(10);
			boolean backToPlayer = context.computeOnClient(mc -> mc.getCameraEntity() == mc.player && !mc.gui.hud.isHidden());
			if (!backToPlayer) {
				throw new AssertionError("The camera and HUD should return to normal after closing the tablet");
			}
			context.takeScreenshot("6_closed");

			testFollowers(context, server);
		}
	}

	private static List<? extends FollowerEntity> followers(TestServerContext server) {
		return server.computeOnServer(s -> List.copyOf(s.overworld().getEntities(EntityTypeTest.forClass(FollowerEntity.class), e -> true)));
	}

	private static <T extends FollowerEntity> T first(TestServerContext server, Class<T> type) {
		return server.computeOnServer(s -> s.overworld().getEntities(EntityTypeTest.forClass(type), e -> true).getFirst());
	}

	private static void testFollowers(ClientGameTestContext context, TestServerContext server) {
		// Place both figurines on open ground, the way a player would.
		server.runCommand("tp @p 0.5 -60 30.5 0 60");
		server.runCommand("item replace entity @p weapon.mainhand with securitycams:little_villager_figurine");
		context.waitTicks(10);
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(10);
		server.runCommand("tp @p 0.5 -60 30.5 50 60");
		server.runCommand("item replace entity @p weapon.mainhand with securitycams:veiled_chameleon_figurine");
		context.waitTicks(10);
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(10);

		int count = followers(server).size();
		if (count != 2) {
			throw new AssertionError("Expected 2 followers after placing both figurines, found " + count);
		}
		boolean ownedByPlayer = server.computeOnServer(s -> {
			var player = s.getPlayerList().getPlayers().getFirst();
			return s.overworld().getEntities(EntityTypeTest.forClass(FollowerEntity.class), e -> true).stream().allMatch(f -> f.isOwnedBy(player));
		});
		if (!ownedByPlayer) {
			throw new AssertionError("Both followers should belong to the player who placed them");
		}

		// They should walk over to a player who steps away, and show up on screen.
		server.runCommand("tp @p 0.5 -60 38.5 180 18");
		context.waitTicks(60);
		context.takeScreenshot("7_followers_arrive");

		// Leave them far behind: they should teleport to the player.
		server.runCommand("tp @p 0.5 -60 80.5 0 10");
		context.waitTicks(120);
		boolean caughtUp = server.computeOnServer(s -> {
			var player = s.getPlayerList().getPlayers().getFirst();
			return s.overworld().getEntities(EntityTypeTest.forClass(FollowerEntity.class), e -> true).stream().allMatch(f -> f.distanceTo(player) < 8.0F);
		});
		if (!caughtUp) {
			throw new AssertionError("Followers should catch up when the player moves far away");
		}

		// Camouflage: the chameleon should take on the colour of the block it stands on.
		server.runCommand("fill -8 -61 70 9 -61 92 minecraft:sand");
		context.waitTicks(40);
		int onSand = server.computeOnServer(s -> first(server, VeiledChameleon.class).getCamouflage());
		if (onSand != 1) {
			throw new AssertionError("Chameleon on sand should use camouflage look 1 (sand), found " + onSand);
		}
		server.runCommand("execute as @p anchored eyes run rotate @s facing entity @e[type=securitycams:veiled_chameleon,limit=1] eyes");
		context.waitTicks(5);
		context.takeScreenshot("8_chameleon_on_sand");
		server.runCommand("fill -8 -61 70 9 -61 92 minecraft:snow_block");
		context.waitTicks(40);
		int onSnow = server.computeOnServer(s -> first(server, VeiledChameleon.class).getCamouflage());
		if (onSnow != 3) {
			throw new AssertionError("Chameleon on snow should use camouflage look 3 (snow), found " + onSnow);
		}
		context.takeScreenshot("9_chameleon_on_snow");

		// Sit and stay: an empty-handed right-click on the villager makes it stay, and again makes it follow.
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
		server.runCommand("execute as @p anchored eyes run rotate @s facing entity @e[type=securitycams:little_villager,limit=1] eyes");
		context.waitTicks(5);
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(20);
		boolean staying = server.computeOnServer(s -> first(server, LittleVillager.class).isOrderedToSit());
		if (!staying) {
			throw new AssertionError("Right-clicking the villager should make it stay put");
		}
		context.takeScreenshot("10_villager_staying");
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(10);
		boolean following = server.computeOnServer(s -> !first(server, LittleVillager.class).isOrderedToSit());
		if (!following) {
			throw new AssertionError("Right-clicking again should make the villager follow again");
		}

		// Pack away: sneak and right-click turns the villager back into its figurine.
		server.runCommand("execute as @p anchored eyes run rotate @s facing entity @e[type=securitycams:little_villager,limit=1] eyes");
		context.waitTicks(5);
		context.getInput().holdKey(options -> options.keyShift);
		context.waitTicks(3);
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(10);
		context.getInput().releaseKey(options -> options.keyShift);
		int remaining = followers(server).size();
		boolean hasFigurine = server.computeOnServer(s -> {
			var inventory = s.getPlayerList().getPlayers().getFirst().getInventory();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (inventory.getItem(i).is(ModItems.LITTLE_VILLAGER_FIGURINE)) {
					return true;
				}
			}
			return false;
		});
		if (remaining != 1 || !hasFigurine) {
			throw new AssertionError("Sneak-right-click should pack the villager away (followers left: " + remaining + ", has figurine: " + hasFigurine + ")");
		}
		context.takeScreenshot("11_villager_packed_away");
	}
}
