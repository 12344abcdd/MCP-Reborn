package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.world.level.ChunkPos;

public class LeveledPriorityQueue {
    private final int levelCount;
    private final ObjectLinkedOpenHashSet[] queues;
    private int firstQueuedLevel;

    public LeveledPriorityQueue(final int levelCount, final int minSize) {
        this.levelCount = levelCount;
        this.queues = new ObjectLinkedOpenHashSet[levelCount];

        for (int i = 0; i < levelCount; i++) {
            this.queues[i] = new ObjectLinkedOpenHashSet(minSize, 0.5F) {
                @Override
                protected void rehash(final int newN) {
                    if (newN > minSize) {
                        super.rehash(newN);
                    }
                }
            };
        }

        this.firstQueuedLevel = levelCount;
    }

    public ChunkPos removeFirst() {
        ObjectLinkedOpenHashSet queue = this.queues[this.firstQueuedLevel];
        ChunkPos result = (ChunkPos) queue.removeFirst();
        if (queue.isEmpty()) {
            this.checkFirstQueuedLevel(this.levelCount);
        }

        return result;
    }

    public boolean isEmpty() {
        return this.firstQueuedLevel >= this.levelCount;
    }

    public void dequeue(final ChunkPos node, final int key, final int upperBound) {
        ObjectLinkedOpenHashSet queue = this.queues[key];
        queue.remove(node);
        if (queue.isEmpty() && this.firstQueuedLevel == key) {
            this.checkFirstQueuedLevel(upperBound);
        }
    }

    public void enqueue(final ChunkPos node, final int key) {
        this.queues[key].add(node);
        if (this.firstQueuedLevel > key) {
            this.firstQueuedLevel = key;
        }
    }

    private void checkFirstQueuedLevel(final int upperBound) {
        int oldLevel = this.firstQueuedLevel;
        this.firstQueuedLevel = upperBound;

        for (int i = oldLevel + 1; i < upperBound; i++) {
            if (!this.queues[i].isEmpty()) {
                this.firstQueuedLevel = i;
                break;
            }
        }
    }
}