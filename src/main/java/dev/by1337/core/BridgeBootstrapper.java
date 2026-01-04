package dev.by1337.core;

import dev.by1337.core.impl.util.command.BukkitCommandRegisterImpl;
import dev.by1337.core.impl.util.inventory.InventoryUtilImpl;
import dev.by1337.core.impl.util.inventory.ItemStackSerializerImpl;
import dev.by1337.core.impl.util.world.BlockEntityUtilImpl;

public class BridgeBootstrapper {

    public static void bootstrap() {
        BCore.bukkitCommandRegister = new BukkitCommandRegisterImpl();
        BCore.inventoryUtil = new InventoryUtilImpl();
        BCore.blockEntityUtil = new BlockEntityUtilImpl();
        BCore.itemStackSerializer = new ItemStackSerializerImpl();
    }
}
