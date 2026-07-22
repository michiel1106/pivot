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
	public static List<UUID> DisplayEntitiesInWorld = new ArrayList<>();
	public static BlockDisplayEntityEditScreen currentscreen = null;
	public static boolean hasPivotPermission = false;




	public static int currentIndex = 0;
	public static boolean glowing = true;
	public static boolean advancedmode = false;


	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(Scheduler::tick);

		KeyBinding kb = null;
		try {
			java.lang.reflect.Constructor<KeyBinding> ctor1 = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, String.class);
			kb = ctor1.newInstance("key.pivot.openeditscreen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.pivot.pivot");
		} catch (Exception e) {
			try {
				Class<?> categoryClass = null;
				try {
					categoryClass = Class.forName("net.minecraft.client.option.KeyBinding$Category");
				} catch (ClassNotFoundException e1) {
					categoryClass = Class.forName("net.minecraft.class_304$class_11900");
				}

				java.lang.reflect.Method createMethod = null;
				try {
					createMethod = categoryClass.getMethod("create", net.minecraft.util.Identifier.class);
				} catch (NoSuchMethodException e2) {
					createMethod = categoryClass.getMethod("method_74698", net.minecraft.util.Identifier.class);
				}

				Object categoryObj = createMethod.invoke(null, net.minecraft.util.Identifier.of("pivot", "pivot"));
				java.lang.reflect.Constructor<KeyBinding> ctor2 = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, categoryClass);
				kb = ctor2.newInstance("key.pivot.openeditscreen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, categoryObj);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to construct KeyBinding via reflection", ex);
			}
		}
		keyBinding = KeyBindingHelper.registerKeyBinding(kb);

		ClientTickEvents.END_CLIENT_TICK.register(PivotClient::Tick);

		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			hasPivotPermission = false;
		});

		ClientPlayNetworking.registerGlobalReceiver(bikerboys.pivot.networking.packets.PivotAuthPacketS2C.ID, (payload, context) -> {
			hasPivotPermission = payload.authorized();
		});

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







	private static void Tick(MinecraftClient minecraftClient) {
		if (keyBinding.wasPressed()) {

			BlockDisplayEntityEditScreen blockDisplayEntityEditScreen = new BlockDisplayEntityEditScreen();
			currentscreen = blockDisplayEntityEditScreen;

			minecraftClient.setScreen(blockDisplayEntityEditScreen);
		}

		if (minecraftClient.player != null) {

			// Clear list first (or just re-create a new one)
			DisplayEntitiesInWorld.clear();

			minecraftClient.world.getEntities().forEach((entity -> {
				if (entity instanceof DisplayEntity blockDisplayEntity) {
					if (minecraftClient.player.distanceTo(blockDisplayEntity) <= 150) {

						UUIDCustomAttachedData attachedOrCreate = blockDisplayEntity.getAttachedOrCreate(Pivot.UUID_ATTACHMENT);




						if (attachedOrCreate.uuid().equals(minecraftClient.player.getUuidAsString()) || attachedOrCreate.uuid().equals("")) {
							DisplayEntitiesInWorld.add(blockDisplayEntity.getUuid());
						} else if (hasPivotPermission || net.minecraft.server.command.CommandManager.GAMEMASTERS_CHECK.allows(MinecraftClient.getInstance().player.getPermissions())) {
							DisplayEntitiesInWorld.add(blockDisplayEntity.getUuid());
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



	public static DisplayEntity getDisplayEntity(UUID uuid) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null) {
			ClientWorld world = MinecraftClient.getInstance().world;
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