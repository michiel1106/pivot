package bikerboys.pivot.attachmenttype;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record OriginalBlockAttachedData(String blockid) {

    public static Codec<OriginalBlockAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("string").forGetter(OriginalBlockAttachedData::blockid) // our object just has a list of floats
    ).apply(instance, OriginalBlockAttachedData::new));
    public static PacketCodec<ByteBuf, OriginalBlockAttachedData> PACKET_CODEC = PacketCodecs.codec(CODEC);

    // A default value we can use as an "empty" or reset data component
    // it uses List.of() which creates an empty, immutable list.
    public static OriginalBlockAttachedData DEFAULT = new OriginalBlockAttachedData("");

    public OriginalBlockAttachedData setUuid(String uuid) {

        return new OriginalBlockAttachedData(uuid);
    }




    public OriginalBlockAttachedData clear() { // clear method, just returns the default empty component
        return DEFAULT;
    }
}
