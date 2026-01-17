package dev.by1337.core.impl.bridge.nbt;

import dev.by1337.core.bridge.nbt.NbtBridge;
import dev.by1337.core.impl.bridge.NMSUtil;
import dev.by1337.core.util.nbt.BinaryNbt;
import net.minecraft.nbt.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class NbtBridgeImpl implements NbtBridge {
    public static final byte TAG_END = 0;
    public static final byte TAG_BYTE = 1;
    public static final byte TAG_SHORT = 2;
    public static final byte TAG_INT = 3;
    public static final byte TAG_LONG = 4;
    public static final byte TAG_FLOAT = 5;
    public static final byte TAG_DOUBLE = 6;
    public static final byte TAG_BYTE_ARRAY = 7;
    public static final byte TAG_STRING = 8;
    public static final byte TAG_INT_ARRAY = 11;
    public static final byte TAG_LONG_ARRAY = 12;
    public static final byte TAG_LIST = 9;
    public static final byte TAG_COMPOUND = 10;

    @Override
    public BinaryNbt.CompoundTag of(PersistentDataContainer pdc) {
        BinaryNbt.CompoundTag tag = new BinaryNbt.CompoundTag();
        if (pdc instanceof CraftPersistentDataContainer cpdc) {
            var map = cpdc.getRaw();
            for (Map.Entry<String, Tag> entry : map.entrySet()) {
                tag.put(entry.getKey(), ofNMS(entry.getValue()));
            }
        }
        return tag;
    }

    @Override
    public BinaryNbt.CompoundTag of(ItemStack itemStack, @Nullable World ignored) {
        var item = NMSUtil.asNMSItemStack(itemStack);
        CompoundTag tag = new CompoundTag();
        item.save(tag);
        return (BinaryNbt.CompoundTag) ofNMS(tag);
    }

    @Override
    public ItemStack create(BinaryNbt.CompoundTag tag, @Nullable World ignored) {
        CompoundTag nms = (CompoundTag) toNMS(tag);
        return CraftItemStack.asCraftMirror(
                net.minecraft.world.item.ItemStack.of(nms)
        );
    }

    @Override
    public Object toNMS(BinaryNbt.NbtTag tag) {
        return switch (tag.getId()) {
            case TAG_END -> EndTag.INSTANCE;
            case TAG_BYTE -> ByteTag.valueOf(((BinaryNbt.ByteTag) tag).value());
            case TAG_SHORT -> ShortTag.valueOf(((BinaryNbt.ShortTag) tag).value());
            case TAG_INT -> IntTag.valueOf(((BinaryNbt.IntTag) tag).value());
            case TAG_LONG -> LongTag.valueOf(((BinaryNbt.LongTag) tag).value());
            case TAG_FLOAT -> FloatTag.valueOf(((BinaryNbt.FloatTag) tag).value());
            case TAG_DOUBLE -> DoubleTag.valueOf(((BinaryNbt.DoubleTag) tag).value());
            case TAG_STRING -> StringTag.valueOf(((BinaryNbt.StringTag) tag).value());
            case TAG_BYTE_ARRAY -> new ByteArrayTag(((BinaryNbt.ByteArrayTag) tag).value());
            case TAG_INT_ARRAY -> new IntArrayTag(((BinaryNbt.IntArrayTag) tag).value());
            case TAG_LONG_ARRAY -> new LongArrayTag(((BinaryNbt.LongArrayTag) tag).value());
            case TAG_LIST -> {
                ListTag listTag = new ListTag();
                for (BinaryNbt.NbtTag nbtTag : ((BinaryNbt.ListTag) tag).tags()) {
                    listTag.add((Tag) toNMS(nbtTag));
                }
                yield listTag;
            }
            case TAG_COMPOUND -> {
                CompoundTag compoundTag = new CompoundTag();
                for (Map.Entry<String, BinaryNbt.NbtTag> entry : ((BinaryNbt.CompoundTag) tag).tags().entrySet()) {
                    compoundTag.put(entry.getKey(), (Tag) toNMS(entry.getValue()));
                }
                yield compoundTag;
            }
            default -> throw new IllegalArgumentException("Unexpected type: " + tag);
        };
    }

    @Override
    public BinaryNbt.NbtTag ofNMS(Object tag) {
        if (!(tag instanceof Tag nms)) {
            throw new IllegalArgumentException("Not an NMS Tag: " + tag);
        }

        return switch (nms.getId()) {
            case TAG_END -> BinaryNbt.EndTag.INSTANCE;
            case TAG_BYTE -> new BinaryNbt.ByteTag(((ByteTag) nms).getAsByte());
            case TAG_SHORT -> new BinaryNbt.ShortTag(((ShortTag) nms).getAsShort());
            case TAG_INT -> new BinaryNbt.IntTag(((IntTag) nms).getAsInt());
            case TAG_LONG -> new BinaryNbt.LongTag(((LongTag) nms).getAsLong());
            case TAG_FLOAT -> new BinaryNbt.FloatTag(((FloatTag) nms).getAsFloat());
            case TAG_DOUBLE -> new BinaryNbt.DoubleTag(((DoubleTag) nms).getAsDouble());
            case TAG_STRING -> new BinaryNbt.StringTag(nms.getAsString());
            case TAG_BYTE_ARRAY -> {
                byte[] v = ((ByteArrayTag) nms).getAsByteArray();
                yield new BinaryNbt.ByteArrayTag(v);
            }
            case TAG_INT_ARRAY -> {
                int[] v = ((IntArrayTag) nms).getAsIntArray();
                yield new BinaryNbt.IntArrayTag(v);
            }
            case TAG_LONG_ARRAY -> {
                long[] v = ((LongArrayTag) nms).getAsLongArray();
                yield new BinaryNbt.LongArrayTag(v);
            }
            case TAG_LIST -> {
                ListTag list = (ListTag) nms;
                BinaryNbt.ListTag out = new BinaryNbt.ListTag();
                for (Tag element : list) {
                    out.add(ofNMS(element));
                }
                yield out;
            }
            case TAG_COMPOUND -> {
                CompoundTag compound = (CompoundTag) nms;
                BinaryNbt.CompoundTag out = new BinaryNbt.CompoundTag();
                for (String key : compound.getAllKeys()) {
                    out.put(key, ofNMS(compound.get(key)));
                }
                yield out;
            }
            default -> throw new IllegalArgumentException("Unexpected NMS tag type: " + nms.getClass());
        };
    }
}