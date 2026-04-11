package com.bettermeteor.addon;

import com.bettermeteor.addon.gui.screens.settings.FontFaceListSettingScreen;
import com.bettermeteor.addon.hud.EnderPearlInfoHud;
import com.bettermeteor.addon.modules.Crystal;
import com.bettermeteor.addon.modules.HandChams;
import com.bettermeteor.addon.modules.MurderMystery;
import com.bettermeteor.addon.modules.ShowHitbox;
import com.bettermeteor.addon.modules.SignCommand;
import com.bettermeteor.addon.modules.HumanAutoTotem;
import com.bettermeteor.addon.modules.ItemNametags;
import com.bettermeteor.addon.utils.font.FontFaceListSetting;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class BetterMeteorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final HudGroup HUD_GROUP = new HudGroup("BetterMeteor");
    public static final Category MINIGAMES = new Category("Minigames", Items.NETHER_STAR.getDefaultStack());

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MINIGAMES);
    }

    @Override
    public void onInitialize() {
        LOG.info("Initializing BetterMeteor");

        SettingsWidgetFactory.registerCustomFactory(FontFaceListSetting.class, theme -> (table, setting) -> {
            FontFaceListSetting fontSetting = (FontFaceListSetting) setting;

            WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();
            WLabel label = list.add(theme.label("(" + fontSetting.get().size() + " selected)")).widget();

            list.add(theme.button("Select")).expandCellX().widget().action = () -> {
                FontFaceListSettingScreen screen = new FontFaceListSettingScreen(theme, fontSetting);
                screen.onClosed(() -> label.set("(" + fontSetting.get().size() + " selected)"));
                MinecraftClient.getInstance().setScreen(screen);
            };

            list.add(theme.button("Reset")).widget().action = () -> {
                fontSetting.reset();
                label.set("(" + fontSetting.get().size() + " selected)");
            };
        });

        // Modules
        Modules.get().add(new SignCommand());
        Modules.get().add(new HumanAutoTotem());
        Modules.get().add(new Crystal());
        Modules.get().add(new ItemNametags());
        Modules.get().add(new HandChams());
        Modules.get().add(new ShowHitbox());
        Modules.get().add(new MurderMystery());

        // HUD
        Hud.get().register(EnderPearlInfoHud.INFO);
    }

    @Override
    public String getPackage() {
        return "com.bettermeteor.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("bettermeteor", "bettermeteor");
    }
}
