package org.bukkit.craftbukkit.block;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftDropper extends CraftBlockState implements Dropper {
    private final CraftWorld world;
    private final net.minecraft.tileentity.TileEntityDropper dropper;

    public CraftDropper(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        dropper = (net.minecraft.tileentity.TileEntityDropper) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(dropper);
    }

    public void drop() {
        Block block = getBlock();

        if (block.getType() == Material.DROPPER) {
            net.minecraft.block.BlockDropper drop = (net.minecraft.block.BlockDropper) net.minecraft.block.Block.field_96469_cy;

            drop.func_82526_n(world.getHandle(), getX(), getY(), getZ());
        }
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            dropper.func_70296_d();
        }

        return result;
    }
}
