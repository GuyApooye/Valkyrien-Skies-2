package org.valkyrienskies.mod.mixin.client.multiplayer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.IShipObjectWorldClientCreator;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.world.ShipDimension;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Shadow
    private ClientLevel level;

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            shift = Shift.AFTER
        ),
        method = "handleLogin"
    )
    private void beforeHandleLogin(final ClientboundLoginPacket packet, final CallbackInfo ci) {
        ((IShipObjectWorldClientCreator) Minecraft.getInstance()).createShipObjectWorldClient();

    }
    @Redirect(method = "handleRespawn", at = @At(value = "NEW",
        target = "(Lnet/minecraft/client/multiplayer/ClientPacketListener;Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/Holder;IILjava/util/function/Supplier;Lnet/minecraft/client/renderer/LevelRenderer;ZJ)Lnet/minecraft/client/multiplayer/ClientLevel;"))
    private ClientLevel afterHandleLogin(ClientPacketListener arg, ClientLevelData arg2, ResourceKey<Level> arg3, Holder<DimensionType> arg4,
        int i, int j, Supplier<ProfilerFiller> supplier, LevelRenderer arg5, boolean bl, long l) {
//        ShipWorldRenderer.INSTANCE.init();
        Holder<DimensionType> type = arg.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
            .getHolderOrThrow(ShipDimension.getShipDimensionTypeKey());
        ShipDimension.INSTANCE.setShipDimensionLevel(new ClientLevel(arg,arg2,ShipDimension.getShipDimensionKey(),type,i,j,supplier,arg5,bl,l));

        return new ClientLevel(arg,arg2,arg3,arg4,i,j,supplier,arg5,bl,l);
    }

    /**
     * Spawn [ShipMountingEntity] on client side
     */
    @Inject(method = "handleAddEntity",
        at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V"),
        cancellable = true)
    private void handleShipMountingEntity(final ClientboundAddEntityPacket packet, final CallbackInfo ci) {
        if (packet.getType().equals(ValkyrienSkiesMod.SHIP_MOUNTING_ENTITY_TYPE)) {
            ci.cancel();
            final double d = packet.getX();
            final double e = packet.getY();
            final double f = packet.getZ();
            final Entity entity = ValkyrienSkiesMod.SHIP_MOUNTING_ENTITY_TYPE.create(level);
            final int i = packet.getId();
            entity.setPacketCoordinates(d, e, f);
            entity.moveTo(d, e, f);
            entity.setXRot((float) (packet.getxRot() * 360) / 256.0f);
            entity.setYRot((float) (packet.getyRot() * 360) / 256.0f);
            entity.setId(i);
            entity.setUUID(packet.getUUID());
            this.level.putNonPlayerEntity(i, entity);
        }
    }

    /**
     * When mc receives a tp packet it lerps it between 2 positions in 3 steps, this is bad for ships it gets stuck in a
     * unloaded chunk clientside and stays there until rejoining the server.
     */
    @WrapOperation(method = "handleTeleportEntity", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V"))
    private void teleportingWithNoStep(final Entity instance,
        final double x, final double y, final double z,
        final float yRot, final float xRot,
        final int lerpSteps, final boolean teleport, final Operation<Void> lerpTo) {
        if (VSGameUtilsKt.getShipObjectManagingPos(instance.level, instance.getX(), instance.getY(), instance.getZ()) !=
            null) {
            instance.setPos(x, y, z);
            lerpTo.call(instance, x, y, z, yRot, xRot, 1, teleport);
        } else {
            lerpTo.call(instance, x, y, z, yRot, xRot, lerpSteps, teleport);
        }
    }
}
