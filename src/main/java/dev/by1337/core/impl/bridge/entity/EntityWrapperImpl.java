package dev.by1337.core.impl.bridge.entity;

import dev.by1337.core.entity.EntityWrapper;
import dev.by1337.core.util.reflect.LambdaMetafactoryUtil;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
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
        entity.setPos(x, y, z);
    }

    private ClientboundSetEntityDataPacket makeEntityDataPacket(boolean all){
        var data = entity.getEntityData();
        if (data.isDirty()){
            dirty = data.packDirty();
        }
        var v = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true);
        if (!all && dirty != null && !dirty.isEmpty()){
            SETTER.accept(v, dirty);
        }
        return v;
    }

    public void sendSpawnPackets(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.connection.send(entity.getAddEntityPacket());
        nms.connection.send(makeEntityDataPacket(true));
    }

    public boolean hasDirtyData() {
        return dirty != null || entity.getEntityData().isDirty();
    }

    public void sendDirtyData(Player player) {
        if (dirty == null) {
            dirty = entity.getEntityData().packDirty();
        }
        if (dirty == null || dirty.isEmpty()) return;
        var nms = ((CraftPlayer) player).getHandle();
        nms.connection.send(makeEntityDataPacket(false));
    }

    public void sendRemovePacket(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.connection.send(new ClientboundRemoveEntitiesPacket(entity.getId()));
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
