package bikerboys.pivot;

import bikerboys.pivot.networking.Listener;
import bikerboys.pivot.networking.packets.*;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Pivot implements ModInitializer {
	public static final String MOD_ID = "pivot";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {


		PayloadTypeRegistry.playC2S().register(UpdateBlockDisplayPacketC2S.ID, UpdateBlockDisplayPacketC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(SetSelectedBlockDisplayEntityS2C.ID, SetSelectedBlockDisplayEntityS2C.CODEC);
		PayloadTypeRegistry.playC2S().register(AlignXYZ.ID, AlignXYZ.CODEC);
		PayloadTypeRegistry.playC2S().register(NewBlockDisplayEntity.ID, NewBlockDisplayEntity.CODEC);
		PayloadTypeRegistry.playC2S().register(TakebackBlockEntity.ID, TakebackBlockEntity.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdateBlockDisplayPos.ID, UpdateBlockDisplayPos.CODEC);
		PayloadTypeRegistry.playC2S().register(DuplicateBlockDisplay.ID, DuplicateBlockDisplay.CODEC);


		Listener.registerListeners();



	}


}