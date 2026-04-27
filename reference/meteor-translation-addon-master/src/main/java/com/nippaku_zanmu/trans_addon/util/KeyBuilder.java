package com.nippaku_zanmu.trans_addon.util;

import meteordevelopment.meteorclient.systems.modules.Module;

public class KeyBuilder {
    StringBuilder sb = new StringBuilder();

    public KeyBuilder(Module m) {
        append("meteor")
            .append(TransUtil.getAddonName(m))
            .append(TransUtil.baseFormat(m.category.name))
            .append(TransUtil.baseFormat(m.name));
    }

    public KeyBuilder() {
        append("meteor");
    }

    public KeyBuilder reset() {
        sb = new StringBuilder();
        append("meteor");
        return this;
    }

    public KeyBuilder module(Module m) {
        append(TransUtil.getAddonName(m))
            .append(TransUtil.baseFormat(m.category.name))
            .append(TransUtil.baseFormat(m.name));
        return this;
    }

    public KeyBuilder append(String s) {
        sb.append(s).append(".");
        return this;
    }

    public KeyBuilder appendWithFormat(String s) {
        sb.append(TransUtil.baseFormat(s)).append(".");
        return this;
    }

    public String end(String s) {
        return sb.append(s).toString();
    }
    public String endWithFormat(String s) {
        return sb.append(TransUtil.baseFormat(s)).toString();
    }
}
