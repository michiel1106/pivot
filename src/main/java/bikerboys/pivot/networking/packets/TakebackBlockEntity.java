package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record TakebackBlockEntity(String uuid) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "take_back_display_entity");
    public static final Id<TakebackBlockEntity> ID = new Id<>(TakebackBlockEntity.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, TakebackBlockEntity> CODEC = PacketCodec.tuple(PacketCodecs.STRING, TakebackBlockEntity::uuid, TakebackBlockEntity::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
