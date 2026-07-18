package dev.ftb.mods.ftbbackups.config;

import dev.ftb.mods.ftbbackups.api.retention.RetentionRule;
import dev.ftb.mods.ftbbackups.retention.RetentionParser;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RetentionRuleListValue extends BaseValue<List<RetentionRule>> {
    public RetentionRuleListValue(@Nullable SNBTConfig config, String key, List<RetentionRule> def) {
        super(config, key, def);
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: " + defaultValue);
        tag.comment(key, String.join("\n", s));

        ListTag values = new ListTag();
        for (RetentionRule rule : get()) {
            values.add(StringTag.valueOf(rule.asString()));
        }
        tag.put(key, values);
    }

    @Override
    public void read(SNBTCompoundTag snbtCompoundTag) {
        List<RetentionRule> rules = new ArrayList<>();
        ListTag values = snbtCompoundTag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < values.size(); i++) {
            rules.add(RetentionParser.parse(values.getString(i)));
        }
        set(rules);
    }
}
