package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record UnlockDisplayEntity(String uuid) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "lock_display_entity");
    public static final Id<UnlockDisplayEntity> ID = new Id<>(UnlockDisplayEntity.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, UnlockDisplayEntity> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UnlockDisplayEntity::uuid, UnlockDisplayEntity::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
