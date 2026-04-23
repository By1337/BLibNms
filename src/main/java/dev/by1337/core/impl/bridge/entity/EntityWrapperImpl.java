package dev.by1337.core.impl.bridge.entity;

import dev.by1337.core.entity.EntityWrapper;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityWrapperImpl implements EntityWrapper {
    private final net.minecraft.world.entity.Entity entity;
    private @Nullable List<SynchedEntityData.DataValue<?>> dirty;

    public EntityWrapperImpl(EntityType type, World w, double x, double y, double z) {
        var level = ((CraftWorld) w).getHandle();
        entity = CraftMagicNumbers.getEntityTypes(type).create(level);
        entity.setPos(x, y, z);
    }

    public void sendSpawnPackets(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.connection.send(entity.getAddEntityPacket());
        entity.getEntityData().refresh(nms);
        //nms.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll()));
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
        nms.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), dirty));
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
}
