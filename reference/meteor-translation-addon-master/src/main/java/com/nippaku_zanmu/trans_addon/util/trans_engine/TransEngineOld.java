package com.nippaku_zanmu.trans_addon.util.trans_engine;

import com.nippaku_zanmu.trans_addon.util.KeyBuilder;
import com.nippaku_zanmu.trans_addon.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;

public  class TransEngineOld extends AbstractTransEngine {
    KeyBuilder builder = new KeyBuilder();
    @Override
    public String getModuleNameKey(Module module) {
        String moduleName = module.name;
        builder.reset();
        String key = builder.append(TransUtil.getAddonName(module))
            .append(TransUtil.baseFormat(module.category.name))
            .end(TransUtil.baseFormat(moduleName));
        return key;
    }

    @Override
    public String getModuleDescriptionKey(Module module) {
        builder.reset();
        String key = builder.module(module).end("description");
        return  key;
    }

    @Override
    public String getSettingNameKey(Module module, SettingGroup group, Setting s) {
        String settingName = s.name;
        builder.reset();
        String key = builder.module(module)
            .append("setting")
            .append(group.name)
            .end(settingName)
            ;
        return key;
    }

    @Override
    public String getSettingDesKey(Module module, SettingGroup group, Setting s) {
        String settingName = s.name;
        builder.reset();
        String key = builder.module(module)
            .append("setting")
            .append(group.name)
            .append(settingName)
            .end("description")
            ;
        return  key;
    }
}
