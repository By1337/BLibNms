package dev.by1337.core.impl.bridge.inventory;

import dev.by1337.core.bridge.inventory.InventoryUtil;
import dev.by1337.core.util.reflect.LambdaMetafactoryUtil;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;

public class InventoryUtilImpl implements InventoryUtil {
    private static final BiConsumer<ServerPlayer, Integer> CONTAINER_UPDATE_DELAY;

    @Override
    public void sendFakeTitle(Inventory inventory, Component newTitle) {
        for (HumanEntity humanEntity : new ArrayList<>(inventory.getViewers())) {
            if (humanEntity instanceof CraftPlayer craftPlayer) {
                ServerPlayer entityPlayer = craftPlayer.getHandle();
                ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                        entityPlayer.containerMenu.containerId, entityPlayer.containerMenu.getType(), PaperAdventure.asVanilla(newTitle)
                );
                entityPlayer.connection.send(packet);
                craftPlayer.updateInventory();
            }
        }
    }

    @Override
    public void flushInv(Player player) {
        ((CraftPlayer) player).getHandle().containerMenu.broadcastChanges();
    }

    @Override
    public void disableAutoFlush(Player player) {
        CONTAINER_UPDATE_DELAY.accept(((CraftPlayer) player).getHandle(), Integer.MAX_VALUE);
    }

    @Override
    public void enableAutoFlush(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CONTAINER_UPDATE_DELAY.accept(craftPlayer.getHandle(), craftPlayer.getHandle().level().paperConfig().tickRates.containerUpdate);
    }

    @Override
    public void setItemStackWithoutCopy(Inventory to, ItemStack who, int index) {
        net.minecraft.world.item.ItemStack nms;
        if (who instanceof CraftItemStack craftItemStack) {
            nms = Objects.requireNonNullElse(craftItemStack.handle, net.minecraft.world.item.ItemStack.EMPTY);
        } else {
            nms = CraftItemStack.asNMSCopy(who);
        }
        if (to instanceof CraftResultInventory cri) {
            //from CraftResultInventory#setItem
            if (index < cri.getIngredientsInventory().getContainerSize()) {
                cri.getIngredientsInventory().setItem(index, nms);
            } else {
                cri.getResultInventory().setItem(index - cri.getIngredientsInventory().getContainerSize(), nms);
            }
        } else if (to instanceof CraftInventoryCrafting cic) {
            //from CraftInventoryCrafting#setItem
            if (index < cic.getResultInventory().getContainerSize()) {
                cic.getResultInventory().setItem(index, nms);
            } else {
                cic.getMatrixInventory().setItem(index - cic.getResultInventory().getContainerSize(), nms);
            }
        } else if (to instanceof CraftInventoryPlayer cip) {
            //from CraftInventoryPlayer#setItem
            cip.getInventory().setItem(index, nms);
            if (cip.getHolder() != null) {
                ServerPlayer player = ((CraftPlayer) cip.getHolder()).getHandle();
                if (index < net.minecraft.world.entity.player.Inventory.getSelectionSize()) {
                    index += 36;
                } else if (index > 39) {
                    index += 5;
                } else if (index > 35) {
                    index = 8 - (index - 36);
                }

                player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), index, nms));
            }
        } else if (to instanceof CraftInventory ci) {
            ci.getInventory().setItem(index, nms);
        } else {
            throw new UnsupportedOperationException("Unknown inventory impl " + to.getClass());
        }
    }

    static {
        try {
            Field field = ServerPlayer.class.getDeclaredField("containerUpdateDelay");
            field.setAccessible(true);
            CONTAINER_UPDATE_DELAY = LambdaMetafactoryUtil.setterOf(field);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
