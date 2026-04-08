package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.base.CollectionMapSettingScreen;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.joml.Vector3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ItemNametags extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilter = settings.createGroup("Filter");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the nametags.")
        .defaultValue(1.1)
        .min(0.1)
        .build()
    );

    private final Setting<Double> maxRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-range")
        .description("Only render item nametags within this range.")
        .defaultValue(64)
        .min(0)
        .sliderMax(512)
        .build()
    );

    private final Setting<Boolean> showCount = sgGeneral.add(new BoolSetting.Builder()
        .name("show-count")
        .description("Displays the number of items in the stack.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> combineRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("combine-radius")
        .description("How close dropped items must be to share the same nametag plate.")
        .defaultValue(4)
        .min(0)
        .sliderMax(16)
        .build()
    );

    private final Setting<Boolean> showEnchantments = sgGeneral.add(new BoolSetting.Builder()
        .name("show-enchantments")
        .description("Appends matched enchantment levels after the item count.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> enchantNameLength = sgGeneral.add(new IntSetting.Builder()
        .name("enchant-name-length")
        .description("The length enchantment names are trimmed to.")
        .defaultValue(4)
        .range(1, 10)
        .sliderRange(1, 10)
        .visible(showEnchantments::get)
        .build()
    );

    private final Setting<ListMode> listMode = sgFilter.add(new EnumSetting.Builder<ListMode>()
        .name("list-mode")
        .description("How the selected items are treated.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Item>> whitelist = sgFilter.add(new ItemListSetting.Builder()
        .name("whitelist")
        .description("Only selected items will get nametags.")
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );

    private final Setting<List<Item>> blacklist = sgFilter.add(new ItemListSetting.Builder()
        .name("blacklist")
        .description("Selected items will not get nametags.")
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    private final Setting<ItemEnchantmentFilter> itemEnchantments = sgFilter.add(new ItemEnchantmentFilterSetting.Builder()
        .name("item-enchantments")
        .description("Configure per-item enchantment filters for items that can render nametags.")
        .defaultValue(new ItemEnchantmentFilter())
        .build()
    );

    private final Setting<SettingColor> background = sgRender.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The background color of the nametag.")
        .defaultValue(new SettingColor(0, 0, 0, 75))
        .build()
    );

    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder()
        .name("name-color")
        .description("The color of the item name.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> countColor = sgRender.add(new ColorSetting.Builder()
        .name("count-color")
        .description("The color of the stack size.")
        .defaultValue(new SettingColor(232, 185, 35))
        .visible(showCount::get)
        .build()
    );

    private final Setting<SettingColor> enchantmentColor = sgRender.add(new ColorSetting.Builder()
        .name("enchantment-color")
        .description("The color of appended enchantment text.")
        .defaultValue(new SettingColor(125, 170, 255))
        .visible(showEnchantments::get)
        .build()
    );

    private final List<ItemEntity> itemEntities = new ArrayList<>();
    private final List<RenderGroup> renderGroups = new ArrayList<>();
    private final List<RenderPlate> renderPlates = new ArrayList<>();
    private final Vector3d pos = new Vector3d();
    private final Vector3d groupPos = new Vector3d();

    public ItemNametags() {
        super(BetterMeteorAddon.CATEGORY, "item-nametags", "Displays nametags above selected dropped items.");
        ItemEnchantmentFilterSetting.registerWidgetFactory();
        bindItemEnchantments();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        itemEntities.clear();
        bindItemEnchantments();
        if (mc.world == null || mc.player == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        double maxRangeSq = maxRange.get() * maxRange.get();

        for (ItemEntity entity : mc.world.getEntitiesByClass(ItemEntity.class, mc.player.getBoundingBox().expand(maxRange.get()), itemEntity -> true)) {
            if (entity == null || entity.isRemoved()) continue;
            if (entity.squaredDistanceTo(mc.gameRenderer.getCamera().getCameraPos()) > maxRangeSq) continue;
            if (!shouldRender(entity.getStack())) continue;

            itemEntities.add(entity);
        }

        itemEntities.sort(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.gameRenderer.getCamera().getCameraPos())));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.world == null) return;

        boolean shadow = Config.get().customFont.get();
        buildRenderGroups(event.tickDelta);
        buildRenderPlates();

        for (int i = renderPlates.size() - 1; i >= 0; i--) {
            RenderPlate plate = renderPlates.get(i);
            pos.set(plate.x(), plate.y(), plate.z());

            if (NametagUtils.to2D(pos, scale.get())) renderPlate(plate, shadow);
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(renderPlates.size());
    }

    private boolean shouldRender(ItemStack stack) {
        if (!matchesItemFilter(stack.getItem())) return false;
        return itemEnchantments.get().matches(stack);
    }

    private boolean matchesItemFilter(Item item) {
        return switch (listMode.get()) {
            case Whitelist -> whitelist.get().contains(item);
            case Blacklist -> !blacklist.get().contains(item);
        };
    }

    private void buildRenderGroups(float tickDelta) {
        renderGroups.clear();

        for (ItemEntity entity : itemEntities) {
            ItemStack stack = entity.getStack();
            if (stack.isEmpty()) continue;

            Utils.set(groupPos, entity, tickDelta);
            groupPos.add(0, entity.getHeight() + 0.2, 0);

            RenderGroup group = findGroup(stack, groupPos);
            if (group == null) {
                renderGroups.add(new RenderGroup(stack.copy(), stack.getCount(), groupPos.x, groupPos.y, groupPos.z, 1));
            } else {
                group.add(stack.getCount(), groupPos.x, groupPos.y, groupPos.z);
            }
        }

        renderGroups.sort(Comparator.comparingDouble(group -> group.distanceTo(mc.gameRenderer.getCamera().getCameraPos().x, mc.gameRenderer.getCamera().getCameraPos().y, mc.gameRenderer.getCamera().getCameraPos().z)));
    }

    private void buildRenderPlates() {
        renderPlates.clear();

        for (RenderGroup group : renderGroups) {
            RenderPlate plate = findPlate(group);
            if (plate == null) {
                plate = new RenderPlate(group.x(), group.y(), group.z());
                renderPlates.add(plate);
            }

            plate.add(group);
        }

        for (RenderPlate plate : renderPlates) {
            plate.sortEntries();
        }

        renderPlates.sort(Comparator.comparingDouble(plate -> plate.distanceTo(mc.gameRenderer.getCamera().getCameraPos().x, mc.gameRenderer.getCamera().getCameraPos().y, mc.gameRenderer.getCamera().getCameraPos().z)));
    }

    private RenderGroup findGroup(ItemStack stack, Vector3d position) {
        double maxDistanceSq = combineRadius.get() * combineRadius.get();

        for (RenderGroup group : renderGroups) {
            if (!ItemStack.areItemsAndComponentsEqual(group.stack(), stack)) continue;
            if (group.distanceTo(position.x, position.y, position.z) > maxDistanceSq) continue;

            return group;
        }

        return null;
    }

    private RenderPlate findPlate(RenderGroup group) {
        double maxDistanceSq = combineRadius.get() * combineRadius.get();

        for (RenderPlate plate : renderPlates) {
            if (plate.distanceTo(group.x(), group.y(), group.z()) > maxDistanceSq) continue;
            return plate;
        }

        return null;
    }

    private void renderPlate(RenderPlate plate, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        double lineHeight = text.getHeight(shadow);
        double width = 0;
        for (RenderGroup group : plate.entries()) {
            String name = getDisplayName(group.stack());
            String countText = " x" + group.totalCount();
            String enchantText = getEnchantText(group.stack());
            double lineWidth = text.getWidth(name, shadow);
            if (showCount.get()) lineWidth += text.getWidth(countText, shadow);
            if (!enchantText.isEmpty()) lineWidth += text.getWidth(enchantText, shadow);
            width = Math.max(width, lineWidth);
        }

        double totalHeight = lineHeight * plate.entries().size();
        double halfWidth = width / 2;

        drawBackground(-halfWidth, -totalHeight, width, totalHeight);

        text.beginBig();
        double y = -totalHeight;

        for (RenderGroup group : plate.entries()) {
            String name = getDisplayName(group.stack());
            String countText = " x" + group.totalCount();
            String enchantText = getEnchantText(group.stack());
            double lineWidth = text.getWidth(name, shadow);
            if (showCount.get()) lineWidth += text.getWidth(countText, shadow);
            if (!enchantText.isEmpty()) lineWidth += text.getWidth(enchantText, shadow);

            double x = -(lineWidth / 2);
            x = text.render(name, x, y, nameColor.get(), shadow);
            if (showCount.get()) text.render(countText, x, y, countColor.get(), shadow);
            if (!enchantText.isEmpty()) text.render(enchantText, x + (showCount.get() ? text.getWidth(countText, shadow) : 0), y, enchantmentColor.get(), shadow);

            y += lineHeight;
        }
        text.end();

        NametagUtils.end();
    }

    private void drawBackground(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1, y - 1, width + 2, height + 2, background.get());
        Renderer2D.COLOR.render();
    }

    private String getEnchantText(ItemStack stack) {
        if (!showEnchantments.get()) return "";

        StringBuilder text = new StringBuilder();
        for (ItemEnchantmentFilter.EnchantmentMatch match : itemEnchantments.get().getMatchingEnchantments(stack)) {
            text.append(" ")
                .append(getEnchantmentShortName(match.enchantment()))
                .append(match.level());
        }

        return text.toString();
    }

    private String getEnchantmentShortName(RegistryKey<Enchantment> key) {
        return switch (key.getValue().getPath()) {
            case "protection" -> "P";
            case "blast_protection" -> "BP";
            case "projectile_protection" -> "PP";
            case "fire_protection" -> "FP";
            case "sharpness" -> "S";
            default -> {
                String name = Names.get(key);
                yield (name.length() > enchantNameLength.get() ? name.substring(0, enchantNameLength.get()) : name).toLowerCase(Locale.ROOT);
            }
        };
    }

    private void bindItemEnchantments() {
        itemEnchantments.get().bind(listMode::get, whitelist::get, blacklist::get);
    }

    private String getDisplayName(ItemStack stack) {
        String name = stack.getName().getString();
        name = stripFormatting(name).trim();
        return name.isEmpty() ? Names.get(stack.getItem()) : name;
    }

    private String stripFormatting(String text) {
        if (text == null || text.isEmpty()) return "";

        String stripped = StringHelper.stripTextFormat(text);
        if (stripped != null && !stripped.isEmpty()) return stripped;

        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }

            builder.append(c);
        }

        return builder.toString();
    }

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    private static final class ItemEnchantmentFilterSetting extends GenericSetting<ItemEnchantmentFilter> {
        private static boolean widgetFactoryRegistered;

        private ItemEnchantmentFilterSetting(String name, String description, ItemEnchantmentFilter defaultValue, java.util.function.Consumer<ItemEnchantmentFilter> onChanged, java.util.function.Consumer<Setting<ItemEnchantmentFilter>> onModuleActivated, meteordevelopment.meteorclient.settings.IVisible visible) {
            super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }

        private static void registerWidgetFactory() {
            if (widgetFactoryRegistered) return;
            widgetFactoryRegistered = true;

            SettingsWidgetFactory.registerCustomFactory(ItemEnchantmentFilterSetting.class, theme -> (table, setting) -> {
                WButton edit = table.add(theme.button("Edit")).expandCellX().widget();
                edit.action = () -> MeteorClient.mc.setScreen(((ItemEnchantmentFilterSetting) setting).createScreen(theme));

                WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
                reset.action = setting::reset;
                reset.tooltip = "Reset";
            });
        }

        private static final class Builder extends Setting.SettingBuilder<Builder, ItemEnchantmentFilter, ItemEnchantmentFilterSetting> {
            private Builder() {
                super(new ItemEnchantmentFilter());
            }

            @Override
            public ItemEnchantmentFilterSetting build() {
                return new ItemEnchantmentFilterSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
            }
        }
    }

    private static final class ItemEnchantmentFilter implements meteordevelopment.meteorclient.settings.IGeneric<ItemEnchantmentFilter> {
        private final Map<Item, ItemEnchantmentsConfig> configs = new LinkedHashMap<>();

        private transient Supplier<ListMode> listModeSupplier = () -> ListMode.Blacklist;
        private transient Supplier<List<Item>> whitelistSupplier = List::of;
        private transient Supplier<List<Item>> blacklistSupplier = List::of;

        private ItemEnchantmentFilter bind(Supplier<ListMode> listModeSupplier, Supplier<List<Item>> whitelistSupplier, Supplier<List<Item>> blacklistSupplier) {
            this.listModeSupplier = listModeSupplier;
            this.whitelistSupplier = whitelistSupplier;
            this.blacklistSupplier = blacklistSupplier;
            return this;
        }

        private boolean shouldIncludeItem(Item item) {
            if (item == Items.AIR) return false;

            return switch (listModeSupplier.get()) {
                case Whitelist -> whitelistSupplier.get().contains(item);
                case Blacklist -> !blacklistSupplier.get().contains(item);
            };
        }

        private boolean matches(ItemStack stack) {
            ItemEnchantmentsConfig config = configs.get(stack.getItem());
            return config == null || !config.hasRules() || config.matches(stack);
        }

        private List<EnchantmentMatch> getMatchingEnchantments(ItemStack stack) {
            ItemEnchantmentsConfig config = configs.get(stack.getItem());
            if (config == null || !config.hasRules()) return List.of();
            return config.getMatchingEnchantments(stack);
        }

        @Override
        public WidgetScreen createScreen(GuiTheme theme, GenericSetting<ItemEnchantmentFilter> setting) {
            return new ItemFilterScreen(theme, setting, this);
        }

        @Override
        public ItemEnchantmentFilter set(ItemEnchantmentFilter value) {
            configs.clear();
            value.configs.forEach((item, config) -> configs.put(item, config.copy()));
            bind(value.listModeSupplier, value.whitelistSupplier, value.blacklistSupplier);
            return this;
        }

        @Override
        public ItemEnchantmentFilter copy() {
            return new ItemEnchantmentFilter().set(this);
        }

        @Override
        public NbtCompound toTag() {
            NbtCompound tag = new NbtCompound();
            NbtList configsTag = new NbtList();

            for (Map.Entry<Item, ItemEnchantmentsConfig> entry : configs.entrySet()) {
                if (!entry.getValue().hasRules()) continue;

                NbtCompound configTag = new NbtCompound();
                configTag.putString("item", Registries.ITEM.getId(entry.getKey()).toString());
                configTag.put("filters", entry.getValue().toTag());
                configsTag.add(configTag);
            }

            tag.put("configs", configsTag);
            return tag;
        }

        @Override
        public ItemEnchantmentFilter fromTag(NbtCompound tag) {
            configs.clear();

            for (NbtElement element : tag.getListOrEmpty("configs")) {
                if (!(element instanceof NbtCompound configTag)) continue;

                Identifier id = Identifier.tryParse(configTag.getString("item", ""));
                if (id == null) continue;

                Item item = Registries.ITEM.get(id);
                if (item == Items.AIR) continue;

                ItemEnchantmentsConfig config = new ItemEnchantmentsConfig().fromTag(configTag.getCompoundOrEmpty("filters"));
                if (config.hasRules()) configs.put(item, config);
            }

            return this;
        }

        private ItemEnchantmentsConfig getOrCreate(Item item) {
            return configs.computeIfAbsent(item, ignored -> new ItemEnchantmentsConfig());
        }

        private void cleanupEmpty(Item item) {
            ItemEnchantmentsConfig config = configs.get(item);
            if (config != null && !config.hasRules()) configs.remove(item);
        }

        private record EnchantmentMatch(RegistryKey<Enchantment> enchantment, int level) {
        }

        private static final class ItemFilterScreen extends CollectionMapSettingScreen<Item, ItemEnchantmentsConfig> {
            private final GenericSetting<ItemEnchantmentFilter> setting;
            private final ItemEnchantmentFilter value;

            private ItemFilterScreen(GuiTheme theme, GenericSetting<ItemEnchantmentFilter> setting, ItemEnchantmentFilter value) {
                super(theme, "Item Enchantments", setting, value.configs, Registries.ITEM);
                this.setting = setting;
                this.value = value;
            }

            @Override
            protected boolean includeValue(Item item) {
                return value.shouldIncludeItem(item);
            }

            @Override
            protected WWidget getValueWidget(Item value) {
                return new WItemWithLabel(value.getDefaultStack(), Names.get(value));
            }

            @Override
            protected WWidget getDataWidget(Item item, @Nullable ItemEnchantmentsConfig data) {
                WHorizontalList list = theme.horizontalList();
                list.add(theme.label(data == null ? "No filter" : data.summary())).expandCellX();

                WButton edit = list.add(theme.button(GuiRenderer.EDIT)).widget();
                edit.action = () -> {
                    ItemEnchantmentsConfig config = value.getOrCreate(item);
                    ItemEnchantmentsScreen screen = new ItemEnchantmentsScreen(theme, setting, item, config);
                    screen.onClosed(() -> {
                        value.cleanupEmpty(item);
                        invalidateTable();
                    });
                    MeteorClient.mc.setScreen(screen);
                };

                return list;
            }

            @Override
            protected String[] getValueNames(Item value) {
                Identifier id = Registries.ITEM.getId(value);
                return new String[] { Names.get(value), id != null ? id.toString() : "" };
            }
        }

        private static final class ItemEnchantmentsScreen extends CollectionMapSettingScreen<RegistryKey<Enchantment>, String> {
            private final GenericSetting<ItemEnchantmentFilter> setting;

            private ItemEnchantmentsScreen(GuiTheme theme, GenericSetting<ItemEnchantmentFilter> setting, Item item, ItemEnchantmentsConfig config) {
                super(theme, Names.get(item), setting, config.levelTexts, getEnchantments());
                this.setting = setting;
            }

            @Override
            protected WWidget getValueWidget(RegistryKey<Enchantment> value) {
                return theme.label(Names.get(value));
            }

            @Override
            protected WWidget getDataWidget(RegistryKey<Enchantment> value, @Nullable String data) {
                String initial = data == null ? "" : data;
                WTextBox textBox = theme.textBox(initial, "1; 4", ItemEnchantmentsConfig::isAllowedCharacter, null);
                String[] lastValid = { initial };

                Runnable apply = () -> {
                    String current = textBox.get();
                    if (!ItemEnchantmentsConfig.isValidLevelsText(current)) {
                        textBox.set(lastValid[0]);
                        return;
                    }

                    String normalized = ItemEnchantmentsConfig.normalizeLevelsText(current);
                    lastValid[0] = normalized;

                    if (normalized.isEmpty()) map.remove(value);
                    else map.put(value, normalized);

                    setting.onChanged();
                };

                textBox.action = apply;
                textBox.actionOnUnfocused = apply;
                return textBox;
            }

            @Override
            protected String[] getValueNames(RegistryKey<Enchantment> value) {
                return new String[] { Names.get(value), value.getValue().toString() };
            }

            @Override
            protected void onClosed() {
                setting.onChanged();
            }

            private static Collection<RegistryKey<Enchantment>> getEnchantments() {
                ClientPlayNetworkHandler networkHandler = MeteorClient.mc.getNetworkHandler();
                if (networkHandler == null) return List.of();

                return networkHandler.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT)
                    .map(ItemEnchantmentsScreen::collectEnchantments)
                    .orElse(List.of());
            }

            private static List<RegistryKey<Enchantment>> collectEnchantments(Registry<Enchantment> registry) {
                List<RegistryKey<Enchantment>> keys = new ArrayList<>();
                registry.streamEntries().forEach(entry -> entry.getKey().ifPresent(keys::add));
                keys.sort(Comparator.comparing(Names::get));
                return keys;
            }
        }

        private static final class ItemEnchantmentsConfig implements IChangeable {
            private final Map<RegistryKey<Enchantment>, String> levelTexts = new LinkedHashMap<>();

            private boolean hasRules() {
                return !levelTexts.isEmpty();
            }

            private List<EnchantmentMatch> getMatchingEnchantments(ItemStack stack) {
                if (stack.isEmpty() || levelTexts.isEmpty()) return List.of();

                Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
                Utils.getEnchantments(stack, enchantments);

                List<EnchantmentMatch> matches = new ArrayList<>();
                for (Map.Entry<RegistryKey<Enchantment>, String> entry : levelTexts.entrySet()) {
                    int level = Utils.getEnchantmentLevel(enchantments, entry.getKey());
                    if (level <= 0) continue;
                    if (parseLevels(entry.getValue()).contains(level)) matches.add(new EnchantmentMatch(entry.getKey(), level));
                }

                matches.sort(Comparator.comparing(match -> Names.get(match.enchantment())));
                return matches;
            }

            private boolean matches(ItemStack stack) {
                return !getMatchingEnchantments(stack).isEmpty();
            }

            private String summary() {
                int size = levelTexts.size();
                return size == 1 ? "1 enchantment filter" : size + " enchantment filters";
            }

            private NbtCompound toTag() {
                NbtCompound tag = new NbtCompound();
                NbtList entries = new NbtList();

                for (Map.Entry<RegistryKey<Enchantment>, String> entry : levelTexts.entrySet()) {
                    NbtCompound entryTag = new NbtCompound();
                    entryTag.putString("enchantment", entry.getKey().getValue().toString());
                    entryTag.putString("levels", entry.getValue());
                    entries.add(entryTag);
                }

                tag.put("entries", entries);
                return tag;
            }

            private ItemEnchantmentsConfig fromTag(NbtCompound tag) {
                levelTexts.clear();

                for (NbtElement element : tag.getListOrEmpty("entries")) {
                    if (!(element instanceof NbtCompound entryTag)) continue;

                    Identifier id = Identifier.tryParse(entryTag.getString("enchantment", ""));
                    if (id == null) continue;

                    String levels = normalizeLevelsText(entryTag.getString("levels", ""));
                    if (levels.isEmpty()) continue;

                    levelTexts.put(RegistryKey.of(RegistryKeys.ENCHANTMENT, id), levels);
                }

                return this;
            }

            private ItemEnchantmentsConfig copy() {
                ItemEnchantmentsConfig copy = new ItemEnchantmentsConfig();
                copy.levelTexts.putAll(levelTexts);
                return copy;
            }

            @Override
            public boolean isChanged() {
                return hasRules();
            }

            private static boolean isAllowedCharacter(String text, char c) {
                return Character.isDigit(c) || c == ';' || Character.isWhitespace(c);
            }

            private static boolean isValidLevelsText(String text) {
                if (text == null || text.trim().isEmpty()) return true;

                String[] parts = text.split(";");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) return false;

                    try {
                        int level = Integer.parseInt(trimmed);
                        if (level < 1 || level > 255) return false;
                    } catch (NumberFormatException ignored) {
                        return false;
                    }
                }

                return true;
            }

            private static String normalizeLevelsText(String text) {
                if (text == null || text.trim().isEmpty()) return "";

                Set<Integer> levels = parseLevels(text);
                if (levels.isEmpty()) return "";

                StringBuilder builder = new StringBuilder();
                for (int level : levels) {
                    if (!builder.isEmpty()) builder.append("; ");
                    builder.append(level);
                }

                return builder.toString();
            }

            private static Set<Integer> parseLevels(String text) {
                Set<Integer> levels = new TreeSet<>();
                if (!isValidLevelsText(text)) return levels;

                for (String part : text.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    levels.add(Integer.parseInt(trimmed));
                }

                return levels;
            }
        }
    }

    private static final class RenderGroup {
        private final ItemStack stack;
        private int totalCount;
        private double x;
        private double y;
        private double z;
        private int size;

        private RenderGroup(ItemStack stack, int totalCount, double x, double y, double z, int size) {
            this.stack = stack;
            this.totalCount = totalCount;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        private void add(int count, double x, double y, double z) {
            totalCount += count;

            size++;
            this.x += (x - this.x) / size;
            this.y += (y - this.y) / size;
            this.z += (z - this.z) / size;
        }

        private double distanceTo(double x, double y, double z) {
            double dX = this.x - x;
            double dY = this.y - y;
            double dZ = this.z - z;
            return dX * dX + dY * dY + dZ * dZ;
        }

        private ItemStack stack() {
            return stack;
        }

        private int totalCount() {
            return totalCount;
        }

        private double x() {
            return x;
        }

        private double y() {
            return y;
        }

        private double z() {
            return z;
        }
    }

    private static final class RenderPlate {
        private final List<RenderGroup> entries = new ArrayList<>();
        private double x;
        private double y;
        private double z;
        private int size;

        private RenderPlate(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void add(RenderGroup group) {
            entries.add(group);

            size++;
            x += (group.x() - x) / size;
            y += (group.y() - y) / size;
            z += (group.z() - z) / size;
        }

        private void sortEntries() {
            entries.sort(
                Comparator.comparing((RenderGroup group) -> Names.get(group.stack()))
                    .thenComparing(Comparator.comparingInt(RenderGroup::totalCount).reversed())
            );
        }

        private double distanceTo(double x, double y, double z) {
            double dX = this.x - x;
            double dY = this.y - y;
            double dZ = this.z - z;
            return dX * dX + dY * dY + dZ * dZ;
        }

        private List<RenderGroup> entries() {
            return entries;
        }

        private double x() {
            return x;
        }

        private double y() {
            return y;
        }

        private double z() {
            return z;
        }
    }
}
