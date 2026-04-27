package com.nippaku_zanmu.trans_addon.util.trans_engine;

import com.nippaku_zanmu.trans_addon.util.KeyBuilder;
import com.nippaku_zanmu.trans_addon.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TransEngineNew extends AbstractTransEngine {
    KeyBuilder builder = new KeyBuilder();

    @Override
    public String getModuleNameKey(Module module) {
        builder.reset();
        String key = builder.module(module).end("name");
        return key;
    }

    @Override
    public String getModuleDescriptionKey(Module module) {
        builder.reset();
        String key = builder.module(module).end("description");
        return key;
    }

    @Override
    public String getSettingNameKey(Module module, SettingGroup group, Setting<?> s) {
        String settingName = s.name;
        builder.reset();
        String key = builder.module(module)
            .appendWithFormat("setting")
            .appendWithFormat(group.name)
            .appendWithFormat(settingName)
            .end("name");
        return key;
    }

    @Override
    public String getSettingDesKey(Module module, SettingGroup group, Setting<?> s) {
        String settingName = s.name;
        builder.reset();
        String key = builder.module(module)
            .appendWithFormat("setting")
            .appendWithFormat(group.name)
            .appendWithFormat(settingName)
            .end("description");
        return key;
    }
}
