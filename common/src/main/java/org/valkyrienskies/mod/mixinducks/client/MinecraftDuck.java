package org.valkyrienskies.mod.mixinducks.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.HitResult;
import org.valkyrienskies.mod.common.IShipObjectWorldClientProvider;

public interface MinecraftDuck extends IShipObjectWorldClientProvider {

    void vs$setOriginalCrosshairTarget(HitResult h);

    HitResult vs$getOriginalCrosshairTarget();
    void setLevelRenderer(LevelRenderer renderer);

}
