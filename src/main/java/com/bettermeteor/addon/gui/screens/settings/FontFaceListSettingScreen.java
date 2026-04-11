package com.bettermeteor.addon.gui.screens.settings;

import com.bettermeteor.addon.utils.font.FontFaceListSetting;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.systems.config.Config;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.List;

public class FontFaceListSettingScreen extends WindowScreen {
    private final FontFaceListSetting setting;
    private final List<FontFace> allFonts = new ArrayList<>();

    private WTable table;
    private String filterText = "";

    public FontFaceListSettingScreen(GuiTheme theme, FontFaceListSetting setting) {
        super(theme, "Select Fallback Fonts");

        this.setting = setting;

        for (FontFamily family : Fonts.FONT_FAMILIES) {
            for (FontInfo.Type type : FontInfo.Type.values()) {
                FontFace face = family.get(type);
                if (face != null) allFonts.add(face);
            }
        }
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();
            table.clear();
            initTable();
        };

        window.view.hasScrollBar = false;

        WView view = add(theme.view()).expandX().widget();
        view.maxHeight = window.view.maxHeight - 128;
        view.scrollOnlyWhenMouseOver = false;

        table = view.add(theme.table()).expandX().widget();
        initTable();
    }

    private void initTable() {
        WTable left = createAvailableTable();
        if (Config.get().syncListSettingWidths.get() || !left.cells.isEmpty()) {
            table.add(theme.verticalSeparator()).expandWidgetY();
        }
        createSelectedTable();
    }

    private WTable createAvailableTable() {
        Cell<WTable> cell = table.add(theme.table()).top();
        if (Config.get().syncListSettingWidths.get()) cell.group("sync-width");
        WTable left = cell.widget();

        for (FontFace face : allFonts) {
            if (setting.get().contains(face) || !include(face)) continue;

            left.add(createFontLabel(face));

            WButton add = left.add(theme.button("Add")).expandCellX().right().widget();
            add.action = () -> {
                setting.get().add(face);
                setting.onChanged();
                refresh();
            };

            left.row();
        }

        if (!left.cells.isEmpty()) cell.expandX();
        return left;
    }

    private void createSelectedTable() {
        Cell<WTable> cell = table.add(theme.table()).top();
        if (Config.get().syncListSettingWidths.get()) cell.group("sync-width");
        WTable right = cell.widget();

        List<FontFace> selected = setting.get();
        for (int i = 0; i < selected.size(); i++) {
            int index = i;
            FontFace face = selected.get(i);
            if (!include(face)) continue;

            right.add(createFontLabel(face));

            WButton up = right.add(theme.button("↑")).widget();
            up.action = () -> move(index, -1);
            up.tooltip = "Move Up";

            WButton down = right.add(theme.button("↓")).widget();
            down.action = () -> move(index, 1);
            down.tooltip = "Move Down";

            WButton remove = right.add(theme.button("Remove")).expandCellX().right().widget();
            remove.action = () -> {
                selected.remove(index);
                setting.onChanged();
                refresh();
            };

            right.row();
        }

        if (!right.cells.isEmpty()) cell.expandX();
    }

    private void move(int index, int direction) {
        int target = index + direction;
        if (target < 0 || target >= setting.get().size()) return;

        FontFace face = setting.get().remove(index);
        setting.get().add(target, face);
        setting.onChanged();
        refresh();
    }

    private boolean include(FontFace face) {
        return filterText.isEmpty() || Strings.CI.contains(getSearchText(face), filterText);
    }

    private WWidget createFontLabel(FontFace face) {
        return theme.label(face.info.family() + " - " + face.info.type());
    }

    private String getSearchText(FontFace face) {
        return face.info.family() + " " + face.info.type();
    }

    private void refresh() {
        table.clear();
        initTable();
    }
}
