package com.bettermeteor.addon.utils;

import meteordevelopment.meteorclient.utils.entity.simulator.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.entity.simulator.SimulationStep;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class EnderPearlInfoUtils {
    private static final ProjectileEntitySimulator SIMULATOR = new ProjectileEntitySimulator();

    private EnderPearlInfoUtils() {
    }

    public static PearlInfo getInfo(MinecraftClient mc, int maxSimulationTicks, double nearbyRange) {
        if (mc == null || mc.world == null || mc.player == null) return null;

        EnderPearlEntity pearl = getOwnPearl(mc);
        if (pearl == null) return null;
        if (!SIMULATOR.set(pearl)) return null;

        for (int ticks = 0; ticks <= maxSimulationTicks; ticks++) {
            SimulationStep step = SIMULATOR.tick();
            if (!step.shouldStop) continue;

            Vec3d landingPos = getLandingPos(step);
            return analyzeLanding(mc, ticks + 1, landingPos, nearbyRange);
        }

        return null;
    }

    private static EnderPearlEntity getOwnPearl(MinecraftClient mc) {
        EnderPearlEntity bestPearl = null;
        double bestDistance = Double.MAX_VALUE;

        for (EnderPearlEntity pearl : mc.world.getEntitiesByClass(EnderPearlEntity.class, mc.player.getBoundingBox().expand(256), entity -> true)) {
            if (pearl == null || pearl.isRemoved()) continue;
            if (pearl.getOwner() != mc.player) continue;

            double distance = pearl.squaredDistanceTo(mc.player);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPearl = pearl;
            }
        }

        return bestPearl;
    }

    private static Vec3d getLandingPos(SimulationStep step) {
        if (step.hitResults != null && step.hitResults.length > 0 && step.hitResults[0] != null) {
            HitResult result = step.hitResults[0];
            return result.getPos();
        }

        return new Vec3d(SIMULATOR.pos.x, SIMULATOR.pos.y, SIMULATOR.pos.z);
    }

    private static PearlInfo analyzeLanding(MinecraftClient mc, int ticksLeft, Vec3d landingPos, double nearbyRange) {
        Box nearbyBox = Box.of(landingPos, nearbyRange * 2.0, nearbyRange * 2.0, nearbyRange * 2.0);

        List<String> nearbyPlayers = mc.world.getEntitiesByClass(PlayerEntity.class, nearbyBox, player ->
            player != mc.player && player.isAlive() && !player.isSpectator()
        ).stream().map(player -> player.getName().getString()).sorted(String.CASE_INSENSITIVE_ORDER).toList();

        boolean crystalsNearby = !mc.world.getEntitiesByClass(EndCrystalEntity.class, nearbyBox, crystal -> crystal.isAlive()).isEmpty();
        boolean tntNearby = !mc.world.getEntitiesByClass(TntEntity.class, nearbyBox, tnt -> tnt.isAlive()).isEmpty()
            || hasNearbyBlock(mc, landingPos, nearbyRange, Blocks.TNT);
        AnchorStatus anchorStatus = getAnchorStatus(mc, landingPos, nearbyRange);

        LandingFluid landingFluid = getLandingFluid(mc, landingPos);

        return new PearlInfo(ticksLeft, landingPos, nearbyPlayers, crystalsNearby, anchorStatus, tntNearby, landingFluid);
    }

    private static boolean hasNearbyBlock(MinecraftClient mc, Vec3d landingPos, double range, net.minecraft.block.Block block) {
        BlockPos center = BlockPos.ofFloored(landingPos);
        int radius = MathHelper.ceil(range);
        double rangeSq = range * range;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    double dx = x + 0.5 - landingPos.x;
                    double dy = y + 0.5 - landingPos.y;
                    double dz = z + 0.5 - landingPos.z;
                    if (dx * dx + dy * dy + dz * dz > rangeSq) continue;

                    mutable.set(x, y, z);
                    BlockState state = mc.world.getBlockState(mutable);
                    if (state.isOf(block)) return true;
                }
            }
        }

        return false;
    }

    private static AnchorStatus getAnchorStatus(MinecraftClient mc, Vec3d landingPos, double range) {
        BlockPos center = BlockPos.ofFloored(landingPos);
        int radius = MathHelper.ceil(range);
        double rangeSq = range * range;
        boolean foundUncharged = false;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    double dx = x + 0.5 - landingPos.x;
                    double dy = y + 0.5 - landingPos.y;
                    double dz = z + 0.5 - landingPos.z;
                    if (dx * dx + dy * dy + dz * dz > rangeSq) continue;

                    mutable.set(x, y, z);
                    BlockState state = mc.world.getBlockState(mutable);
                    if (!state.isOf(Blocks.RESPAWN_ANCHOR)) continue;

                    int charges = state.contains(RespawnAnchorBlock.CHARGES) ? state.get(RespawnAnchorBlock.CHARGES) : 0;
                    if (charges > 0) return AnchorStatus.Charged;
                    foundUncharged = true;
                }
            }
        }

        return foundUncharged ? AnchorStatus.Uncharged : AnchorStatus.None;
    }

    private static LandingFluid getLandingFluid(MinecraftClient mc, Vec3d landingPos) {
        BlockPos pos = BlockPos.ofFloored(landingPos);

        if (isWater(mc, pos) || isWater(mc, pos.down())) return LandingFluid.Water;
        if (isLava(mc, pos) || isLava(mc, pos.down())) return LandingFluid.Lava;

        return LandingFluid.None;
    }

    private static boolean isWater(MinecraftClient mc, BlockPos pos) {
        FluidState fluid = mc.world.getFluidState(pos);
        return fluid.isIn(FluidTags.WATER) || mc.world.getBlockState(pos).isOf(Blocks.WATER);
    }

    private static boolean isLava(MinecraftClient mc, BlockPos pos) {
        FluidState fluid = mc.world.getFluidState(pos);
        return fluid.isIn(FluidTags.LAVA) || mc.world.getBlockState(pos).isOf(Blocks.LAVA);
    }

    public record PearlInfo(
        int ticksLeft,
        Vec3d landingPos,
        List<String> nearbyPlayers,
        boolean crystalsNearby,
        AnchorStatus anchorStatus,
        boolean tntNearby,
        LandingFluid landingFluid
    ) {
    }

    public enum AnchorStatus {
        None,
        Uncharged,
        Charged
    }

    public enum LandingFluid {
        None,
        Water,
        Lava
    }
}
