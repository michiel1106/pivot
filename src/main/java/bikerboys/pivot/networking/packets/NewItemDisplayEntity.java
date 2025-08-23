package bikerboys.pivot.networking.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static bikerboys.pivot.Pivot.MOD_ID;

public record NewItemDisplayEntity(String string) implements CustomPayload {
    public static final Identifier UPDATE_BLOCK_DISPLAY_ID = Identifier.of(MOD_ID, "new_item_display");
    public static final Id<NewItemDisplayEntity> ID = new Id<>(NewItemDisplayEntity.UPDATE_BLOCK_DISPLAY_ID);
    public static final PacketCodec<RegistryByteBuf, NewItemDisplayEntity> CODEC = PacketCodec.tuple(PacketCodecs.STRING, NewItemDisplayEntity::string, NewItemDisplayEntity::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
