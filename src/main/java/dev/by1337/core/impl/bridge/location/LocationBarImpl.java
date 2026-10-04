package dev.by1337.core.impl.bridge.location;

import dev.by1337.core.bridge.location.LocationBar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class LocationBarImpl implements LocationBar {

    @Override
    public void send(Player viewer, Waypoint waypoint) {
        var icon = new net.minecraft.world.waypoints.Waypoint.Icon();
        icon.color = Optional.of(waypoint.getRgb());
        if (waypoint.getStyle() != null) {
            icon.style = ResourceKey.create(
                    WaypointStyleAssets.ROOT_ID,
                    CraftNamespacedKey.toMinecraft(waypoint.getStyle())
            );
        }
        ((CraftPlayer) viewer).getHandle().connection.send(
                ClientboundTrackedWaypointPacket.addWaypointPosition(
                        waypoint.getUuid(), icon, position(waypoint)
                )
        );
    }

    @Override
    public void update(Player viewer, Waypoint waypoint) {
        ((CraftPlayer) viewer).getHandle().connection.send(
                ClientboundTrackedWaypointPacket.updateWaypointPosition(
                        waypoint.getUuid(),
                        net.minecraft.world.waypoints.Waypoint.Icon.NULL,
                        position(waypoint)
                )
        );
    }

    @Override
    public void remove(Player viewer, UUID uuid) {
        ((CraftPlayer) viewer).getHandle().connection.send(
                ClientboundTrackedWaypointPacket.removeWaypoint(uuid)
        );
    }

    private static BlockPos position(Waypoint waypoint) {
        return BlockPos.containing(waypoint.getX(), waypoint.getY(), waypoint.getZ());
    }
}
