package dev.by1337.core.impl.util;

import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class NBTUtil {

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
}
