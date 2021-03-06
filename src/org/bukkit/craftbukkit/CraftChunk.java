package org.bukkit.craftbukkit;

import java.lang.ref.WeakReference;
import java.util.Arrays;


import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.ChunkSnapshot;

public class CraftChunk implements Chunk {
    private WeakReference<net.minecraft.world.chunk.Chunk> weakChunk;
    private final net.minecraft.world.WorldServer worldServer;
    private final int x;
    private final int z;
    private static final byte[] emptyData = new byte[2048];
    private static final short[] emptyBlockIDs = new short[4096];
    private static final byte[] emptySkyLight = new byte[2048];

    public CraftChunk(net.minecraft.world.chunk.Chunk chunk) {
        if (!(chunk instanceof net.minecraft.world.chunk.EmptyChunk)) {
            this.weakChunk = new WeakReference<net.minecraft.world.chunk.Chunk>(chunk);
        }

        worldServer = (net.minecraft.world.WorldServer) getHandle().field_76637_e;
        x = getHandle().field_76635_g;
        z = getHandle().field_76647_h;
    }

    public World getWorld() {
        return worldServer.getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    public net.minecraft.world.chunk.Chunk getHandle() {
        net.minecraft.world.chunk.Chunk c = weakChunk.get();

        if (c == null) {
            c = worldServer.func_72964_e(x, z);

            if (!(c instanceof net.minecraft.world.chunk.EmptyChunk)) {
                weakChunk = new WeakReference<net.minecraft.world.chunk.Chunk>(c);
            }
        }

        return c;
    }

    void breakLink() {
        weakChunk.clear();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "CraftChunk{" + "x=" + getX() + "z=" + getZ() + '}';
    }

    public Block getBlock(int x, int y, int z) {
        return new CraftBlock(this, (getX() << 4) | (x & 0xF), y & 0xFF, (getZ() << 4) | (z & 0xF));
    }

    public Entity[] getEntities() {
        int count = 0, index = 0;
        net.minecraft.world.chunk.Chunk chunk = getHandle();

        for (int i = 0; i < 16; i++) {
            count += chunk.field_76645_j[i].size();
        }

        Entity[] entities = new Entity[count];

        for (int i = 0; i < 16; i++) {
            for (Object obj : chunk.field_76645_j[i].toArray()) {
                if (!(obj instanceof net.minecraft.entity.Entity)) {
                    continue;
                }

                entities[index++] = ((net.minecraft.entity.Entity) obj).getBukkitEntity();
            }
        }

        return entities;
    }

    public BlockState[] getTileEntities() {
        int index = 0;
        net.minecraft.world.chunk.Chunk chunk = getHandle();
        BlockState[] entities = new BlockState[chunk.field_76648_i.size()];

        for (Object obj : chunk.field_76648_i.keySet().toArray()) {
            if (!(obj instanceof net.minecraft.world.ChunkPosition)) {
                continue;
            }

            net.minecraft.world.ChunkPosition position = (net.minecraft.world.ChunkPosition) obj;
            entities[index++] = worldServer.getWorld().getBlockAt(position.field_76930_a + (chunk.field_76635_g << 4), position.field_76928_b, position.field_76929_c + (chunk.field_76647_h << 4)).getState();
        }
        return entities;
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(this);
    }

    public boolean load() {
        return getWorld().loadChunk(getX(), getZ(), true);
    }

    public boolean load(boolean generate) {
        return getWorld().loadChunk(getX(), getZ(), generate);
    }

    public boolean unload() {
        return getWorld().unloadChunk(getX(), getZ());
    }

    public boolean unload(boolean save) {
        return getWorld().unloadChunk(getX(), getZ(), save);
    }

    public boolean unload(boolean save, boolean safe) {
        return getWorld().unloadChunk(getX(), getZ(), save, safe);
    }

    public ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }

    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.world.chunk.Chunk chunk = getHandle();

