package bikerboys.pivot;

import bikerboys.pivot.attachmenttype.UUIDCustomAttachedData;
import bikerboys.pivot.networking.packets.SetSelectedBlockDisplayEntityS2C;
import bikerboys.pivot.screen.BlockDisplayEntityEditScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PivotClient implements ClientModInitializer {
	private static KeyBinding keyBinding;
	public static List<UUID> BlockEntitiesInWorld = new ArrayList<>();



	public static int currentIndex = 0;
	public static boolean glowing = true;
	public static boolean advancedmode = false;


	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(Scheduler::tick);

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.pivot.openeditscreen", // The translation key of the keybinding's name
				InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"category.pivot.pivot" // The translation key of the keybinding's category.
		));




		ClientTickEvents.END_CLIENT_TICK.register(PivotClient::Tick);


		ClientPlayNetworking.registerGlobalReceiver(SetSelectedBlockDisplayEntityS2C.ID, ((payload, context) -> {

			Scheduler.runLater(() -> {



			String uuid = payload.uuid();


			UUID selectedBlockDisplayEntity = UUID.fromString(uuid);

			int i = BlockEntitiesInWorld.indexOf(selectedBlockDisplayEntity);


			if (i != -1) {
				currentIndex = i;
			}
			System.out.println(i);

			}, 2);


		}));


	}







	private static void Tick(MinecraftClient minecraftClient) {
		if (keyBinding.wasPressed()) {
			minecraftClient.setScreen(new BlockDisplayEntityEditScreen());
		}

		if (minecraftClient.player != null) {

			// Clear list first (or just re-create a new one)
			BlockEntitiesInWorld.clear();

			minecraftClient.player.clientWorld.getEntities().forEach((entity -> {
				if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
					if (minecraftClient.player.distanceTo(blockDisplayEntity) <= 150) {

						UUIDCustomAttachedData attachedOrCreate = blockDisplayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);



						if (attachedOrCreate.uuid().equals(minecraftClient.player.getUuidAsString()) || attachedOrCreate.uuid().equals("")) {
							BlockEntitiesInWorld.add(blockDisplayEntity.getUuid());
						}
					}
				}
			}));

			// Clamp currentIndex to valid range or reset to 0 if empty
			if (BlockEntitiesInWorld.isEmpty()) {
				currentIndex = 0;
			} else if (currentIndex >= BlockEntitiesInWorld.size()) {
				currentIndex = BlockEntitiesInWorld.size() - 1;
			}
		}
	}



	public static DisplayEntity getDisplayEntity(UUID uuid) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null) {
			ClientWorld world = player.clientWorld;
			if (world != null) {
				Entity entity = world.getEntity(uuid);

				if (entity instanceof DisplayEntity displayEntity) {
					return displayEntity;
				}
			}
		}
		return null;

	}
}