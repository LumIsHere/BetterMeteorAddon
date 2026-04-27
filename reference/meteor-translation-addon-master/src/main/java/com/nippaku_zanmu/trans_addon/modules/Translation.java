package com.nippaku_zanmu.trans_addon.modules;


import com.nippaku_zanmu.trans_addon.MeteorTranslation;
import com.nippaku_zanmu.trans_addon.settings.StringSelectSetting;
import com.nippaku_zanmu.trans_addon.util.JsonDump;
import com.nippaku_zanmu.trans_addon.util.TransUtil;
import com.nippaku_zanmu.trans_addon.mixin.ModuleAccessor;
import com.nippaku_zanmu.trans_addon.mixin.SettingAccessor;
import com.nippaku_zanmu.trans_addon.mixin.SettingGroupAccessor;
import com.nippaku_zanmu.trans_addon.util.trans_engine.AbstractTransEngine;
import com.nippaku_zanmu.trans_addon.util.trans_engine.EngineManager;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.Set;

public class Translation extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public final Setting<Boolean> bSetAutoTranslation = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-translation")
        .description("")
        .defaultValue(false)
        .build());

    public final Setting<Set<String>> translationModules = sgGeneral.add(new StringSelectSetting.Builder()
        .validValues(TransUtil.getAddonNames())
        .defaultValue(TransUtil.getAddonNames())
        .name("translation-modules")
        .build());

    private final SettingGroup sgDev = this.settings.createGroup("Dev", false);

    public final Setting<String> strSetTransEngine = sgDev.add(new StringSetting.Builder()
        .defaultValue("NEW")
        .name("translation-engine")
        .build());


    public final Setting<String> sSetDumpPath = sgDev.add(new StringSetting.Builder()
        .name("dump-path")
        .defaultValue("D:\\hack\\Misc\\meteor-translation-addon\\test\\en_us.json")
        .build());

    public final Setting<Boolean> bSetDumpText = sgDev.add(new BoolSetting.Builder()
        .name("dump-text")
        .defaultValue(false)
        .build()
    );
    public final Setting<String> strSetDumpTextEngine = sgDev.add(new StringSetting.Builder()
        .defaultValue("OLD")
        .visible(bSetDumpText::get)
        .name("dump-text-engine")
        .build());


    public Translation() {
        super(MeteorTranslation.CATEGORY, "meteor-trans", "An example module that highlights the center of the world.");
    }

    private boolean isTranslation = false;

    @Override
    public void onActivate() {
        if (bSetAutoTranslation.get() && !isTranslation) {
            isTranslation = true;
            tran();
        }

        ChatUtils.warning("流星翻译插件是开源的项目且完全免费 作者不会以任何形式对此插件进行收费");
        ChatUtils.warning("如果你购买了此插件 则说明你被骗了");
    }


    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();

        WHorizontalList l1 = list.add(theme.horizontalList()).expandX().widget();

        WButton start = l1.add(theme.button("Translate")).expandX().widget();
        start.action = () -> {
            if (this.isActive()) {
                isTranslation = true;
                tran();
            } else {
                ChatUtils.warning("你首先要开启此模块");
            }
        };

        if (!sgDev.sectionExpanded)
            return list;
        WHorizontalList l2 = list.add(theme.horizontalList()).expandX().widget();
        WButton dump = l2.add(theme.button("Dump")).expandX().widget();
        dump.action = () -> {
            JsonDump.getINSTANCE().write(EngineManager.getInstance().getEngine(strSetTransEngine.get()), EngineManager.getInstance().getEngine(strSetDumpTextEngine.get()));
        };
        return list;
    }


    public void tran() {
        tran(EngineManager.getInstance().getEngine(strSetTransEngine.get()));
    }


    private void tran(AbstractTransEngine engine) {
        for (Module module : Modules.get().getAll()) {
            String addonName = TransUtil.getAddonName(module);
            if (!translationModules.get().contains(addonName)) continue;
            //插件过滤

            String tranName = engine.transModuleName(module);
            // 经过翻译的名称
            ((ModuleAccessor) module).setTitle(Utils.nameToTitle(tranName));
            //把标题设为翻译之后的名称

            String tranDescry = engine.transModuleDescription(module);
            ((ModuleAccessor) module).setDescription(Utils.nameToTitle(tranDescry));
            //翻译简介

            for (SettingGroup group : module.settings.groups) {
                for (Setting<?> setting : ((SettingGroupAccessor) group).getSettings()) {

                    String tranSettName = engine.transSettingName(module, group, setting);
                    ((SettingAccessor) setting).setTitle(Utils.nameToTitle(tranSettName));

                    String tranSettDesc = engine.transSettingDes(module, group, setting);
                    ((SettingAccessor) setting).setDescription(Utils.nameToTitle(tranSettDesc));
                }
            }

        }
    }
}
