package io.izzel.arclight.common.mixin.core.world.level.portal;

import io.izzel.arclight.common.bridge.core.world.level.LevelAccessorBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.level.portal.PortalShapeBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.portal.PortalShape;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin implements PortalShapeBridge {

    // @formatter:off
    @Shadow @Final private Direction.Axis axis;
    @Shadow @Final private Direction rightDir;
    @Shadow @Final private BlockPos bottomLeft;
    @Shadow @Final private int height;
    @Shadow @Final private int width;
    @Shadow public abstract void createPortalBlocks(LevelAccessor level);
    // @formatter:on

    // 26.1.2: PortalShape no longer stores LevelAccessor; frame capture is threaded via findAnyShape.
    @Unique private BlockStateListPopulator arclight$blocks;
    @Unique private static final ThreadLocal<BlockStateListPopulator> arclight$capture = new ThreadLocal<>();
    @Unique private transient boolean arclight$ret = true;

    @Inject(method = "findAnyShape", at = @At("HEAD"))
    private static void arclight$beginCapture(BlockGetter level, BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<PortalShape> cir) {
        if (!(level instanceof LevelAccessor accessor)) {
            return;
        }
        ServerLevel serverLevel = null;
        LevelAccessorBridge bridge = LevelAccessorBridge.from(accessor);
        if (bridge != null) {
            serverLevel = bridge.bridge$getMinecraftWorld();
        } else if (accessor instanceof ServerLevel sl) {
            serverLevel = sl;
        }
        if (serverLevel != null) {
            arclight$capture.set(new BlockStateListPopulator(serverLevel));
        }
    }

    @Inject(method = "findAnyShape", at = @At("RETURN"))
    private static void arclight$endCapture(BlockGetter level, BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<PortalShape> cir) {
        BlockStateListPopulator blocks = arclight$capture.get();
        arclight$capture.remove();
        PortalShape shape = cir.getReturnValue();
        if (blocks != null && shape != null) {
            ((PortalShapeMixin) (Object) shape).arclight$blocks = blocks;
        }
    }

    @Decorate(method = "getDistanceUntilEdgeAboveFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean arclight$captureBlock(BlockBehaviour.StatePredicate predicate, net.minecraft.world.level.block.state.BlockState state, BlockGetter getter, BlockPos pos) throws Throwable {
        boolean test = (boolean) DecorationOps.callsite().invoke(predicate, state, getter, pos);
        if (test) {
            BlockStateListPopulator blocks = arclight$capture.get();
            if (blocks != null) {
                blocks.setBlock(pos, getter.getBlockState(pos), 18);
            }
        }
        return test;
    }

    @Decorate(method = "hasTopFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean arclight$captureBlock2(BlockBehaviour.StatePredicate predicate, net.minecraft.world.level.block.state.BlockState state, BlockGetter getter, BlockPos pos) throws Throwable {
        boolean test = (boolean) DecorationOps.callsite().invoke(predicate, state, getter, pos);
        if (test) {
            BlockStateListPopulator blocks = arclight$capture.get();
            if (blocks != null) {
                blocks.setBlock(pos, getter.getBlockState(pos), 18);
            }
        }
        return test;
    }

    // Avoid @Decorate + @Local here: Decorator.prepareLvtMapping NPEs when methodNode.parameters is null
    // (common on static targets). At ordinal=1 FRAME.test, pos is already bottomLeft.up(j).relative(rightDir, width).
    @Decorate(method = "getDistanceUntilTop", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean arclight$captureBlock3(BlockBehaviour.StatePredicate predicate, net.minecraft.world.level.block.state.BlockState state, BlockGetter getter, BlockPos pos,
                                                  BlockGetter level, BlockPos bottomLeft, Direction rightDir, BlockPos.MutableBlockPos mutablePos, int width,
                                                  org.apache.commons.lang3.mutable.MutableInt portalBlockCount) throws Throwable {
        boolean test = (boolean) DecorationOps.callsite().invoke(predicate, state, getter, pos);
        if (test) {
            BlockStateListPopulator blocks = arclight$capture.get();
            if (blocks != null) {
                int j = pos.getY() - bottomLeft.getY();
                blocks.setBlock(mutablePos.set(bottomLeft).move(Direction.UP, j).move(rightDir, -1), level.getBlockState(mutablePos), 18);
                blocks.setBlock(mutablePos.set(bottomLeft).move(Direction.UP, j).move(rightDir, width), level.getBlockState(mutablePos), 18);
            }
        }
        return test;
    }

    @Inject(method = "createPortalBlocks(Lnet/minecraft/world/level/LevelAccessor;)V", cancellable = true, at = @At("HEAD"))
    private void arclight$buildPortal(LevelAccessor level, CallbackInfo ci) {
        LevelAccessorBridge accessor = LevelAccessorBridge.from(level);
        if (accessor == null) {
            return;
        }
        ServerLevel serverLevel = accessor.bridge$getMinecraftWorld();
        World world = ((WorldBridge) serverLevel).bridge$getWorld();
        if (this.arclight$blocks == null) {
            this.arclight$blocks = new BlockStateListPopulator(serverLevel);
        }
        BlockStateListPopulator blocks = this.arclight$blocks;
        net.minecraft.world.level.block.state.BlockState blockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((blockPos) -> {
            blocks.setBlock(blockPos, blockState, 18);
        });
        PortalCreateEvent event = new PortalCreateEvent((java.util.List<org.bukkit.block.BlockState>) (java.util.List) blocks.getList(), world, null, PortalCreateEvent.CreateReason.FIRE);
        Bukkit.getPluginManager().callEvent(event);
        this.arclight$ret = !event.isCancelled();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public boolean bridge$createPortal(LevelAccessor level) {
        this.createPortalBlocks(level);
        return this.arclight$ret;
    }
}
