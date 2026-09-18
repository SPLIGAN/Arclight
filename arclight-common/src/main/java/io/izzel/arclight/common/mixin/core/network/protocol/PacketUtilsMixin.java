package io.izzel.arclight.common.mixin.core.network.protocol;

import io.izzel.arclight.common.bridge.core.server.network.ServerCommonPacketListenerImplBridge;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PacketUtils.class)
public abstract class PacketUtilsMixin {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow public static <T extends PacketListener> ReportedException makeReportedException(Exception exception, Packet<T> packet, T packetListener) { throw new RuntimeException(); }
    // @formatter:on

    /**
     * @author IzzelAliz
     * @reason Skip scheduling after Bukkit disconnect; 26.1 replaced BlockableEventLoop with PacketProcessor.
     */
    @Overwrite
    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packetIn, T processor, PacketProcessor executor) throws RunningOnDifferentThreadException {
        if (!executor.isSameThread()) {
            if (processor instanceof ServerCommonPacketListenerImpl && ((ServerCommonPacketListenerImplBridge) processor).bridge$processedDisconnect()) {
                throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
            }
            executor.scheduleIfPossible(processor, packetIn);
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
}
