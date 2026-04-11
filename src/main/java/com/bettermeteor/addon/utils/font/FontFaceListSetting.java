package com.bettermeteor.addon.utils.font;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FontFaceListSetting extends Setting<List<FontFace>> {
    public FontFaceListSetting(String name, String description, List<FontFace> defaultValue, Consumer<List<FontFace>> onChanged, Consumer<Setting<List<FontFace>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<FontFace> parseImpl(String str) {
        List<FontFace> faces = new ArrayList<>();
        for (String part : str.split(",")) {
            FontFace face = FontFaceParser.parse(part.trim());
            if (face != null) faces.add(face);
        }
        return faces;
    }

    @Override
    protected boolean isValueValid(List<FontFace> value) {
        return value != null;
    }

    @Override
    protected void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    @Override
    public List<String> getSuggestions() {
        return List.of("JetBrainsMono-Regular", "PingFangSC-Regular");
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();

        for (FontFace face : get()) {
            NbtCompound faceTag = new NbtCompound();
            faceTag.putString("family", face.info.family());
            faceTag.putString("type", face.info.type().toString());
            valueTag.add(faceTag);
        }

        tag.put("value", valueTag);
        return tag;
    }

    @Override
    protected List<FontFace> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getListOrEmpty("value");
        for (NbtElement element : valueTag) {
            if (!(element instanceof NbtCompound faceTag)) continue;

            FontFace face = FontFaceParser.parse(faceTag.getString("family", "") + "-" + faceTag.getString("type", ""));
            if (face != null && !get().contains(face)) get().add(face);
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<FontFace>, FontFaceListSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(FontFace... defaults) {
            List<FontFace> values = new ArrayList<>();
            if (defaults != null) {
                for (FontFace face : defaults) {
                    if (face != null) values.add(face);
                }
            }

            return defaultValue(values);
        }

        @Override
        public FontFaceListSetting build() {
            return new FontFaceListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
