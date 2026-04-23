package dev.by1337.core.impl.bridge.entity;

import dev.by1337.core.entity.EntityWrapper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class EntityWrapperImpl implements EntityWrapper {
    private final net.minecraft.world.entity.Entity entity;
    private @Nullable List<SynchedEntityData.DataValue<?>> dirty;
    private final ServerEntity serverEntity;

    public EntityWrapperImpl(EntityType type, World w, double x, double y, double z) {
        var level = ((CraftWorld) w).getHandle();
        entity = CraftEntityType.bukkitToMinecraft(type).create(level, EntitySpawnReason.COMMAND);
        entity.setPos(x, y, z);
        serverEntity = new ServerEntity(
                level,
                entity,
                1,
                false,
                NOP_SYNCHRONIZER,
                Set.of()
        );

    }

    public void sendSpawnPackets(Player player) {
        var nms = ((CraftPlayer) player).getHandle();
        nms.connection.send(entity.getAddEntityPacket(serverEntity));
        nms.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll()));
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

    private static final ServerEntity.Synchronizer NOP_SYNCHRONIZER = new ServerEntity.Synchronizer() {
        @Override
        public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {

        }

        @Override
        public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {

        }

        @Override
        public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {

        }
    };
}
