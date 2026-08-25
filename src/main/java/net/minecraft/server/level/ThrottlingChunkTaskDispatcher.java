package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public class ThrottlingChunkTaskDispatcher extends ChunkTaskDispatcher {
    private final ObjectSet<ChunkPos> chunkPositionsInExecution = new ObjectOpenHashSet<>();
    private final int maxChunksInExecution;
    private final String executorSchedulerName;

    public ThrottlingChunkTaskDispatcher(final TaskScheduler<Runnable> executor, final Executor dispatcherExecutor, final int maxChunksInExecution) {
        super(executor, dispatcherExecutor);
        this.maxChunksInExecution = maxChunksInExecution;
        this.executorSchedulerName = executor.name();
    }

    @Override
    protected void onRelease(final ChunkPos key) {
        this.chunkPositionsInExecution.remove(key);
    }

    @Override
    protected ChunkTaskPriorityQueue.@Nullable TasksForChunk popTasks() {
        return this.chunkPositionsInExecution.size() < this.maxChunksInExecution ? super.popTasks() : null;
    }

    @Override
    protected void scheduleForExecution(final ChunkTaskPriorityQueue.TasksForChunk tasksForChunk) {
        this.chunkPositionsInExecution.add(tasksForChunk.chunkPos());
        super.scheduleForExecution(tasksForChunk);
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return this.executorSchedulerName
            + "=["
            + this.chunkPositionsInExecution.stream().map(ChunkPos::toString).collect(Collectors.joining(","))
            + "], s="
            + this.sleeping;
    }
}