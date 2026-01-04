package dev.by1337.core.impl.util.world;

import dev.by1337.core.impl.util.NBTUtil;
import dev.by1337.core.util.world.BlockEntityUtil;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

public class BlockEntityUtilImpl implements BlockEntityUtil {
    @Override
    public void setBlock(Location location, int id, byte @Nullable [] bytes, boolean applyPhysics) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        if (cb.setTypeAndData(Block.BLOCK_STATE_REGISTRY.byId(id), applyPhysics)) {
            BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
            if (entity != null && bytes != null) {
                entity.load(NBTUtil.fromByteArray(bytes));
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
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        entity.save(tag);
        return NBTUtil.toByteArray(tag);
    }

    @Override
    public void tryClear(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockEntity entity = cb.getCraftWorld().getHandle().getBlockEntity(cb.getPosition());
        Clearable.tryClear(entity);
    }
}
