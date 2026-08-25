package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import org.jspecify.annotations.Nullable;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
    private static final int CACHE_SIZE = 2;
    private final SectionPos[] lastSectionKeys = new SectionPos[2];
    private final @Nullable DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Object2ObjectOpenHashMap<SectionPos,DataLayer> map;

    protected DataLayerStorageMap(final Object2ObjectOpenHashMap<SectionPos,DataLayer> map) {
        this.map = map;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public DataLayer copyDataLayer(final SectionPos sectionNode) {
        DataLayer newDataLayer = this.map.get(sectionNode).copy();
        this.map.put(sectionNode, newDataLayer);
        this.clearCache();
        return newDataLayer;
    }

    public boolean hasLayer(final SectionPos sectionNode) {
        return this.map.containsKey(sectionNode);
    }

    public @Nullable DataLayer getLayer(final SectionPos sectionNode) {
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; i++) {
                if (sectionNode == this.lastSectionKeys[i]) {
                    return this.lastSections[i];
                }
            }
        }

        DataLayer data = this.map.get(sectionNode);
        if (data == null) {
            return null;
        }

        if (this.cacheEnabled) {
            for (int i = 1; i > 0; i--) {
                this.lastSectionKeys[i] = this.lastSectionKeys[i - 1];
                this.lastSections[i] = this.lastSections[i - 1];
            }

            this.lastSectionKeys[0] = sectionNode;
            this.lastSections[0] = data;
        }

        return data;
    }

    public @Nullable DataLayer removeLayer(final SectionPos sectionNode) {
        return this.map.remove(sectionNode);
    }

    public void setLayer(final SectionPos sectionNode, final DataLayer layer) {
        this.map.put(sectionNode, layer);
    }

    public void clearCache() {
        for (int i = 0; i < 2; i++) {
            this.lastSectionKeys[i] = null;
            this.lastSections[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}