package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public interface ICollisionAccess extends IBlockAccess {

    WorldBorder getWorldBorder();

    @Nullable
    IBlockAccess getChunkForCollisions(int i, int j);

    default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelshape) {
        return true;
    }

    default boolean isUnobstructed(IBlockData iblockdata, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        VoxelShape voxelshape = iblockdata.getCollisionShape(this, blockposition, voxelshapecollision);

        return voxelshape.isEmpty() || this.isUnobstructed((Entity) null, voxelshape.move((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ()));
    }

    default boolean isUnobstructed(Entity entity) {
        return this.isUnobstructed(entity, VoxelShapes.create(entity.getBoundingBox()));
    }

    default boolean noCollision(AxisAlignedBB axisalignedbb) {
        return this.noCollision((Entity) null, axisalignedbb);
    }

    default boolean noCollision(Entity entity) {
        return this.noCollision(entity, entity.getBoundingBox());
    }

    default boolean noCollision(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return this.noCollision(entity, axisalignedbb, false);
    }

    default boolean noCollision(@Nullable Entity entity, AxisAlignedBB axisalignedbb, boolean flag) {
        Iterable<VoxelShape> iterable = flag ? this.getBlockAndLiquidCollisions(entity, axisalignedbb) : this.getBlockCollisions(entity, axisalignedbb);
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            VoxelShape voxelshape = (VoxelShape) iterator.next();

            if (!voxelshape.isEmpty()) {
                return false;
            }
        }

        if (!this.getEntityCollisions(entity, axisalignedbb).isEmpty()) {
            return false;
        } else if (entity == null) {
            return true;
        } else {
            VoxelShape voxelshape1 = this.borderCollision(entity, axisalignedbb);

            return voxelshape1 == null || !VoxelShapes.joinIsNotEmpty(voxelshape1, VoxelShapes.create(axisalignedbb), OperatorBoolean.AND);
        }
    }

    default boolean noBlockCollision(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        Iterator iterator = this.getBlockCollisions(entity, axisalignedbb).iterator();

        VoxelShape voxelshape;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            voxelshape = (VoxelShape) iterator.next();
        } while (voxelshape.isEmpty());

        return false;
    }

    List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb);

    default Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        List<VoxelShape> list = this.getEntityCollisions(entity, axisalignedbb);
        Iterable<VoxelShape> iterable = this.getBlockCollisions(entity, axisalignedbb);

        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return () -> {
            return new VoxelShapeSpliterator<>(this, entity, axisalignedbb, false, (blockposition_mutableblockposition, voxelshape) -> {
                return voxelshape;
            });
        };
    }

    default Iterable<VoxelShape> getBlockAndLiquidCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return () -> {
            return new VoxelShapeSpliterator<>(this, VoxelShapeCollision.of(entity, true), axisalignedbb, false, (blockposition_mutableblockposition, voxelshape) -> {
                return voxelshape;
            });
        };
    }

    @Nullable
    private VoxelShape borderCollision(Entity entity, AxisAlignedBB axisalignedbb) {
        WorldBorder worldborder = this.getWorldBorder();

        return worldborder.isInsideCloseToBorder(entity, axisalignedbb) ? worldborder.getCollisionShape() : null;
    }

    default MovingObjectPositionBlock clipIncludingBorder(RayTrace raytrace) {
        MovingObjectPositionBlock movingobjectpositionblock = this.clip(raytrace);
        WorldBorder worldborder = this.getWorldBorder();

        if (worldborder.isWithinBounds(raytrace.getFrom()) && !worldborder.isWithinBounds(movingobjectpositionblock.getLocation())) {
            Vec3D vec3d = movingobjectpositionblock.getLocation().subtract(raytrace.getFrom());
            EnumDirection enumdirection = EnumDirection.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z);
            Vec3D vec3d1 = worldborder.clampVec3ToBound(movingobjectpositionblock.getLocation());

            return new MovingObjectPositionBlock(vec3d1, enumdirection, BlockPosition.containing(vec3d1), false, true);
        } else {
            return movingobjectpositionblock;
        }
    }

    default boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        VoxelShapeSpliterator<VoxelShape> voxelshapespliterator = new VoxelShapeSpliterator<>(this, entity, axisalignedbb, true, (blockposition_mutableblockposition, voxelshape) -> {
            return voxelshape;
        });

        do {
            if (!voxelshapespliterator.hasNext()) {
                return false;
            }
        } while (((VoxelShape) voxelshapespliterator.next()).isEmpty());

        return true;
    }

    default Optional<BlockPosition> findSupportingBlock(Entity entity, AxisAlignedBB axisalignedbb) {
        BlockPosition blockposition = null;
        double d0 = Double.MAX_VALUE;
        VoxelShapeSpliterator<BlockPosition> voxelshapespliterator = new VoxelShapeSpliterator<>(this, entity, axisalignedbb, false, (blockposition_mutableblockposition, voxelshape) -> {
            return blockposition_mutableblockposition;
        });

        while (voxelshapespliterator.hasNext()) {
            BlockPosition blockposition1 = (BlockPosition) voxelshapespliterator.next();
            double d1 = blockposition1.distToCenterSqr(entity.position());

            if (d1 < d0 || d1 == d0 && (blockposition == null || blockposition.compareTo((BaseBlockPosition) blockposition1) < 0)) {
                blockposition = blockposition1.immutable();
                d0 = d1;
            }
        }

        return Optional.ofNullable(blockposition);
    }

    default Optional<Vec3D> findFreePosition(@Nullable Entity entity, VoxelShape voxelshape, Vec3D vec3d, double d0, double d1, double d2) {
        if (voxelshape.isEmpty()) {
            return Optional.empty();
        } else {
            AxisAlignedBB axisalignedbb = voxelshape.bounds().inflate(d0, d1, d2);
            VoxelShape voxelshape1 = (VoxelShape) StreamSupport.stream(this.getBlockCollisions(entity, axisalignedbb).spliterator(), false).filter((voxelshape2) -> {
                return this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(voxelshape2.bounds());
            }).flatMap((voxelshape2) -> {
                return voxelshape2.toAabbs().stream();
            }).map((axisalignedbb1) -> {
                return axisalignedbb1.inflate(d0 / 2.0D, d1 / 2.0D, d2 / 2.0D);
            }).map(VoxelShapes::create).reduce(VoxelShapes.empty(), VoxelShapes::or);
            VoxelShape voxelshape2 = VoxelShapes.join(voxelshape, voxelshape1, OperatorBoolean.ONLY_FIRST);

            return voxelshape2.closestPointTo(vec3d);
        }
    }
}
