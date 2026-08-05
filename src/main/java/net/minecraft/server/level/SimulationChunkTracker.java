package net.minecraft.server.level;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;

public class SimulationChunkTracker extends ChunkTracker {
    public static final int MAX_LEVEL = 33;
    protected final Object2ByteMap<ChunkPos> chunks = new Object2ByteOpenHashMap<>();
    private final TicketStorage ticketStorage;

    public SimulationChunkTracker(final TicketStorage ticketStorage) {
        super(34, 16, 256);
        this.ticketStorage = ticketStorage;
        ticketStorage.setSimulationChunkUpdatedListener(this::update);
        this.chunks.defaultReturnValue((byte)33);
    }

    @Override
    protected int getLevelFromSource(final ChunkPos to) {
        return this.ticketStorage.getTicketLevelAt(to, true);
    }

//    public int getLevel(final ChunkPos node) {
//        return this.getLevel(node);
//    }

    @Override
    public int getLevel(final ChunkPos node) {
        return this.chunks.get(node);
    }

    @Override
    protected void setLevel(final ChunkPos node, final int level) {
        if (level >= 33) {
            this.chunks.remove(node);
        } else {
            this.chunks.put(node, (byte)level);
        }
    }

    public void runAllUpdates() {
        this.runUpdates(Integer.MAX_VALUE);
    }
}