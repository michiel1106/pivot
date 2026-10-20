package bikerboys.pivot.attachmenttype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UUIDCustomAttachedData(String uuid) {

    public static Codec<UUIDCustomAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("string").forGetter(UUIDCustomAttachedData::uuid) // our object just has a list of floats
    ).apply(instance, UUIDCustomAttachedData::new));
    public static StreamCodec<ByteBuf, UUIDCustomAttachedData> PACKET_CODEC = ByteBufCodecs.fromCodec(CODEC);

    // A default value we can use as an "empty" or reset data component
    // it uses List.of() which creates an empty, immutable list.
    public static UUIDCustomAttachedData DEFAULT = new UUIDCustomAttachedData("");

    public UUIDCustomAttachedData setUuid(String uuid) {

        return new UUIDCustomAttachedData(uuid);
    }




    public UUIDCustomAttachedData clear() { // clear method, just returns the default empty component
        return DEFAULT;
    }
}
