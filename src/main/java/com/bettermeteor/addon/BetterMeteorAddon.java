package com.bettermeteor.addon;

import com.bettermeteor.addon.hud.EnderPearlInfoHud;
import com.bettermeteor.addon.modules.HandChams;
import com.bettermeteor.addon.modules.MurderMystery;
import com.bettermeteor.addon.modules.SignCommand;
import com.bettermeteor.addon.modules.HumanAutoTotem;
import com.bettermeteor.addon.modules.ItemNametags;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
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

        // Modules
        Modules.get().add(new SignCommand());
        Modules.get().add(new HumanAutoTotem());
        Modules.get().add(new ItemNametags());
        Modules.get().add(new HandChams());
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
