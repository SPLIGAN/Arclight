package io.izzel.arclight.common.mixin.core.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.level.saveddata.maps.MapItemSavedDataBridge;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin implements MapItemSavedDataBridge {

    // @formatter:off
    @Shadow @Final @Mutable public static Codec<MapItemSavedData> CODEC;
    @Shadow @Final public ResourceKey<Level> dimension;
    @Shadow @Final private boolean trackingPosition;
    @Shadow @Final private boolean unlimitedTracking;
    @Shadow @Final private Map<String, MapFrame> frameMarkers;
    @Shadow @Final private List<MapItemSavedData.HoldingPlayer> carriedBy;
    // @formatter:on

    public CraftMapView mapView;
    public UUID uniqueId;
    public MapId id;

    @Invoker("<init>")
    static MapItemSavedData arclight$construct(ResourceKey<Level> dimension, int centerX, int centerZ, byte scale, ByteBuffer colors, boolean trackingPosition, boolean unlimitedTracking, boolean locked, List<MapBanner> banners, List<MapFrame> frames) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void arclight$replaceCodec(CallbackInfo ci) {
        // CraftBukkit: persist world UUID alongside dimension key (maps survive custom dimension renames).
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(data -> data.dimension),
            Codec.INT.fieldOf("xCenter").forGetter(data -> data.centerX),
            Codec.INT.fieldOf("zCenter").forGetter(data -> data.centerZ),
            Codec.BYTE.optionalFieldOf("scale", (byte) 0).forGetter(data -> data.scale),
            Codec.BYTE_BUFFER.fieldOf("colors").forGetter(data -> ByteBuffer.wrap(data.colors)),
            Codec.BOOL.optionalFieldOf("trackingPosition", true).forGetter(data -> ((MapItemSavedDataMixin) (Object) data).trackingPosition),
            Codec.BOOL.optionalFieldOf("unlimitedTracking", false).forGetter(data -> ((MapItemSavedDataMixin) (Object) data).unlimitedTracking),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(data -> data.locked),
            MapBanner.CODEC.listOf().optionalFieldOf("banners", List.of()).forGetter(data -> List.copyOf(data.getBanners())),
            MapFrame.CODEC.listOf().optionalFieldOf("frames", List.of()).forGetter(data -> List.copyOf(((MapItemSavedDataMixin) (Object) data).frameMarkers.values())),
            Codec.LONG.optionalFieldOf("UUIDLeast", 0L).forGetter(MapItemSavedDataMixin::arclight$uuidLeast),
            Codec.LONG.optionalFieldOf("UUIDMost", 0L).forGetter(MapItemSavedDataMixin::arclight$uuidMost)
        ).apply(instance, MapItemSavedDataMixin::arclight$fromCodec));
    }

    @Inject(method = "<init>(IIBZZZLnet/minecraft/resources/ResourceKey;)V", at = @At("RETURN"))
    public void arclight$init(int centerX, int centerZ, byte scale, boolean trackingPosition, boolean unlimitedTracking, boolean locked, ResourceKey<Level> dimension, CallbackInfo ci) {
        this.updateUUID();
        this.mapView = new CraftMapView((MapItemSavedData) (Object) this);
    }

    private static MapItemSavedData arclight$fromCodec(ResourceKey<Level> dimension, int centerX, int centerZ, byte scale, ByteBuffer colors, boolean trackingPosition, boolean unlimitedTracking, boolean locked, List<MapBanner> banners, List<MapFrame> frames, long uuidLeast, long uuidMost) {
        return arclight$construct(getWorldKey(dimension, uuidLeast, uuidMost), centerX, centerZ, scale, colors, trackingPosition, unlimitedTracking, locked, banners, frames);
    }

    private static MinecraftServer arclight$server() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    private static ResourceKey<Level> getWorldKey(ResourceKey<Level> resourceKey, long uuidLeast, long uuidMost) {
        Level lookup = arclight$server().getLevel(resourceKey);
        if (lookup != null) {
            return resourceKey;
        }

        if (uuidLeast != 0L && uuidMost != 0L) {
            UUID uniqueId = new UUID(uuidMost, uuidLeast);
            CraftWorld world = (CraftWorld) Bukkit.getWorld(uniqueId);
            if (world != null) {
                return world.getHandle().dimension();
            }
        }
        throw new IllegalArgumentException("Invalid map dimension: " + resourceKey);
    }

    @Nullable
    public UUID updateUUID() {
        if (this.uniqueId == null) {
            Level level = arclight$server().getLevel(this.dimension);
            if (level != null) {
                this.uniqueId = ((WorldBridge) level).bridge$getWorld().getUID();
            }
        }
        return this.uniqueId;
    }

    private static long arclight$uuidLeast(MapItemSavedData data) {
        UUID uuid = ((MapItemSavedDataMixin) (Object) data).updateUUID();
        return uuid != null ? uuid.getLeastSignificantBits() : 0L;
    }

    private static long arclight$uuidMost(MapItemSavedData data) {
        UUID uuid = ((MapItemSavedDataMixin) (Object) data).updateUUID();
        return uuid != null ? uuid.getMostSignificantBits() : 0L;
    }

    @Override
    public void bridge$setId(MapId id) {
        this.id = id;
    }

    @Override
    public List<MapItemSavedData.HoldingPlayer> bridge$getCarriedBy() {
        return this.carriedBy;
    }

    @Override
    public CraftMapView bridge$getMapView() {
        return mapView;
    }
}
