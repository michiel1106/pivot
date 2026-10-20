package bikerboys.pivot.mixin.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl.class)
public class ClientCommonNetworkHandler {

    @Inject(method = "handleDisconnect", at = @At("HEAD"))
    private void handleDisconnect(ClientboundDisconnectPacket packet, CallbackInfo ci) {
        Minecraft instance = Minecraft.getInstance();

        if (instance != null) {
            if (instance.player != null) {
                ClientLevel clientWorld = instance.level;
                if (clientWorld != null) {
                    clientWorld.entitiesForRendering().forEach((entity -> {
                        if (entity instanceof Display displayEntity) {
                            displayEntity.setGlowingTag(false);
                        }

                    }));
                }
            }
        }

    }

}
