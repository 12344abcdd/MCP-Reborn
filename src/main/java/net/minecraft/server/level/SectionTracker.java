package net.minecraft.server.level;

import net.minecraft.core.Pos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class SectionTracker extends DynamicGraphMinFixedPoint<SectionPos> {
    protected SectionTracker(final int levelCount, final int minQueueSize, final int minMapSize) {
        super(levelCount, minQueueSize, minMapSize);
    }

    @Override
    protected void checkNeighborsAfterUpdate(final SectionPos node, final int level, final boolean onlyDecrease) {
        if (!onlyDecrease || level < this.levelCount - 2) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        SectionPos neighbor = SectionPos.offset(node, offsetX, offsetY, offsetZ);
                        if (!neighbor.equals(node)) {
                            this.checkNeighbor(node, neighbor, level, onlyDecrease);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected int getComputedLevel(final SectionPos node, final SectionPos knownParent, final int knownLevelFromParent) {
        int computedLevel = knownLevelFromParent;

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    SectionPos neighbor = SectionPos.offset(node, offsetX, offsetY, offsetZ);
                    if (neighbor.equals(node)) {
                        neighbor = null;
                    }

                    if (neighbor != null && !neighbor.equals(knownParent)) {
                        int costFromNeighbor = this.computeLevelFromNeighbor(neighbor, node, this.getLevel(neighbor));
                        if (computedLevel > costFromNeighbor) {
                            computedLevel = costFromNeighbor;
                        }

                        if (computedLevel == 0) {
                            return computedLevel;
                        }
                    }
                }
            }
        }

        return computedLevel;
    }

    //@Override
    protected int computeLevelFromNeighbor(final SectionPos from, final SectionPos to, final int fromLevel) {
        return this.isSource(from) ? this.getLevelFromSource(to) : fromLevel + 1;
    }

    protected abstract int getLevelFromSource(SectionPos to);

    public void update(final SectionPos node, final int newLevelFrom, final boolean onlyDecreased) {
        this.checkEdge(null, node, newLevelFrom, onlyDecreased);
    }
}