package com.bettermeteor.addon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Nametags.class)
public abstract class NametagsMixin {
    @Shadow @Final private SettingGroup sgRender;

    @Unique
    private Setting<Boolean> bettermeteor$usePlayerNameColor;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bettermeteor$addNameColorModeSetting(CallbackInfo ci) {
        bettermeteor$usePlayerNameColor = sgRender.add(new BoolSetting.Builder()
            .name("use-player-name-color")
            .description("Uses the player's name color instead of keeping names white.")
            .defaultValue(true)
            .build()
        );
    }

    @WrapOperation(
        method = "renderNametagPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/utils/player/PlayerUtils;getPlayerColor(Lnet/minecraft/entity/player/PlayerEntity;Lmeteordevelopment/meteorclient/utils/render/color/SettingColor;)Lmeteordevelopment/meteorclient/utils/render/color/Color;"
        )
    )
    private Color bettermeteor$maybeKeepNamesWhite(PlayerEntity player, SettingColor fallbackColor, Operation<Color> original) {
        if (bettermeteor$usePlayerNameColor != null && bettermeteor$usePlayerNameColor.get()) {
            return original.call(player, fallbackColor);
        }

        return Color.WHITE;
    }
}
