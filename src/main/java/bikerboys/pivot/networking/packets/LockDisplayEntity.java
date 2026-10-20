package bikerboys.pivot.networking.packets;

import static bikerboys.pivot.Pivot.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LockDisplayEntity(String uuid) implements CustomPacketPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "lock_display_entity");
    public static final Type<LockDisplayEntity> ID = new Type<>(LockDisplayEntity.UPDATE_BLOCK_DISPLAY_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockDisplayEntity> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, LockDisplayEntity::uuid, LockDisplayEntity::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
