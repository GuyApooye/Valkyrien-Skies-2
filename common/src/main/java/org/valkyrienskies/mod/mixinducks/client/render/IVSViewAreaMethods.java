package org.valkyrienskies.mod.mixinducks.client.render;

import net.minecraft.client.renderer.ViewArea;

public interface IVSViewAreaMethods {
    void unloadChunk(int chunkX, int chunkZ);
    ViewArea newWithShipDispatcher();
}
