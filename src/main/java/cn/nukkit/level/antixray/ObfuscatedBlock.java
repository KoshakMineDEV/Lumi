package cn.nukkit.level.antixray;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObfuscatedBlock extends OkaeriConfig {
    @CustomKey("originalBlock")
    private String originalBlock;

    @CustomKey("fakeBlocks")
    private List<String> fakeBlocks;
}