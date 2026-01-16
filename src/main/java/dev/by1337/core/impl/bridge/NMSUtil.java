package dev.by1337.core.impl.bridge;

import net.minecraft.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class NMSUtil {

    public static byte[] toByteArray(CompoundTag tag) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(tag, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static CompoundTag fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return NbtIo.readCompressed(bis);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    public static net.minecraft.world.item.ItemStack asNMSItemStack(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack c) {
            return c.getHandle();
        }
        return CraftItemStack.asNMSCopy(itemStack);
    }
}
