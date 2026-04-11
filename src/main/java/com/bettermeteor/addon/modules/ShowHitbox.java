package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.Entity;

import java.util.Set;

import net.minecraft.entity.EntityType;

public class ShowHitbox extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Choose exactly which entity hitboxes to show.")
        .defaultValue(EntityType.PLAYER, EntityType.END_CRYSTAL)
        .build()
    );

    public ShowHitbox() {
        super(Categories.Render, "show-hitbox", "Uses vanilla Minecraft hitboxes for only the entity types you choose.");
    }

    public boolean shouldRender(Entity entity) {
        return entities.get().contains(entity.getType());
    }
}
