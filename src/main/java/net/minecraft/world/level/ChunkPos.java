package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Pos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import org.jspecify.annotations.Nullable;
import java.math.BigInteger;

public record ChunkPos(BigInteger x, BigInteger z)implements Pos {
    public ChunkPos(int x, int z) {
        this(BigInteger.valueOf(x), BigInteger.valueOf(z));
    }

    public static final Codec<ChunkPos> CODEC = Codec.INT_STREAM
        .<ChunkPos>comapFlatMap(input -> Util.fixedSize(input, 2).map(ints -> new ChunkPos(ints[0], ints[1])), pos -> IntStream.of(pos.x.intValueExact(), pos.z.intValueExact()))//临时
        .stable();
    public static final StreamCodec<ByteBuf, ChunkPos> STREAM_CODEC = new StreamCodec<ByteBuf, ChunkPos>() {
        public ChunkPos decode(final ByteBuf input) {
            return FriendlyByteBuf.readChunkPos(input);
        }

        public void encode(final ByteBuf output, final ChunkPos value) {
            FriendlyByteBuf.writeChunkPos(output, value);
        }
    };
    private static final int SAFETY_MARGIN = 1056;
    public static final ChunkPos INVALID_CHUNK_POS = new ChunkPos(1875066, 1875066);
    public static final ChunkPos ZERO = new ChunkPos(0, 0);
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 4294967295L;
    private static final int REGION_BITS = 5;
    public static final int REGION_SIZE = 32;
    private static final int REGION_MASK = 31;
    public static final int REGION_MAX_INDEX = 31;
    private static final int HASH_A = 1664525;
    private static final int HASH_C = 1013904223;
    private static final int HASH_Z_XOR = -559038737;

    public static ChunkPos containing(final BlockPos pos) {
        return new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    /** 结构引用序列化：ChunkPos 存为 [x0, z0][x1, z1] ... String */
    public static String packChunkPosList(final ObjectList<ChunkPos> positions) {
        StringBuilder builder = new StringBuilder();
        for (ChunkPos pos : positions) {
            builder.append(pos.x());
            builder.append(",");
            builder.append(pos.z());
            builder.append(",");
        }
        return builder.toString();
    }

    /** 结构引用序列化：ChunkPos 存为 [x0, z0][x1, z1] ... String */
    public static String packChunkPosSet(final ObjectSet<ChunkPos> positions) {
        StringBuilder builder = new StringBuilder();
        for (ChunkPos pos : positions) {
            builder.append(pos.x());
            builder.append(",");
            builder.append(pos.z());
            builder.append(",");
        }
        return builder.toString();
    }

    public static ChunkPos unpack(final long key) {
        return new ChunkPos((int)key, (int)(key >> 32));
    }

    public static ChunkPos minFromRegion(final int regionX, final int regionZ) {
        return new ChunkPos(regionX << 5, regionZ << 5);
    }

    public static ChunkPos maxFromRegion(final int regionX, final int regionZ) {
        return new ChunkPos((regionX << 5) + 31, (regionZ << 5) + 31);
    }

    public boolean isValid() {
        return isValid(this.x, this.z);
    }

    public static boolean isValid(final int x, final int z) {
        return Mth.absMax(x, z) <= ChunkPyramid.MAX_CHUNK_COORDINATE_VALUE;
    }

    public static boolean isValid(final BigInteger x, final BigInteger z) {
        return Mth.absMax(x, z).compareTo(BigInteger.valueOf(ChunkPyramid.MAX_CHUNK_COORDINATE_VALUE)) <= 0;
    }

    public static ChunkPos of(final int x, final int z) {
        return new ChunkPos(x, z);
    }

    public long pack() {
        return pack(this.x.intValueExact(), this.z.intValueExact());
    }

    public static long pack(final int x, final int z) {
        return x & 4294967295L | (z & 4294967295L) << 32;
    }
//
//    public static long fromSectionNode(final long sectionNode) {
//        return pack(SectionPos.x(sectionNode), SectionPos.z(sectionNode));
//    }

    public static ChunkPos fromSectionNode(final SectionPos sectionNode) {
        return new ChunkPos(SectionPos.x(sectionNode), SectionPos.z(sectionNode));
    }

//    public static long pack(final BlockPos pos) {
//        return pack(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
//    }

    public static ChunkPos of(final BlockPos pos) {
        return new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

//    public static int getX(final long pos) {
//        return (int)(pos & 4294967295L);
//    }
//
//    public static int getZ(final long pos) {
//        return (int)(pos >>> 32 & 4294967295L);
//    }

    public static int getX(final ChunkPos pos) {
        return pos.x.intValueExact();
    }

    public static int getZ(final ChunkPos pos) {
        return pos.z.intValueExact();
    }

    @Override
    public int hashCode() {
        return hash(this.x, this.z);
    }

    @Deprecated
    public static int hash(final int x, final int z) {
        int xTransform = 1664525 * x + 1013904223;
        int zTransform = 1664525 * (z ^ -559038737) + 1013904223;
        return xTransform ^ zTransform;
    }

    public static int hash(final BigInteger x, final BigInteger z) {
        int xTransform = 1664525 * x.intValue() + 1013904223;
        int zTransform = 1664525 * (z.intValue() ^ -559038737) + 1013904223;
        return xTransform ^ zTransform;
    }

    public int getMiddleBlockX() {
        return this.getBlockX(8);
    }

    public int getMiddleBlockZ() {
        return this.getBlockZ(8);
    }

    public int getMinBlockX() {
        return SectionPos.sectionToBlockCoord(this.x.intValueExact());
    }

    public int getMinBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z.intValueExact());
    }

