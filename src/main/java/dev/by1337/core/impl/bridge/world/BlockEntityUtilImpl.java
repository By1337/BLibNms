package dev.by1337.core.impl.bridge.world;

import dev.by1337.core.bridge.world.BlockEntityUtil;
import dev.by1337.core.impl.bridge.NMSUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BlockEntityUtilImpl implements BlockEntityUtil {

    private static final Logger log = LoggerFactory.getLogger("BLib#BlockEntity");

    @Override
    public void setBlock(Location location, int id, byte @Nullable [] bytes, boolean applyPhysics) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockPos pos = cb.getPosition();
        ServerLevel level = cb.getCraftWorld().getHandle();
        var state = Block.stateById(id);
        boolean ignored = CraftBlock.setBlockState(cb.getHandle(), pos, cb.getNMS(), state, applyPhysics);
        if (bytes != null && state == cb.getNMS()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity != null) {
                //CraftBlockEntityState#copyData через loadWithComponents грузит не создавая новый BlockEntity
                try (ProblemReporter.ScopedCollector problemReporter = new ProblemReporter.ScopedCollector(() -> "BlockEntityUtilImpl@" + pos.toShortString(), log)) {
                    entity.loadWithComponents(TagValueInput.create(problemReporter, level.registryAccess(), NMSUtil.fromByteArray(bytes)));
                }
            }
        }
    }

    @Override
    public BlockInfo getBlock(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        int blockId = Block.BLOCK_STATE_REGISTRY.getId(cb.getNMS());
        byte[] entity = getBlockEntity(cb);
        return new BlockInfo(cb.getBlockData(), blockId, entity);
    }

    private byte @Nullable [] getBlockEntity(CraftBlock cb) {
        var level = cb.getCraftWorld().getHandle();
        BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
        if (entity == null) return null;
        net.minecraft.nbt.CompoundTag tag = entity.saveWithFullMetadata(level.registryAccess());
        return NMSUtil.toByteArray(tag);
    }

    @Override
    public void tryClear(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
        if (entity instanceof Clearable clearable) clearable.clearContent();
    }

    private static final BlockPos.MutableBlockPos cachedPos = new BlockPos.MutableBlockPos();

    @Override
    public void forEachContentAndClear(Location location, Consumer<ItemStack> consumer) {
        var level = ((CraftWorld) location.getWorld()).getHandle();
        cachedPos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        var entity = level.getBlockEntity(cachedPos);
        if (entity == null) return;
        if (!(entity instanceof Container c)) return;
        var list = c.getContents();
        if (list.isEmpty()) return;
        for (var stack : list) {
            consumer.accept(CraftItemStack.asCraftMirror(stack));
        }
        c.clearContent();
    }
}
