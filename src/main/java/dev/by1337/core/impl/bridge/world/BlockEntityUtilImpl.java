package dev.by1337.core.impl.bridge.world;

import dev.by1337.core.bridge.world.BlockEntityUtil;
import dev.by1337.core.impl.bridge.NMSUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

public class BlockEntityUtilImpl implements BlockEntityUtil {

    @Override
    public void setBlock(Location location, int id, byte @Nullable [] bytes, boolean applyPhysics) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockPos pos = cb.getPosition();
        ServerLevel level = cb.getCraftWorld().getHandle();
        var state = Block.REGISTRY_ID.fromId(id);
        boolean ignored = cb.setTypeAndData(state, applyPhysics);
        if (bytes != null && state == cb.getNMS()) {
            BlockEntity entity = level.getTileEntity(pos, false);
            if (entity != null) {
                entity.load(cb.getNMS(), NMSUtil.fromByteArray(bytes));
                //CraftBlockEntityState#copyData через load грузит не создавая новый BlockEntity
                if (!pos.equals(entity.getPosition())) {
                    entity.setPosition(pos);
                }
            }
        }
    }

    @Override
    public BlockInfo getBlock(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        int blockId = Block.REGISTRY_ID.getId(cb.getNMS());
        byte[] entity = getBlockEntity(cb);
        return new BlockInfo(cb.getBlockData(), blockId, entity);
    }

    private byte @Nullable [] getBlockEntity(CraftBlock cb) {
        BlockEntity entity = cb.getCraftWorld().getHandle().getTileEntity(cb.getPosition());
        if (entity == null) return null;
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        entity.save(tag);
        return NMSUtil.toByteArray(tag);
    }

    @Override
    public void tryClear(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockEntity entity = cb.getCraftWorld().getHandle().getTileEntity(cb.getPosition());
        Clearable.tryClear(entity);
    }
}
