package cn.nukkit.block.material.tags.impl;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.BlockTypes;
import cn.nukkit.block.material.tags.BlockTag;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@EqualsAndHashCode
@ToString
public class LazyBlockTag implements BlockTag {

    public static final Map<String, Set<String>> VANILLA_DEFINITIONS;

    private final String tag;
    private Set<BlockType> blockTypes;

    public LazyBlockTag(String tag) {
        this.tag = tag;
    }

    private Set<BlockType> load() {
        Set<String> definitions = VANILLA_DEFINITIONS.get(tag);
        if (definitions == null) {
            throw new IllegalStateException("Unknown vanilla tag: " + this.tag);
        }

        Set<BlockType> blockTypes = new ObjectOpenHashSet<>();
        for (String definition : definitions) {
            BlockType material = BlockTypes.get(definition);
            if (material != null) {
                blockTypes.add(material);
            }
        }

        return blockTypes;
    }

    @Override
    public Set<BlockType> getBlockTypes() {
        if (this.blockTypes == null) {
            this.blockTypes = this.load();
        }
        return this.blockTypes;
    }

    static {
        Map<String, Set<String>> parsed = Collections.emptyMap();
        var stream = LazyBlockTag.class.getClassLoader().getResourceAsStream("block_tags.json");
        if (stream != null) {
            try (var reader = new InputStreamReader(stream)) {
                var type = new TypeToken<Map<String, Set<String>>>() {}.getType();
                parsed = new Gson().fromJson(reader, type);
            } catch (Exception e) {
                log.error("Failed to load vanilla block tags", e);
            }
        }
        VANILLA_DEFINITIONS = parsed;
    }
}
