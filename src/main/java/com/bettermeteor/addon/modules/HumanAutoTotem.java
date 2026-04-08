package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.ThreadLocalRandom;

public class HumanAutoTotem extends Module {
    private static final PlayerActionC2SPacket SWAP_HANDS_PACKET = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.UP);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelay = settings.createGroup("Delay");

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
        .name("health")
        .description("Switch to a totem when your total health is at or below this value.")
        .defaultValue(10)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );

    private final Setting<Integer> healthMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("health-min-delay-ms")
        .description("Minimum delay in milliseconds before switching for low health.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> actionMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("action-min-delay-ms")
        .description("Minimum delay in milliseconds between swap actions such as hotbar switching and inventory handling.")
        .defaultValue(10)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> actionMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("action-max-delay-ms")
        .description("Maximum delay in milliseconds between swap actions such as hotbar switching and inventory handling.")
        .defaultValue(30)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> healthMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("health-max-delay-ms")
        .description("Maximum delay in milliseconds before switching for low health.")
        .defaultValue(180)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Boolean> elytra = sgGeneral.add(new BoolSetting.Builder()
        .name("elytra")
        .description("Switch to a totem when elytra collision speed could kill you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> elytraMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("elytra-min-delay-ms")
        .description("Minimum delay in milliseconds before switching for elytra danger.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> elytraMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("elytra-max-delay-ms")
        .description("Maximum delay in milliseconds before switching for elytra danger.")
        .defaultValue(180)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Boolean> riptide = sgGeneral.add(new BoolSetting.Builder()
        .name("riptide")
        .description("Switch to a totem when riptide collision speed could kill you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> riptideMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("riptide-min-delay-ms")
        .description("Minimum delay in milliseconds before switching for riptide danger.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> riptideMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("riptide-max-delay-ms")
        .description("Maximum delay in milliseconds before switching for riptide danger.")
        .defaultValue(180)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Boolean> fall = sgGeneral.add(new BoolSetting.Builder()
        .name("fall")
        .description("Switch to a totem when falling more than 23.5 blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fallMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("fall-min-delay-ms")
        .description("Minimum delay in milliseconds before switching for fall danger.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> fallMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("fall-max-delay-ms")
        .description("Maximum delay in milliseconds before switching for fall danger.")
        .defaultValue(180)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Boolean> explosion = sgGeneral.add(new BoolSetting.Builder()
        .name("explosion")
        .description("Switch to a totem when primed TNT, a charged anchor, or an end crystal is within range.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> explosionRange = sgGeneral.add(new IntSetting.Builder()
        .name("explosion-range")
        .description("Range in blocks for explosion danger checks.")
        .defaultValue(3)
        .range(1, 6)
        .sliderRange(1, 6)
        .build()
    );

    private final Setting<Integer> explosionMinDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("explosion-min-delay-ms")
        .description("Minimum delay in milliseconds before switching for explosion danger.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Integer> explosionMaxDelayMs = sgDelay.add(new IntSetting.Builder()
        .name("explosion-max-delay-ms")
        .description("Maximum delay in milliseconds before switching for explosion danger.")
        .defaultValue(180)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );

    private final Setting<Boolean> idlePause = sgGeneral.add(new BoolSetting.Builder()
        .name("idle-pause")
        .description("Pause auto totem when you have not provided any input for 30 seconds.")
        .defaultValue(false)
        .build()
    );

    public boolean locked;
    private int totems;
    private DangerType pendingDanger;
    private DesiredHand pendingHand;
    private long executeAtMs;
    private long lastInputAtMs;
    private ActionPlan actionPlan;
    private int actionStage;
    private long nextActionAtMs;

    public HumanAutoTotem() {
        super(BetterMeteorAddon.CATEGORY, "human-auto-totem", "Smart totem switching with separate danger checks and randomized millisecond delays.");
    }

    @Override
    public void onActivate() {
        lastInputAtMs = System.currentTimeMillis();
        resetPending();
        resetActionPlan();
    }

    @Override
    public void onDeactivate() {
        locked = false;
        resetPending();
        resetActionPlan();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (hasRecentInput()) lastInputAtMs = now;

        if (runActionPlan(now)) return;

        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();

        if (totems <= 0) {
            locked = false;
            resetPending();
            resetActionPlan();
            return;
        }

        if (idlePause.get() && now - lastInputAtMs >= 30_000L) {
            locked = false;
            resetPending();
            resetActionPlan();
            return;
        }

        Danger danger = getDanger();
        if (danger == null) {
            locked = false;
            resetPending();
            return;
        }

        locked = true;
        if (hasTotemIn(danger.hand)) {
            resetPending();
            return;
        }

        if (pendingDanger != danger.type || pendingHand != danger.hand) {
            pendingDanger = danger.type;
            pendingHand = danger.hand;
            executeAtMs = now + randomDelayMs(danger.type);
            return;
        }

        if (now < executeAtMs) return;

        startActionPlan(danger.hand, now);
        resetPending();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket packet)) return;
        if (packet.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) return;

        Entity entity = packet.getEntity(mc.world);
        if (entity == null || entity != mc.player) return;

        resetPending();
        resetActionPlan();
    }

    public boolean isLocked() {
        return isActive() && locked;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(totems);
    }

    private Danger getDanger() {
        if (explosion.get() && isExplosionDanger()) {
            return new Danger(DangerType.EXPLOSION, DesiredHand.OFFHAND);
        }

        if (fall.get() && mc.player.fallDistance > 23.5f) {
            return new Danger(DangerType.FALL, DesiredHand.OFFHAND);
        }

        if (elytra.get() && isElytraDanger()) {
            DesiredHand hand = mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET) ? DesiredHand.MAINHAND : DesiredHand.OFFHAND;
            return new Danger(DangerType.ELYTRA, hand);
        }

        if (riptide.get() && isRiptideDanger()) {
            return new Danger(DangerType.RIPTIDE, DesiredHand.OFFHAND);
        }

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.get()) {
            return new Danger(DangerType.HEALTH, DesiredHand.OFFHAND);
        }

        return null;
    }

    private boolean isExplosionDanger() {
        double range = explosionRange.get();
        double rangeSq = range * range;
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());

        for (Entity entity : mc.world.getEntities()) {
            boolean dangerous = entity instanceof EndCrystalEntity
                || entity instanceof TntEntity
                || entity instanceof TntMinecartEntity minecart && minecart.isPrimed();

            if (dangerous && entity.squaredDistanceTo(playerPos) <= rangeSq) return true;
        }

        BlockPos center = mc.player.getBlockPos();
        int radius = explosionRange.get();
        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (pos.getSquaredDistance(playerPos) > rangeSq) continue;

            BlockState state = mc.world.getBlockState(pos);
            if (state.isOf(Blocks.RESPAWN_ANCHOR) && state.contains(RespawnAnchorBlock.CHARGES) && state.get(RespawnAnchorBlock.CHARGES) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isElytraDanger() {
        boolean gliding = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && mc.player.isGliding();
        if (!gliding) return false;

        return isHighSpeedCollisionDanger();
    }

    private boolean isRiptideDanger() {
        boolean riptiding = mc.player.isUsingItem() && mc.player.getActiveItem().isOf(Items.TRIDENT);
        if (!riptiding) return false;

        return isHighSpeedCollisionDanger();
    }

    private boolean isHighSpeedCollisionDanger() {
        Vec3d velocity = mc.player.getVelocity();
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float predictedDamage = (float) (horizontalSpeed * 10.0 - 3.0);

        if (predictedDamage <= 0 || predictedDamage < mc.player.getHealth() + mc.player.getAbsorptionAmount()) return false;
        if (horizontalSpeed <= 0.1) return false;

        Vec3d start = mc.player.getEyePos();
        Vec3d end = start.add(velocity.normalize().multiply(Math.max(1.5, horizontalSpeed * 3.0)));
        HitResult hit = mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    private boolean hasTotemIn(DesiredHand hand) {
        return switch (hand) {
            case OFFHAND -> mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            case MAINHAND -> mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
        };
    }

    private int randomDelayMs(DangerType type) {
        int min;
        int max;

        switch (type) {
            case HEALTH -> {
                min = healthMinDelayMs.get();
                max = healthMaxDelayMs.get();
            }
            case ELYTRA -> {
                min = elytraMinDelayMs.get();
                max = elytraMaxDelayMs.get();
            }
            case RIPTIDE -> {
                min = riptideMinDelayMs.get();
                max = riptideMaxDelayMs.get();
            }
            case FALL -> {
                min = fallMinDelayMs.get();
                max = fallMaxDelayMs.get();
            }
            case EXPLOSION -> {
                min = explosionMinDelayMs.get();
                max = explosionMaxDelayMs.get();
            }
            default -> {
                min = 0;
                max = 0;
            }
        }

        int normalizedMin = Math.min(min, max);
        int normalizedMax = Math.max(min, max);
        return ThreadLocalRandom.current().nextInt(normalizedMin, normalizedMax + 1);
    }

    private void startActionPlan(DesiredHand hand, long now) {
        int hotbarTotemSlot = findHotbarTotemSlot();
        if (hotbarTotemSlot != -1) {
            int selectedSlot = mc.player.getInventory().getSelectedSlot();
            actionPlan = new ActionPlan(hand == DesiredHand.OFFHAND ? ActionPlanType.HOTBAR_TO_OFFHAND : ActionPlanType.HOTBAR_TO_MAINHAND, hotbarTotemSlot, selectedSlot);
            actionStage = 0;
            nextActionAtMs = now + randomActionDelayMs();
            return;
        }

        int inventoryTotemSlot = findInventoryTotemSlot();
        if (inventoryTotemSlot == -1) return;

        if (hand == DesiredHand.OFFHAND) {
            actionPlan = new ActionPlan(ActionPlanType.INVENTORY_TO_OFFHAND, inventoryTotemSlot, -1);
            actionStage = 0;
            nextActionAtMs = now + randomActionDelayMs();
            return;
        }

        int emptyHotbarSlot = findEmptyHotbarSlot();
        if (emptyHotbarSlot != -1) {
            actionPlan = new ActionPlan(ActionPlanType.INVENTORY_TO_MAINHAND, inventoryTotemSlot, emptyHotbarSlot);
            actionStage = 0;
            nextActionAtMs = now + randomActionDelayMs();
            return;
        }

        actionPlan = new ActionPlan(ActionPlanType.INVENTORY_TO_OFFHAND_FALLBACK, inventoryTotemSlot, -1);
        actionStage = 0;
        nextActionAtMs = now + randomActionDelayMs();
    }

    private boolean runActionPlan(long now) {
        if (actionPlan == null) return false;
        if (now < nextActionAtMs) return true;

        switch (actionPlan.type) {
            case HOTBAR_TO_MAINHAND -> {
                if (actionStage == 0) selectHotbarSlot(actionPlan.sourceSlot);
                else {
                    resetActionPlan();
                    return false;
                }
            }
            case HOTBAR_TO_OFFHAND -> {
                if (actionStage == 0) selectHotbarSlot(actionPlan.sourceSlot);
                else if (actionStage == 1) swapSelectedWithOffhand();
                else if (actionStage == 2) selectHotbarSlot(actionPlan.hotbarSlot);
                else {
                    resetActionPlan();
                    return false;
                }
            }
            case INVENTORY_TO_OFFHAND, INVENTORY_TO_OFFHAND_FALLBACK -> {
                if (actionStage == 0) openInventoryScreen();
                else if (actionStage == 1) swapInventorySlotWithOffhand(actionPlan.sourceSlot);
                else if (actionStage == 2) closeInventoryScreen();
                else if (actionStage > 2) {
                    resetActionPlan();
                    return false;
                }
            }
            case INVENTORY_TO_MAINHAND -> {
                if (actionStage == 1) InvUtils.move().from(actionPlan.sourceSlot).toHotbar(actionPlan.hotbarSlot);
                else if (actionStage == 3) selectHotbarSlot(actionPlan.hotbarSlot);
                else if (actionStage > 3) {
                    resetActionPlan();
                    return false;
                }
            }
        }

        actionStage++;
        if (isActionPlanComplete()) {
            resetActionPlan();
            return false;
        }

        nextActionAtMs = now + randomActionDelayMs();
        return true;
    }

    private boolean isActionPlanComplete() {
        return switch (actionPlan.type) {
            case HOTBAR_TO_MAINHAND -> actionStage > 0;
            case HOTBAR_TO_OFFHAND -> actionStage > 2;
            case INVENTORY_TO_OFFHAND, INVENTORY_TO_OFFHAND_FALLBACK -> actionStage > 2;
            case INVENTORY_TO_MAINHAND -> actionStage > 3;
        };
    }

    private int findHotbarTotemSlot() {
        for (int slot = SlotUtils.HOTBAR_START; slot <= SlotUtils.HOTBAR_END; slot++) {
            if (mc.player.getInventory().getStack(slot).isOf(Items.TOTEM_OF_UNDYING)) return slot;
        }

        return -1;
    }

    private int findInventoryTotemSlot() {
        for (int slot = SlotUtils.MAIN_START; slot <= SlotUtils.MAIN_END; slot++) {
            if (mc.player.getInventory().getStack(slot).isOf(Items.TOTEM_OF_UNDYING)) return slot;
        }

        return -1;
    }

    private int findEmptyHotbarSlot() {
        for (int slot = SlotUtils.HOTBAR_START; slot <= SlotUtils.HOTBAR_END; slot++) {
            if (mc.player.getInventory().getStack(slot).isEmpty()) return slot;
        }

        return -1;
    }

    private void selectHotbarSlot(int slot) {
        if (!SlotUtils.isHotbar(slot) || mc.player.getInventory().getSelectedSlot() == slot) return;

        mc.player.getInventory().setSelectedSlot(slot);
        ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
    }

    private void swapSelectedWithOffhand() {
        mc.getNetworkHandler().sendPacket(SWAP_HANDS_PACKET);
    }

    private void openInventoryScreen() {
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            mc.setScreen(new InventoryScreen(mc.player));
        }
    }

    private void closeInventoryScreen() {
        if (mc.currentScreen instanceof InventoryScreen) mc.setScreen(null);
    }

    private void swapInventorySlotWithOffhand(int slot) {
        int slotId = SlotUtils.indexToId(slot);
        if (slotId == -1) return;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, SlotUtils.OFFHAND, SlotActionType.SWAP, mc.player);
    }

    private int randomActionDelayMs() {
        int normalizedMin = Math.min(actionMinDelayMs.get(), actionMaxDelayMs.get());
        int normalizedMax = Math.max(actionMinDelayMs.get(), actionMaxDelayMs.get());
        return ThreadLocalRandom.current().nextInt(normalizedMin, normalizedMax + 1);
    }

    private boolean hasRecentInput() {
        return mc.options.forwardKey.isPressed()
            || mc.options.backKey.isPressed()
            || mc.options.leftKey.isPressed()
            || mc.options.rightKey.isPressed()
            || mc.options.jumpKey.isPressed()
            || mc.options.sneakKey.isPressed()
            || mc.options.sprintKey.isPressed()
            || mc.options.attackKey.isPressed()
            || mc.options.useKey.isPressed()
            || mc.options.pickItemKey.isPressed()
            || mc.options.inventoryKey.isPressed()
            || mc.options.swapHandsKey.isPressed()
            || mc.options.dropKey.isPressed();
    }

    private void resetPending() {
        pendingDanger = null;
        pendingHand = null;
        executeAtMs = 0;
    }

    private void resetActionPlan() {
        actionPlan = null;
        actionStage = 0;
        nextActionAtMs = 0;
    }

    private record Danger(DangerType type, DesiredHand hand) {}

    private record ActionPlan(ActionPlanType type, int sourceSlot, int hotbarSlot) {}

    private enum DangerType {
        HEALTH,
        ELYTRA,
        RIPTIDE,
        FALL,
        EXPLOSION
    }

    private enum DesiredHand {
        OFFHAND,
        MAINHAND
    }

    private enum ActionPlanType {
        HOTBAR_TO_MAINHAND,
        HOTBAR_TO_OFFHAND,
        INVENTORY_TO_OFFHAND,
        INVENTORY_TO_MAINHAND,
        INVENTORY_TO_OFFHAND_FALLBACK
    }
}
