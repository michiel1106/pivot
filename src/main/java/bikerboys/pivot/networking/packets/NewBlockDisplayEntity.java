package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record NewBlockDisplayEntity(String string) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "new_block_display");
    public static final CustomPayload.Id<NewBlockDisplayEntity> ID = new CustomPayload.Id<>(NewBlockDisplayEntity.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, NewBlockDisplayEntity> CODEC = PacketCodec.tuple(PacketCodecs.STRING, NewBlockDisplayEntity::string, NewBlockDisplayEntity::new);


    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
