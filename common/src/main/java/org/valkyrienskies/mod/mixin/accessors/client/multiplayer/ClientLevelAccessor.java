package org.valkyrienskies.mod.mixin.accessors.client.multiplayer;

import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    @Accessor("levelRenderer")
    LevelRenderer getLevelRenderer();

    @Accessor("clientLevelData")
    ClientLevelData getClientLevelData();

    @Accessor("mapData")
    Map<String, MapItemSavedData> getMapData();

    @Accessor("mapData")
    void setMapData(Map<String, MapItemSavedData> mapData);
}