    public int getMaxBlockX() {
        return this.getBlockX(15);
    }

    public int getMaxBlockZ() {
        return this.getBlockZ(15);
    }

    @Deprecated //临时
    public int getRegionX() {
        return this.x.intValueExact() >> 5;
    }

    @Deprecated
    public int getRegionZ() {
        return this.z.intValueExact() >> 5;
    }

    public static int getRegionX(final ChunkPos pos) {
        return getX(pos) >> 5;
    }

    public static int getRegionZ(final ChunkPos pos) {
        return getZ(pos) >> 5;
    }

    @Deprecated
    public int getRegionLocalX() {
        return this.x.intValueExact() & 31;
    }

    @Deprecated
    public int getRegionLocalZ() {
        return this.z.intValueExact() & 31;
    }

    public BlockPos getBlockAt(final int x, final int y, final int z) {
        return new BlockPos(this.getBlockX(x), y, this.getBlockZ(z));
    }

    public int getBlockX(final int offset) {
        return SectionPos.sectionToBlockCoord(this.x.intValueExact(), offset);
    }

    public int getBlockZ(final int offset) {
        return SectionPos.sectionToBlockCoord(this.z.intValueExact(), offset);
    }

    public BlockPos getMiddleBlockPosition(final int y) {
        return new BlockPos(this.getMiddleBlockX(), y, this.getMiddleBlockZ());
    }

    public boolean contains(final BlockPos pos) {
        return pos.getX() >= this.getMinBlockX() && pos.getZ() >= this.getMinBlockZ() && pos.getX() <= this.getMaxBlockX() && pos.getZ() <= this.getMaxBlockZ();
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public BlockPos getWorldPosition() {
        return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
    }

    public int getChessboardDistance(final ChunkPos pos) {
        return this.getChessboardDistance(pos.x.intValueExact(), pos.z.intValueExact());
    }

    public int getChessboardDistance(final int x, final int z) {
        return Mth.chessboardDistance(x, z, this.x.intValueExact(), this.z.intValueExact());
    }

    public int distanceSquared(final ChunkPos pos) {
        return this.distanceSquared(pos.x.intValueExact(), pos.z.intValueExact());
    }

//    public int distanceSquared(final ChunkPos pos) {
//        return this.distanceSquared(getX(pos), getZ(pos));
//    }

    private int distanceSquared(final int x, final int z) {
        int deltaX = x - this.x.intValueExact();
        int deltaZ = z - this.z.intValueExact();
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos center, final int radius) {
        return rangeClosed(new ChunkPos(center.x.intValueExact() - radius, center.z.intValueExact() - radius), new ChunkPos(center.x.intValueExact() + radius, center.z.intValueExact() + radius));
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos from, final ChunkPos to) {
        int xSize = Math.abs(from.x.intValueExact() - to.x.intValueExact()) + 1;
        int zSize = Math.abs(from.z.intValueExact() - to.z.intValueExact()) + 1;
        final int xDiff = from.x.intValueExact() < to.x.intValueExact() ? 1 : -1;
        final int zDiff = from.z.intValueExact() < to.z.intValueExact() ? 1 : -1;
        return StreamSupport.stream(new AbstractSpliterator<ChunkPos>(xSize * zSize, 64) {
            private @Nullable ChunkPos pos;

            @Override
            public boolean tryAdvance(final Consumer<? super ChunkPos> action) {
                if (this.pos == null) {
                    this.pos = from;
                } else {
                    int x = this.pos.x.intValueExact();
                    int z = this.pos.z.intValueExact();
                    if (x == to.x.intValueExact()) {
                        if (z == to.z.intValueExact()) {
                            return false;
                        }

                        this.pos = new ChunkPos(from.x.intValueExact(), z + zDiff);
                    } else {
                        this.pos = new ChunkPos(x + xDiff, z);
                    }
                }

                action.accept(this.pos);
                return true;
            }
        }, false);
    }
}