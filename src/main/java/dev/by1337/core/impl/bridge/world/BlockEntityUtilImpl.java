package dev.by1337.core.impl.bridge.world;

import dev.by1337.core.bridge.world.BlockEntityUtil;
import dev.by1337.core.impl.bridge.NMSUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

public class BlockEntityUtilImpl implements BlockEntityUtil {

    @Override
    public void setBlock(Location location, int id, byte @Nullable [] bytes, boolean applyPhysics) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockPos pos = cb.getPosition();
        ServerLevel level = cb.getCraftWorld().getHandle();
        var state = Block.stateById(id);
        boolean ignored = CraftBlock.setTypeAndData(cb.getHandle(), pos, cb.getNMS(), state, applyPhysics);
        ;
        if (bytes != null && state == cb.getNMS()) {
            BlockEntity entity = level.getBlockEntity(pos, false);
            if (entity != null) {
                //CraftBlockEntityState#copyData через load грузит не создавая новый BlockEntity
                entity.load(NMSUtil.fromByteArray(bytes));
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
        BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
        if (entity == null) return null;
        net.minecraft.nbt.CompoundTag tag = entity.saveWithFullMetadata();
        return NMSUtil.toByteArray(tag);
    }

    @Override
    public void tryClear(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
        Clearable.tryClear(entity);
    }
}
