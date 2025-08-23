package bikerboys.pivot;

import bikerboys.pivot.attachmenttype.LockedCustomAttachedData;
import bikerboys.pivot.attachmenttype.UUIDCustomAttachedData;
import bikerboys.pivot.networking.Listener;
import bikerboys.pivot.networking.packets.*;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pivot implements ModInitializer {
	public static final String MOD_ID = "pivot";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final AttachmentType<UUIDCustomAttachedData> UUID_ATTACHMENT = AttachmentRegistry.create(
					Identifier.of(MOD_ID,"uuid"),
					builder->builder // we are using a builder chain here to configure the attachment data type
							.initializer(()->UUIDCustomAttachedData.DEFAULT) // a default value to provide if you dont supply one
							.persistent(UUIDCustomAttachedData.CODEC)
							.syncWith(UUIDCustomAttachedData.PACKET_CODEC, AttachmentSyncPredicate.all()));

	public static final AttachmentType<LockedCustomAttachedData> LOCKED_ATTACHMENT = AttachmentRegistry.create(
			Identifier.of(MOD_ID,"locked"),
			builder->builder // we are using a builder chain here to configure the attachment data type
					.initializer(()->LockedCustomAttachedData.DEFAULT) // a default value to provide if you dont supply one
					.persistent(LockedCustomAttachedData.CODEC)
					.syncWith(LockedCustomAttachedData.PACKET_CODEC, AttachmentSyncPredicate.all()));

	@Override
	public void onInitialize() {


		PayloadTypeRegistry.playC2S().register(UpdateBlockDisplayPacketC2S.ID, UpdateBlockDisplayPacketC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(SetSelectedBlockDisplayEntityS2C.ID, SetSelectedBlockDisplayEntityS2C.CODEC);
		PayloadTypeRegistry.playC2S().register(AlignXYZ.ID, AlignXYZ.CODEC);
		PayloadTypeRegistry.playC2S().register(NewBlockDisplayEntity.ID, NewBlockDisplayEntity.CODEC);
		PayloadTypeRegistry.playC2S().register(NewItemDisplayEntity.ID, NewItemDisplayEntity.CODEC);
		PayloadTypeRegistry.playC2S().register(TakebackBlockEntity.ID, TakebackBlockEntity.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdateBlockDisplayPos.ID, UpdateBlockDisplayPos.CODEC);
		PayloadTypeRegistry.playC2S().register(DuplicateBlockDisplay.ID, DuplicateBlockDisplay.CODEC);
		PayloadTypeRegistry.playC2S().register(LockDisplayEntity.ID, LockDisplayEntity.CODEC);


		Listener.registerListeners();



	}


}