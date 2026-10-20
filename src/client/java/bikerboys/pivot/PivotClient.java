package bikerboys.pivot;

import net.minecraft.server.permissions.Permissions;
import bikerboys.pivot.attachmenttype.UUIDCustomAttachedData;
import bikerboys.pivot.networking.packets.SetSelectedBlockDisplayEntityS2C;
import bikerboys.pivot.screen.BlockDisplayEntityEditScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PivotClient implements ClientModInitializer {
	private static KeyMapping keyBinding;
	public static List<UUID> DisplayEntitiesInWorld = new ArrayList<>();
	public static BlockDisplayEntityEditScreen currentscreen = null;




	public static int currentIndex = 0;
	public static boolean glowing = true;
	public static boolean advancedmode = false;


	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(Scheduler::tick);

		keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.pivot.openeditscreen", // The translation key of the keybinding's name
				InputConstants.KEY_R, // The keycode of the key
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pivot", "pivot")) // The category the keybinding is listed under.
		));




		ClientTickEvents.END_CLIENT_TICK.register(PivotClient::Tick);


		ClientPlayNetworking.registerGlobalReceiver(SetSelectedBlockDisplayEntityS2C.ID, ((payload, context) -> {




			Scheduler.runLater(() -> {

				if (currentscreen != null) {
					currentscreen.setTick();
				}


			String uuid = payload.uuid();


			UUID selectedBlockDisplayEntity = UUID.fromString(uuid);

			int i = DisplayEntitiesInWorld.indexOf(selectedBlockDisplayEntity);


			if (i != -1) {
				currentIndex = i;

			}

			}, 2);


		}));


	}







	private static void Tick(Minecraft minecraftClient) {
		if (keyBinding.consumeClick()) {

			BlockDisplayEntityEditScreen blockDisplayEntityEditScreen = new BlockDisplayEntityEditScreen();
			currentscreen = blockDisplayEntityEditScreen;

			minecraftClient.setScreenAndShow(blockDisplayEntityEditScreen);
		}

		if (minecraftClient.player != null) {

			// Clear list first (or just re-create a new one)
			DisplayEntitiesInWorld.clear();

			minecraftClient.level.entitiesForRendering().forEach((entity -> {
				if (entity instanceof Display blockDisplayEntity) {
					if (minecraftClient.player.distanceTo(blockDisplayEntity) <= 150) {

						UUIDCustomAttachedData attachedOrCreate = blockDisplayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);




						if (attachedOrCreate.uuid().equals(minecraftClient.player.getStringUUID()) || attachedOrCreate.uuid().equals("")) {
							DisplayEntitiesInWorld.add(blockDisplayEntity.getUUID());
						} else if (Minecraft.getInstance().player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
							DisplayEntitiesInWorld.add(blockDisplayEntity.getUUID());
						}
					}
				}
			}));

			// Clamp currentIndex to valid range or reset to 0 if empty
			if (DisplayEntitiesInWorld.isEmpty()) {
				currentIndex = 0;
			} else if (currentIndex >= DisplayEntitiesInWorld.size()) {
				currentIndex = DisplayEntitiesInWorld.size() - 1;
			}
		}
	}



	public static Display getDisplayEntity(UUID uuid) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null) {
			ClientLevel world = Minecraft.getInstance().level;
			if (world != null) {
				Entity entity = world.getEntity(uuid);

				if (entity instanceof Display displayEntity) {
					return displayEntity;
				}
			}
		}
		return null;

	}
}