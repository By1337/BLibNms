package dev.by1337.core.impl.bridge;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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
            return NbtIo.readCompressed(bis, NbtAccounter.unlimitedHeap());
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

    public static ItemStack decodeItem(CompoundTag tag, @Nullable World world) {
        RegistryAccess registryAccess;
        if (world != null) {
            registryAccess = ((CraftWorld) world).getHandle().registryAccess();
        } else {
            registryAccess = MinecraftServer.getServer().registryAccess();
        }
        return CraftItemStack.asCraftMirror(
                net.minecraft.world.item.ItemStack.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, registryAccess), tag)
                        .getOrThrow().getFirst()
        );
    }
    public static CompoundTag encodeItem(ItemStack itemStack, @Nullable World world) {
        RegistryAccess registryAccess;
        if (world != null) {
            registryAccess = ((CraftWorld) world).getHandle().registryAccess();
        } else {
            registryAccess = MinecraftServer.getServer().registryAccess();
        }
        return (CompoundTag) net.minecraft.world.item.ItemStack.CODEC.encodeStart(
                RegistryOps.create(NbtOps.INSTANCE, registryAccess), asNMSItemStack(itemStack)
        ).getOrThrow();
    }
}
