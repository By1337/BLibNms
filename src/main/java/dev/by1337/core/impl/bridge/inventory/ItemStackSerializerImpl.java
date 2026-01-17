package dev.by1337.core.impl.bridge.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.by1337.core.bridge.inventory.ItemStackSerializer;
import dev.by1337.core.impl.bridge.NMSUtil;
import net.minecraft.nbt.TagParser;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackSerializerImpl implements ItemStackSerializer {

    @Override
    public byte[] serialize(ItemStack itemStack, @Nullable World world) {
        try {
            return NMSUtil.toByteArray(NMSUtil.encodeItem(itemStack, world));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize ItemStack", e);
        }
    }

    @Override
    public ItemStack deserialize(byte[] bytes, @Nullable World world) {
        try {
            return NMSUtil.decodeItem(NMSUtil.fromByteArray(bytes), world);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize ItemStack", e);
        }
    }

    @Override
    public String toSNbt(ItemStack itemStack, @Nullable World world) {
        return NMSUtil.encodeItem(itemStack, world).toString();
    }

    @Override
    public ItemStack fromSNbt(String snbt) {
        try {
            return NMSUtil.decodeItem(TagParser.parseTag(snbt), null);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}