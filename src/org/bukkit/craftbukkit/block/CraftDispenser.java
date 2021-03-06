package org.bukkit.craftbukkit.block;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftDispenser extends CraftBlockState implements Dispenser {
    private final CraftWorld world;
    private final net.minecraft.tileentity.TileEntityDispenser dispenser;

    public CraftDispenser(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        dispenser = (net.minecraft.tileentity.TileEntityDispenser) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(dispenser);
    }

    public boolean dispense() {
        Block block = getBlock();

        if (block.getType() == Material.DISPENSER) {
            net.minecraft.block.BlockDispenser dispense = (net.minecraft.block.BlockDispenser) net.minecraft.block.Block.field_71958_P;

            dispense.func_82526_n(world.getHandle(), getX(), getY(), getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            dispenser.func_70296_d();
        }

        return result;
    }
}
