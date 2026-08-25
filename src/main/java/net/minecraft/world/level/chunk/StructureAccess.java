package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jspecify.annotations.Nullable;

public interface StructureAccess {
    @Nullable StructureStart getStartForStructure(Structure structure);

    void setStartForStructure(Structure structure, StructureStart structureStart);

    ObjectSet<ChunkPos> getReferencesForStructure(Structure structure);

    void addReferenceForStructure(Structure structure, ChunkPos reference);

    Map<Structure, ObjectSet<ChunkPos>> getAllReferences();

    void setAllReferences(Map<Structure, ObjectSet<ChunkPos>> data);
}