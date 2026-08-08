package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.Pos;
import net.minecraft.world.level.ChunkPos;

public class LeveledPriorityQueue<T extends Pos> {
    private final int levelCount;
    private final ObjectLinkedOpenHashSet<T>[] queues;
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

    public Pos removeFirst() {
        ObjectLinkedOpenHashSet<T> queue = this.queues[this.firstQueuedLevel];
        Pos result = queue.removeFirst();
        if (queue.isEmpty()) {
            this.checkFirstQueuedLevel(this.levelCount);
        }

        return result;
    }

    public boolean isEmpty() {
        return this.firstQueuedLevel >= this.levelCount;
    }

    public void dequeue(final T node, final int key, final int upperBound) {
        ObjectLinkedOpenHashSet<T> queue = this.queues[key];
        queue.remove(node);
        if (queue.isEmpty() && this.firstQueuedLevel == key) {
            this.checkFirstQueuedLevel(upperBound);
        }
    }

    public void enqueue(final T node, final int key) {
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