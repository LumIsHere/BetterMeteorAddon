package com.nippaku_zanmu.trans_addon.util.trans_engine;

import com.nippaku_zanmu.trans_addon.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public abstract class AbstractTransEngine implements IKeyGenerate {

    public String transModuleName(Module module){
        return TransUtil.trans(getModuleNameKey(module),module.name);
    }
    public String transModuleDescription(Module module){
        return TransUtil.trans(getModuleDescriptionKey(module),module.description);
    }
    public String transSettingName(Module module, SettingGroup group, Setting s){
        return TransUtil.trans(getSettingNameKey(module,group,s),s.name);
    }
    public String transSettingDes(Module module, SettingGroup group, Setting s){
        return TransUtil.trans(getSettingDesKey(module,group,s),s.description);
    }


}
