package net.minecraft.client.renderer.state.level;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkLoadingRenderState {
    public ObjectOpenHashSet<SectionPos> addedEmptySections = new ObjectOpenHashSet<>();
    public ObjectOpenHashSet<SectionPos> removedEmptySections = new ObjectOpenHashSet<>();
    public ObjectOpenHashSet<ChunkPos> addedLoadedChunks = new ObjectOpenHashSet<>();
    public ObjectOpenHashSet<ChunkPos> removedLoadedChunks = new ObjectOpenHashSet<>();
    public ObjectOpenHashSet<ChunkPos> loadedExpectedChunks = new ObjectOpenHashSet<>();

    public void reset() {
        this.loadedExpectedChunks.clear();
    }
}