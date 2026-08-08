package net.minecraft.server.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class ChunkTracker extends DynamicGraphMinFixedPoint<ChunkPos> {
    protected ChunkTracker(final int levelCount, final int minQueueSize, final int minMapSize) {
        super(levelCount, minQueueSize, minMapSize);
    }

    @Override
    protected boolean isSource(final ChunkPos node) {
        return ChunkPos.INVALID_CHUNK_POS.equals(node);
    }

    @Override
    protected void checkNeighborsAfterUpdate(final ChunkPos pos, final int level, final boolean onlyDecrease) {
        if (!onlyDecrease || level < this.levelCount - 2) {
            int x = pos.x().intValueExact();
            int z = pos.z().intValueExact();

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    ChunkPos neighbor = new ChunkPos(x + offsetX, z + offsetZ);
                    if (!neighbor.equals(pos)) {
                        this.checkNeighbor(pos, neighbor, level, onlyDecrease);
                    }
                }
            }
        }
    }

    @Override
    protected int getComputedLevel(final ChunkPos pos, final ChunkPos knownParent, final int knownLevelFromParent) {
        int computedLevel = knownLevelFromParent;
        int x = pos.x().intValueExact();
        int z = pos.z().intValueExact();

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                ChunkPos neighbor = new ChunkPos(x + offsetX, z + offsetZ);
                if (neighbor.equals(pos)) {
                    neighbor = ChunkPos.INVALID_CHUNK_POS;
                }

                if (!neighbor.equals(knownParent)) {
                    int costFromNeighbor = this.computeLevelFromNeighbor(neighbor, pos, this.getLevel(neighbor));
                    if (computedLevel > costFromNeighbor) {
                        computedLevel = costFromNeighbor;
                    }

                    if (computedLevel == 0) {
                        return computedLevel;
                    }
                }
            }
        }

        return computedLevel;
    }

    @Override
    protected int computeLevelFromNeighbor(final ChunkPos from, final ChunkPos to, final int fromLevel) {
        return from.equals(ChunkPos.INVALID_CHUNK_POS) ? this.getLevelFromSource(to) : fromLevel + 1;
    }

    protected abstract int getLevelFromSource(ChunkPos to);

    public void update(final ChunkPos node, final int newLevelFrom, final boolean onlyDecreased) {
        this.checkEdge(ChunkPos.INVALID_CHUNK_POS, node, newLevelFrom, onlyDecreased);
    }
}