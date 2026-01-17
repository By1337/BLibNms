package dev.by1337.core.impl.bridge;

import net.minecraft.nbt.NbtIo;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class NMSUtil {

    public static byte[] toByteArray(net.minecraft.nbt.CompoundTag tag) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(tag, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static net.minecraft.nbt.CompoundTag fromByteArray(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return NbtIo.readCompressed(bis);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    public static net.minecraft.world.item.ItemStack asNMSItemStack(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack c) {
            return c.handle;
        }
        return CraftItemStack.asNMSCopy(itemStack);
    }
}
