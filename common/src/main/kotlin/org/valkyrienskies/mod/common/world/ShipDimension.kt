package org.valkyrienskies.mod.common.world

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexBuffer
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.newSingleThreadContext
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LevelRenderer.RenderChunkInfo
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundLoginPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType
import org.spongepowered.asm.mixin.Unique
import org.valkyrienskies.mod.common.world.ShipDimension.shipDimensionKey
import org.valkyrienskies.mod.common.world.ShipDimension.shipDimensionLevel
import org.valkyrienskies.mod.common.world.ShipDimension.shipDimensionRenderer
import org.valkyrienskies.mod.common.world.ShipDimension.shipDimensionTypeKey
import org.valkyrienskies.mod.mixin.accessors.client.multiplayer.ClientLevelAccessor
import org.valkyrienskies.mod.mixin.accessors.client.multiplayer.ClientLevelDataAccessor
import org.valkyrienskies.mod.mixin.accessors.client.world.level.biome.BiomeManagerAccessor
import java.util.function.Supplier

object ShipDimension {
    @JvmStatic
    val shipDimensionKey : ResourceKey<Level> = ResourceKey.create(Registry.DIMENSION_REGISTRY, ResourceLocation("valkyrienskies","shipworld"))
    @JvmStatic
    val shipDimensionTypeKey : ResourceKey<DimensionType> = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, shipDimensionKey.location())
    @JvmStatic
    val shipDimensionId = "minecraft:dimension:valkyrienskies:shipworld"

    @JvmStatic
    var shipDimensionLevel : ClientLevel? = null
    var shipDimensionRenderer : LevelRenderer? = null

    fun register() {}
}
object ShipWorldRenderer {
    val minecraft: Minecraft = Minecraft.getInstance()
    @Unique
    private fun initializeShipWorldLevel(minecraft: Minecraft,packet: ClientboundLoginPacket ,connection: ClientPacketListener,levelRenderer: LevelRenderer) {
        val level: ClientLevel = minecraft.level!!
        level.profiler.push("vs_create_ship_world")
        val shipWorldRenderer: LevelRenderer = LevelRenderer(minecraft,minecraft.renderBuffers())
        val shipWorldLevel: ClientLevel

        val mainNetHandler = connection
        val mapData = (level as ClientLevelAccessor).mapData

        val currentProperty =
            (minecraft.level as ClientLevelAccessor).clientLevelData
        val registryManager = mainNetHandler.registryAccess()
        val simulationDistance: Int = minecraft.level!!.serverSimulationDistance

        val dimensionTypeKey = shipDimensionTypeKey
        val dimensionType = registryManager
            .registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
            .getHolderOrThrow(dimensionTypeKey)

        val properties = ClientLevelData(
            currentProperty.difficulty,
            currentProperty.isHardcore,
            packet.isFlat
        )
        shipWorldLevel = ClientLevel(
            mainNetHandler,
            properties,
            shipDimensionKey,
            dimensionType,
            minecraft.options.renderDistance,
            simulationDistance,  // seems that client world does not use this
            { minecraft.profiler },
            levelRenderer,
            level.isDebug,
            (level.biomeManager as BiomeManagerAccessor).biomeZoomSeed
        )

        // all worlds share the same map data map
        (shipWorldLevel as ClientLevelAccessor).mapData = mapData
        shipWorldRenderer.setLevel(shipWorldLevel)
        shipWorldRenderer.onResourceManagerReload(minecraft.resourceManager)
        shipDimensionLevel = shipWorldLevel
        shipDimensionRenderer = shipWorldRenderer
        level.profiler.pop()
    }
    private inline fun init(minecraft: Minecraft,packet: ClientboundLoginPacket ,connection: ClientPacketListener,levelRenderer: LevelRenderer) {
        initializeShipWorldLevel(minecraft,packet, connection, levelRenderer)
    }
    inline fun softInit() {
        if (shipDimensionLevel != null) return
        val level: ClientLevel = minecraft.level!!
        level.profiler.push("vs_create_ship_world")
        val shipWorldRenderer = LevelRenderer(minecraft,minecraft.renderBuffers())
        val shipWorldLevel: ClientLevel

        val mainNetHandler = minecraft.connection
        val mapData = (level as ClientLevelAccessor).mapData

        val currentProperty =
            (minecraft.level as ClientLevelAccessor).clientLevelData
        val registryManager = mainNetHandler!!.registryAccess()
        val simulationDistance: Int = minecraft.level!!.serverSimulationDistance

        val dimensionTypeKey = shipDimensionTypeKey
        val dimensionType = registryManager
            .registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
            .getHolderOrThrow(dimensionTypeKey)

        val properties = ClientLevelData(
            currentProperty.difficulty,
            currentProperty.isHardcore,
            (currentProperty as ClientLevelDataAccessor).isFlat
        )
        shipWorldLevel = ClientLevel(
            mainNetHandler,
            properties,
            shipDimensionKey,
            dimensionType,
            minecraft.options.renderDistance,
            simulationDistance,  // seems that client world does not use this
            { minecraft.profiler },
            minecraft.levelRenderer,
            level.isDebug,
            (level.biomeManager as BiomeManagerAccessor).biomeZoomSeed
        )

        // all worlds share the same map data map
        (shipWorldLevel as ClientLevelAccessor).mapData = mapData
        shipWorldRenderer.setLevel(shipWorldLevel)
        shipWorldRenderer.onResourceManagerReload(minecraft.resourceManager)
        shipDimensionLevel = shipWorldLevel
        shipDimensionRenderer = shipWorldRenderer
        level.profiler.pop()
    }
}
