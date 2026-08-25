package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
    protected SkyLightSectionStorage(final LightChunkGetter chunkSource) {
        super(
            LightLayer.SKY,
            chunkSource,
            new SkyLightSectionStorage.SkyDataLayerStorageMap(new Object2ObjectOpenHashMap<>(), new Object2IntOpenHashMap<>(), Integer.MAX_VALUE)
        );
    }

    @Override
    protected int getLightValue(final long blockNode) {
        return this.getLightValue(blockNode, false);
    }

    protected int getLightValue(long blockNode, final boolean updating) {
        SectionPos sectionNode = SectionPos.blockToSection(blockNode);
        int sectionY = SectionPos.y(sectionNode);
        SkyLightSectionStorage.SkyDataLayerStorageMap sections = updating ? this.updatingSectionData : this.visibleSectionData;
        int topSection = sections.topSections.get(SectionPos.getZeroNode(sectionNode));
        if (topSection != sections.currentLowestY && sectionY < topSection) {
            DataLayer layer = this.getDataLayer(sections, sectionNode);
            if (layer == null) {
                blockNode = BlockPos.getFlatIndex(blockNode);

                while (layer == null) {
                    if (++sectionY >= topSection) {
                        return 15;
                    }

                    sectionNode = SectionPos.offset(sectionNode, Direction.UP);
                    layer = this.getDataLayer(sections, sectionNode);
                }
            }

            return layer.get(
                SectionPos.sectionRelative(BlockPos.getX(blockNode)),
                SectionPos.sectionRelative(BlockPos.getY(blockNode)),
                SectionPos.sectionRelative(BlockPos.getZ(blockNode))
            );
        } else {
            return updating && !this.lightOnInSection(sectionNode) ? 0 : 15;
        }
    }

    @Override
    protected void onNodeAdded(final SectionPos sectionNode) {
        int y = SectionPos.y(sectionNode);
        if (this.updatingSectionData.currentLowestY > y) {
            this.updatingSectionData.currentLowestY = y;
            this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
        }

        SectionPos zeroNode = SectionPos.getZeroNode(sectionNode);
        int oldTop = this.updatingSectionData.topSections.get(zeroNode);
        if (oldTop < y + 1) {
            this.updatingSectionData.topSections.put(zeroNode, y + 1);
        }
    }

    @Override
    protected void onNodeRemoved(final SectionPos sectionNode) {
        SectionPos zeroNode = SectionPos.getZeroNode(sectionNode);
        int y = SectionPos.y(sectionNode);
        if (this.updatingSectionData.topSections.get(zeroNode) == y + 1) {
            SectionPos newTopSection;
            for (newTopSection = sectionNode;
                !this.storingLightForSection(newTopSection) && this.hasLightDataAtOrBelow(y);
                newTopSection = SectionPos.offset(newTopSection, Direction.DOWN)
            ) {
                y--;
            }

            if (this.storingLightForSection(newTopSection)) {
                this.updatingSectionData.topSections.put(zeroNode, y + 1);
            } else {
                this.updatingSectionData.topSections.remove(zeroNode);
            }
        }
    }

    @Override
    protected DataLayer createDataLayer(final SectionPos sectionNode) {
        DataLayer queuedLayer = this.queuedSections.get(sectionNode);
        if (queuedLayer != null) {
            return queuedLayer;
        }

        int topSection = this.updatingSectionData.topSections.get(SectionPos.getZeroNode(sectionNode));
        if (topSection != this.updatingSectionData.currentLowestY && SectionPos.y(sectionNode) < topSection) {
            SectionPos aboveSection = SectionPos.offset(sectionNode, Direction.UP);

            DataLayer aboveData;
            while ((aboveData = this.getDataLayer(aboveSection, true)) == null) {
                aboveSection = SectionPos.offset(aboveSection, Direction.UP);
            }

            return repeatFirstLayer(aboveData);
        } else {
            return this.lightOnInSection(sectionNode) ? new DataLayer(15) : new DataLayer();
        }
    }

    private static DataLayer repeatFirstLayer(final DataLayer data) {
        if (data.isDefinitelyHomogenous()) {
            return data.copy();
        }

        byte[] input = data.getData();
        byte[] output = new byte[2048];

        for (int i = 0; i < 16; i++) {
            System.arraycopy(input, 0, output, i * 128, 128);
        }

        return new DataLayer(output);
    }

    protected boolean hasLightDataAtOrBelow(final int sectionY) {
        return sectionY >= this.updatingSectionData.currentLowestY;
    }

    protected boolean isAboveData(final SectionPos sectionNode) {
        SectionPos zeroNode = SectionPos.getZeroNode(sectionNode);
        int topSection = this.updatingSectionData.topSections.get(zeroNode);
        return topSection == this.updatingSectionData.currentLowestY || SectionPos.y(sectionNode) >= topSection;
    }

    protected int getTopSectionY(final SectionPos zeroNode) {
        return this.updatingSectionData.topSections.get(zeroNode);
    }

    protected int getBottomSectionY() {
        return this.updatingSectionData.currentLowestY;
    }

    protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
        private int currentLowestY;
        private final Object2IntOpenHashMap<SectionPos> topSections;

        public SkyDataLayerStorageMap(final Object2ObjectOpenHashMap<SectionPos,DataLayer> map, final Object2IntOpenHashMap<SectionPos> topSections, final int currentLowestY) {
            super(map);
            this.topSections = topSections;
            topSections.defaultReturnValue(currentLowestY);
            this.currentLowestY = currentLowestY;
        }

        public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
            return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }
    }
}