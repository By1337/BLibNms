package dev.by1337.core.impl.util.inventory;

import dev.by1337.core.impl.util.NBTUtil;
import dev.by1337.core.util.inventory.ItemStackSerializer;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializerImpl implements ItemStackSerializer {

    @Override
    public byte[] serialize(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack i = asNMSCopy(itemStack);
            CompoundTag itemTag = new CompoundTag();
            i.save(itemTag);
            return NBTUtil.toByteArray(itemTag);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize ItemStack", e);
        }
    }

    @Override
    public ItemStack deserialize(byte[] bytes) {
        try {
            CompoundTag data = NBTUtil.fromByteArray(bytes);
            net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.of(data);
            return CraftItemStack.asCraftMirror(itemStack);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize ItemStack", e);
        }
    }

    private net.minecraft.world.item.ItemStack asNMSCopy(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack c) {
            return c.handle;
        }
        return CraftItemStack.asNMSCopy(itemStack);
    }

}
