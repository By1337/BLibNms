package dev.by1337.core.impl.util.world;

import dev.by1337.core.impl.util.NBTUtil;
import dev.by1337.core.util.world.BlockEntityUtil;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class BlockEntityUtilImpl implements BlockEntityUtil {
    @Override
    public void setBlock(Location location, int id, byte @Nullable [] bytes, boolean applyPhysics) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        if (cb.setTypeAndData(Block.REGISTRY_ID.fromId(id), applyPhysics)) {
            BlockEntity entity = cb.getCraftWorld().getHandle().getTileEntity(cb.getPosition());
            if (entity != null && bytes != null) {
                entity.load(cb.getNMS(), NBTUtil.fromByteArray(bytes));
                if (!entity.getPosition().equals(cb.getPosition())) {
                    entity.setPosition(cb.getPosition());
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
        return NBTUtil.toByteArray(tag);
    }

    @Override
    public void tryClear(Location location) {
        CraftBlock cb = (CraftBlock) location.getBlock();
        BlockEntity entity = cb.getCraftWorld().getHandle().getTileEntity(cb.getPosition());
        Clearable.tryClear(entity);
    }
}
