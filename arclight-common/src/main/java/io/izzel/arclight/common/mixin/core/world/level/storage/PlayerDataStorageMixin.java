package io.izzel.arclight.common.mixin.core.world.level.storage;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.storage.PlayerDataStorageBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Optional;

@Mixin(PlayerDataStorage.class)
public abstract class PlayerDataStorageMixin implements PlayerDataStorageBridge {

    // @formatter:off
    @Shadow @Final private File playerDir;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract Optional<CompoundTag> load(NameAndId nameAndId);
    // @formatter:on

    /**
     * @author IzzelAliz
     * @reason Spigot offline-mode UUID fallback for player.dat files
     */
    @Overwrite
    private Optional<CompoundTag> load(NameAndId nameAndId, String suffix) {
        File file = this.playerDir;
        String s1 = String.valueOf(nameAndId.id());
        File file1 = new File(file, s1 + suffix);
        // Spigot Start
        boolean usingWrongFile = false;
        if (!file1.exists()) {
            file1 = new File(file, NameAndId.createOffline(nameAndId.name()).id().toString() + suffix);
            if (file1.exists()) {
                usingWrongFile = true;
                org.bukkit.Bukkit.getServer().getLogger().warning("Using offline mode UUID file for player " + nameAndId.name() + " as it is the only copy we can find.");
            }
        }
        // Spigot End

        if (file1.exists() && file1.isFile()) {
            try {
                // Spigot Start
                Optional<CompoundTag> optional = Optional.of(NbtIo.readCompressed(file1.toPath(), NbtAccounter.unlimitedHeap()));
                if (usingWrongFile) {
                    file1.renameTo(new File(file1.getPath() + ".offline-read"));
                }
                return optional;
                // Spigot End
            } catch (Exception exception) {
                LOGGER.warn("Failed to load player data for {}", nameAndId.name());
            }
        }

        return Optional.empty();
    }

    /**
     * @author IzzelAliz
     * @reason Use FileNameDateFormatter (moved out of PlayerDataStorage in 26.1)
     */
    @Overwrite
    private void backup(NameAndId nameAndId, String suffix) {
        Path path = this.playerDir.toPath();
        String s1 = nameAndId.id().toString();
        Path path1 = path.resolve(s1 + suffix);
        Path path2 = path.resolve(s1 + "_corrupted_" + ZonedDateTime.now().format(FileNameDateFormatter.FORMATTER) + suffix);

        if (Files.isRegularFile(path1, new LinkOption[0])) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                LOGGER.warn("Failed to copy the player.dat file for {}", nameAndId.name(), exception);
            }
        }
    }

    // CraftBukkit start
    public Optional<CompoundTag> load(Player player) {
        return this.load(player.nameAndId()).map((compoundtag) -> {
            if (player instanceof ServerPlayer) {
                CraftPlayer craftPlayer = (CraftPlayer) ((EntityBridge) player).bridge$getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDir, player.getStringUUID() + ".dat").lastModified();
                if (modified < craftPlayer.getFirstPlayed()) {
                    craftPlayer.setFirstPlayed(modified);
                }
            }
            return compoundtag;
        });
    }
    // CraftBukkit end

    public CompoundTag getPlayerData(String uuid) {
        try {
            final File file1 = new File(this.playerDir, uuid + ".dat");
            if (file1.exists()) {
                return NbtIo.readCompressed(new FileInputStream(file1), NbtAccounter.unlimitedHeap());
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load player data for " + uuid);
        }
        return null;
    }

    @Override
    public File bridge$getPlayerDir() {
        return this.playerDir;
    }

    @Override
    public CompoundTag bridge$getPlayerData(String uuid) {
        return getPlayerData(uuid);
    }
}