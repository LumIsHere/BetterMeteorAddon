package com.nippaku_zanmu.trans_addon.util.trans_engine;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public interface IKeyGenerate {
    String getModuleNameKey(Module m);
    String getModuleDescriptionKey(Module m);
    String getSettingNameKey(Module module, SettingGroup group, Setting<?> s);
    String getSettingDesKey(Module module, SettingGroup group, Setting<?> s);
}