        net.minecraft.world.chunk.storage.ExtendedBlockStorage[] cs = chunk.func_76587_i(); /* Get sections */
        short[][] sectionBlockIDs = new short[cs.length][];
        byte[][] sectionBlockData = new byte[cs.length][];
        byte[][] sectionSkyLights = new byte[cs.length][];
        byte[][] sectionEmitLights = new byte[cs.length][];
        boolean[] sectionEmpty = new boolean[cs.length];

        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == null) { /* Section is empty? */
                sectionBlockIDs[i] = emptyBlockIDs;
                sectionBlockData[i] = emptyData;
                sectionSkyLights[i] = emptySkyLight;
                sectionEmitLights[i] = emptyData;
                sectionEmpty[i] = true;
            } else { /* Not empty */
                short[] blockids = new short[4096];
                byte[] baseids = cs[i].func_76658_g();

                /* Copy base IDs */
                for (int j = 0; j < 4096; j++) {
                    blockids[j] = (short) (baseids[j] & 0xFF);
                }

                if (cs[i].func_76660_i() != null) { /* If we've got extended IDs */
                    byte[] extids = cs[i].func_76660_i().field_76585_a;

                    for (int j = 0; j < 2048; j++) {
                        short b = (short) (extids[j] & 0xFF);

                        if (b == 0) {
                            continue;
                        }

                        blockids[j<<1] |= (b & 0x0F) << 8;
                        blockids[(j<<1)+1] |= (b & 0xF0) << 4;
                    }
                }

                sectionBlockIDs[i] = blockids;

                /* Get block data nibbles */
                sectionBlockData[i] = new byte[2048];
                System.arraycopy(cs[i].func_76669_j().field_76585_a, 0, sectionBlockData[i], 0, 2048); // Should be getData
                if (cs[i].func_76671_l() == null) {
                    sectionSkyLights[i] = emptyData;
                } else {
                    sectionSkyLights[i] = new byte[2048];
                    System.arraycopy(cs[i].func_76671_l().field_76585_a, 0, sectionSkyLights[i], 0, 2048); // Should be getSkyLight
                }
                sectionEmitLights[i] = new byte[2048];
                System.arraycopy(cs[i].func_76661_k().field_76585_a, 0, sectionEmitLights[i], 0, 2048); // Should be getBlockLight
            }
        }

        int[] hmap = null;

        if (includeMaxBlockY) {
            hmap = new int[256]; // Get copy of height map
            System.arraycopy(chunk.field_76634_f, 0, hmap, 0, 256);
        }

        net.minecraft.world.biome.BiomeGenBase[] biome = null;
        double[] biomeTemp = null;
        double[] biomeRain = null;

        if (includeBiome || includeBiomeTempRain) {
            net.minecraft.world.biome.WorldChunkManager wcm = chunk.field_76637_e.func_72959_q();

            if (includeBiome) {
                biome = new net.minecraft.world.biome.BiomeGenBase[256];
                for (int i = 0; i < 256; i++) {
                    biome[i] = chunk.func_76591_a(i & 0xF, i >> 4, wcm);
                }
            }

            if (includeBiomeTempRain) {
                biomeTemp = new double[256];
                biomeRain = new double[256];
                float[] dat = wcm.func_76934_b(null, getX() << 4, getZ() << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeTemp[i] = dat[i];
                }

                dat = wcm.func_76936_a(null, getX() << 4, getZ() << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeRain[i] = dat[i];
                }
            }
        }

        World world = getWorld();
        return new CraftChunkSnapshot(getX(), getZ(), world.getName(), world.getFullTime(), sectionBlockIDs, sectionBlockData, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, biome, biomeTemp, biomeRain);
    }

    public static ChunkSnapshot getEmptyChunkSnapshot(int x, int z, CraftWorld world, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.world.biome.BiomeGenBase[] biome = null;
        double[] biomeTemp = null;
        double[] biomeRain = null;

        if (includeBiome || includeBiomeTempRain) {
            net.minecraft.world.biome.WorldChunkManager wcm = world.getHandle().func_72959_q();

            if (includeBiome) {
                biome = new net.minecraft.world.biome.BiomeGenBase[256];
                for (int i = 0; i < 256; i++) {
                    biome[i] = world.getHandle().func_72807_a((x << 4) + (i & 0xF), (z << 4) + (i >> 4));
                }
            }

            if (includeBiomeTempRain) {
                biomeTemp = new double[256];
                biomeRain = new double[256];
                float[] dat = wcm.func_76934_b(null, x << 4, z << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeTemp[i] = dat[i];
                }

                dat = wcm.func_76936_a(null, x << 4, z << 4, 16, 16);

                for (int i = 0; i < 256; i++) {
                    biomeRain[i] = dat[i];
                }
            }
        }

        /* Fill with empty data */
        int hSection = world.getMaxHeight() >> 4;
        short[][] blockIDs = new short[hSection][];
        byte[][] skyLight = new byte[hSection][];
        byte[][] emitLight = new byte[hSection][];
        byte[][] blockData = new byte[hSection][];
        boolean[] empty = new boolean[hSection];

        for (int i = 0; i < hSection; i++) {
            blockIDs[i] = emptyBlockIDs;
            skyLight[i] = emptySkyLight;
            emitLight[i] = emptyData;
            blockData[i] = emptyData;
            empty[i] = true;
        }

        return new CraftChunkSnapshot(x, z, world.getName(), world.getFullTime(), blockIDs, blockData, skyLight, emitLight, empty, new int[256], biome, biomeTemp, biomeRain);
    }

    static {
        Arrays.fill(emptySkyLight, (byte) 0xFF);
    }
}
