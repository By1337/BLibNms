package dev.by1337.core.impl.bridge.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.by1337.core.bridge.inventory.ItemStackSerializer;
import dev.by1337.core.impl.bridge.NMSUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackSerializerImpl implements ItemStackSerializer {

    @Override
    public byte[] serialize(ItemStack itemStack, @Nullable World ignored) {
        try {
            net.minecraft.world.item.ItemStack i = NMSUtil.asNMSItemStack(itemStack);
            CompoundTag itemTag = new CompoundTag();
            i.save(itemTag);
            return NMSUtil.toByteArray(itemTag);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize ItemStack", e);
        }
    }

    @Override
    public ItemStack deserialize(byte[] bytes, @Nullable World ignored) {
        try {
            CompoundTag data = NMSUtil.fromByteArray(bytes);
            net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.of(data);
            return CraftItemStack.asCraftMirror(itemStack);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize ItemStack", e);
        }
    }

    @Override
    public String toSNbt(ItemStack itemStack, @Nullable World ignored) {
        var item = NMSUtil.asNMSItemStack(itemStack);
        CompoundTag tag = new CompoundTag();
        item.save(tag);
        return tag.toString();
    }

    @Override
    public ItemStack fromSNbt(String snbt) {
        try {
            return CraftItemStack.asCraftMirror(
                    net.minecraft.world.item.ItemStack.of(TagParser.parseTag(snbt))
            );
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}