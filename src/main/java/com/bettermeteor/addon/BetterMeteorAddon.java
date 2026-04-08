package com.bettermeteor.addon;

import com.bettermeteor.addon.commands.CommandExample;
import com.bettermeteor.addon.modules.SignCommand;
import com.bettermeteor.addon.hud.HudExample;
import com.bettermeteor.addon.modules.EnderPearlTimer;
import com.bettermeteor.addon.modules.HumanAutoTotem;
import com.bettermeteor.addon.modules.ItemNametags;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class BetterMeteorAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("BetterMeteor");
    public static final HudGroup HUD_GROUP = new HudGroup("BetterMeteor");

    @Override
    public void onInitialize() {
        LOG.info("Initializing BetterMeteor");

        // Modules
        Modules.get().add(new SignCommand());
        Modules.get().add(new EnderPearlTimer());
        Modules.get().add(new HumanAutoTotem());
        Modules.get().add(new ItemNametags());

        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
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
