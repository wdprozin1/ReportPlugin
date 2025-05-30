package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.BlockUtil;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceRecord;
import net.minecraft.world.level.block.BlockPortal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.HeightMap;

public class PortalTravelAgent {

    public static final int TICKET_RADIUS = 3;
    private static final int NETHER_PORTAL_RADIUS = 16;
    private static final int OVERWORLD_PORTAL_RADIUS = 128;
    private static final int FRAME_HEIGHT = 5;
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_BOX = 3;
    private static final int FRAME_HEIGHT_START = -1;
    private static final int FRAME_HEIGHT_END = 4;
    private static final int FRAME_WIDTH_START = -1;
    private static final int FRAME_WIDTH_END = 3;
    private static final int FRAME_BOX_START = -1;
    private static final int FRAME_BOX_END = 2;
    private static final int NOTHING_FOUND = -1;
    private final WorldServer level;

    public PortalTravelAgent(WorldServer worldserver) {
        this.level = worldserver;
    }

    public Optional<BlockPosition> findClosestPortalPosition(BlockPosition blockposition, boolean flag, WorldBorder worldborder) {
        VillagePlace villageplace = this.level.getPoiManager();
        int i = flag ? 16 : 128;

        villageplace.ensureLoadedAndValid(this.level, blockposition, i);
        Stream stream = villageplace.getInSquare((holder) -> {
            return holder.is(PoiTypes.NETHER_PORTAL);
        }, blockposition, i, VillagePlace.Occupancy.ANY).map(VillagePlaceRecord::getPos);

        Objects.requireNonNull(worldborder);
        return stream.filter(worldborder::isWithinBounds).filter((blockposition1) -> {
            return this.level.getBlockState(blockposition1).hasProperty(BlockProperties.HORIZONTAL_AXIS);
        }).min(Comparator.comparingDouble((blockposition1) -> {
            return blockposition1.distSqr(blockposition);
        }).thenComparingInt(BaseBlockPosition::getY));
    }

