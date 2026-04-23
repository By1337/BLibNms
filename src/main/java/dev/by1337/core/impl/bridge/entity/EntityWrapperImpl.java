package dev.by1337.core.impl.bridge.entity;

import dev.by1337.core.entity.EntityWrapper;
import dev.by1337.core.util.reflect.LambdaMetafactoryUtil;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EntityWrapperImpl implements EntityWrapper {
    private final net.minecraft.world.entity.Entity entity;
    private @Nullable List<SynchedEntityData.DataItem<?>> dirty;
    private static final BiConsumer<ClientboundSetEntityDataPacket, List<SynchedEntityData.DataItem<?>>> SETTER;

    public EntityWrapperImpl(EntityType type, World w, double x, double y, double z) {
        var level = ((CraftWorld) w).getHandle();
        entity = CraftMagicNumbers.getEntityTypes(type).create(level);
        entity.setPosition(x, y, z);
    }

    private ClientboundSetEntityDataPacket makeEntityDataPacket(boolean all) {
        var data = entity.getDataWatcher();
        if (data.isDirty()) {
            dirty = data.packDirty();
        }
        var v = new ClientboundSetEntityDataPacket(entity.getId(), entity.getDataWatcher(), true);
        if (!all && dirty != null && !dirty.isEmpty()) {
            SETTER.accept(v, dirty);
        }
        return v;
    }

    public void sendSpawnPackets(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.playerConnection.sendPacket(entity.getAddEntityPacket());
        nms.playerConnection.sendPacket(makeEntityDataPacket(true));
    }

    public boolean hasDirtyData() {
        return dirty != null || entity.getDataWatcher().isDirty();
    }

    public void sendDirtyData(Player player) {
        if (dirty == null) {
            dirty = entity.getDataWatcher().packDirty();
        }
        if (dirty == null || dirty.isEmpty()) return;
        var nms = ((CraftPlayer) player).getHandle();
        nms.playerConnection.sendPacket(makeEntityDataPacket(false));
    }

    public void sendRemovePacket(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.playerConnection.sendPacket(new ClientboundRemoveEntitiesPacket(entity.getId()));
    }

    public void removeDirtyData() {
        dirty = null;
    }

    public org.bukkit.entity.Entity asBukkit() {
        return entity.getBukkitEntity();
    }

    public int getId() {
        return entity.getId();
    }

    static {
        try {
            Field list = null;
            for (Field field : ClientboundSetEntityDataPacket.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == List.class) {
                    list = field;
                    break;
                }
            }
            Objects.requireNonNull(list, "Field not found ClientboundSetEntityDataPacket#packedItems");
            SETTER = LambdaMetafactoryUtil.setterOf(list);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