    public Optional<BlockUtil.Rectangle> createPortal(BlockPosition blockposition, EnumDirection.EnumAxis enumdirection_enumaxis) {
        EnumDirection enumdirection = EnumDirection.get(EnumDirection.EnumAxisDirection.POSITIVE, enumdirection_enumaxis);
        double d0 = -1.0D;
        BlockPosition blockposition1 = null;
        double d1 = -1.0D;
        BlockPosition blockposition2 = null;
        WorldBorder worldborder = this.level.getWorldBorder();
        int i = Math.min(this.level.getMaxY(), this.level.getMinY() + this.level.getLogicalHeight() - 1);
        boolean flag = true;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();
        Iterator iterator = BlockPosition.spiralAround(blockposition, 16, EnumDirection.EAST, EnumDirection.SOUTH).iterator();

        int j;
        int k;
        int l;
        int i1;

        while (iterator.hasNext()) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = (BlockPosition.MutableBlockPosition) iterator.next();

            j = Math.min(i, this.level.getHeight(HeightMap.Type.MOTION_BLOCKING, blockposition_mutableblockposition1.getX(), blockposition_mutableblockposition1.getZ()));
            if (worldborder.isWithinBounds((BlockPosition) blockposition_mutableblockposition1) && worldborder.isWithinBounds((BlockPosition) blockposition_mutableblockposition1.move(enumdirection, 1))) {
                blockposition_mutableblockposition1.move(enumdirection.getOpposite(), 1);

                for (k = j; k >= this.level.getMinY(); --k) {
                    blockposition_mutableblockposition1.setY(k);
                    if (this.canPortalReplaceBlock(blockposition_mutableblockposition1)) {
                        for (l = k; k > this.level.getMinY() && this.canPortalReplaceBlock(blockposition_mutableblockposition1.move(EnumDirection.DOWN)); --k) {
                            ;
                        }

                        if (k + 4 <= i) {
                            i1 = l - k;
                            if (i1 <= 0 || i1 >= 3) {
                                blockposition_mutableblockposition1.setY(k);
                                if (this.canHostFrame(blockposition_mutableblockposition1, blockposition_mutableblockposition, enumdirection, 0)) {
                                    double d2 = blockposition.distSqr(blockposition_mutableblockposition1);

                                    if (this.canHostFrame(blockposition_mutableblockposition1, blockposition_mutableblockposition, enumdirection, -1) && this.canHostFrame(blockposition_mutableblockposition1, blockposition_mutableblockposition, enumdirection, 1) && (d0 == -1.0D || d0 > d2)) {
                                        d0 = d2;
                                        blockposition1 = blockposition_mutableblockposition1.immutable();
                                    }

                                    if (d0 == -1.0D && (d1 == -1.0D || d1 > d2)) {
                                        d1 = d2;
                                        blockposition2 = blockposition_mutableblockposition1.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (d0 == -1.0D && d1 != -1.0D) {
            blockposition1 = blockposition2;
            d0 = d1;
        }

        int j1;
        int k1;

        if (d0 == -1.0D) {
            j1 = Math.max(this.level.getMinY() - -1, 70);
            k1 = i - 9;
            if (k1 < j1) {
                return Optional.empty();
            }

            blockposition1 = (new BlockPosition(blockposition.getX() - enumdirection.getStepX() * 1, MathHelper.clamp(blockposition.getY(), j1, k1), blockposition.getZ() - enumdirection.getStepZ() * 1)).immutable();
            blockposition1 = worldborder.clampToBounds(blockposition1);
            EnumDirection enumdirection1 = enumdirection.getClockWise();

            for (k = -1; k < 2; ++k) {
                for (l = 0; l < 2; ++l) {
                    for (i1 = -1; i1 < 3; ++i1) {
                        IBlockData iblockdata = i1 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();

                        blockposition_mutableblockposition.setWithOffset(blockposition1, l * enumdirection.getStepX() + k * enumdirection1.getStepX(), i1, l * enumdirection.getStepZ() + k * enumdirection1.getStepZ());
                        this.level.setBlockAndUpdate(blockposition_mutableblockposition, iblockdata);
                    }
                }
            }
        }

        for (j1 = -1; j1 < 3; ++j1) {
            for (k1 = -1; k1 < 4; ++k1) {
                if (j1 == -1 || j1 == 2 || k1 == -1 || k1 == 3) {
                    blockposition_mutableblockposition.setWithOffset(blockposition1, j1 * enumdirection.getStepX(), k1, j1 * enumdirection.getStepZ());
                    this.level.setBlock(blockposition_mutableblockposition, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        IBlockData iblockdata1 = (IBlockData) Blocks.NETHER_PORTAL.defaultBlockState().setValue(BlockPortal.AXIS, enumdirection_enumaxis);

        for (k1 = 0; k1 < 2; ++k1) {
            for (j = 0; j < 3; ++j) {
                blockposition_mutableblockposition.setWithOffset(blockposition1, k1 * enumdirection.getStepX(), j, k1 * enumdirection.getStepZ());
                this.level.setBlock(blockposition_mutableblockposition, iblockdata1, 18);
            }
        }

        return Optional.of(new BlockUtil.Rectangle(blockposition1.immutable(), 2, 3));
    }

    private boolean canPortalReplaceBlock(BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        IBlockData iblockdata = this.level.getBlockState(blockposition_mutableblockposition);

        return iblockdata.canBeReplaced() && iblockdata.getFluidState().isEmpty();
    }

    private boolean canHostFrame(BlockPosition blockposition, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, EnumDirection enumdirection, int i) {
        EnumDirection enumdirection1 = enumdirection.getClockWise();

        for (int j = -1; j < 3; ++j) {
            for (int k = -1; k < 4; ++k) {
                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection.getStepX() * j + enumdirection1.getStepX() * i, k, enumdirection.getStepZ() * j + enumdirection1.getStepZ() * i);
                if (k < 0 && !this.level.getBlockState(blockposition_mutableblockposition).isSolid()) {
                    return false;
                }

                if (k >= 0 && !this.canPortalReplaceBlock(blockposition_mutableblockposition)) {
                    return false;
                }
            }
        }

        return true;
    }
}
