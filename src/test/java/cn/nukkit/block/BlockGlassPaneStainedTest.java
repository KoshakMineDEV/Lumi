package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.data.DyeColor;
import cn.nukkit.level.BlockPalette;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.registry.Registries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockGlassPaneStainedTest {

    @BeforeAll
    static void loadBlockToItemMappings() {
        Registries.BLOCK_TO_ITEM.init();
    }

    @Test
    void stainedPaneBaseIsAbstract() {
        assertTrue(Modifier.isAbstract(BlockGlassPaneStained.class.getModifiers()));
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("coloredPanes")
    void coloredPanesHaveIndependentIdsAndConnectionMeta(
            Class<? extends BlockGlassPaneStained> blockClass,
            int expectedId,
            String expectedIdentifier,
            DyeColor expectedColor
    ) throws ReflectiveOperationException {
        Constructor<? extends BlockGlassPaneStained> constructor = blockClass.getDeclaredConstructor(int.class);

        for (int meta = 0; meta < 16; meta++) {
            BlockGlassPaneStained pane = constructor.newInstance(meta);

            assertEquals(expectedId, pane.getId());
            assertEquals(expectedIdentifier, pane.getIdentifier());
            assertEquals(expectedColor, pane.getDyeColor());
            assertEquals(expectedColor.getName() + " Stained Glass Pane", pane.getName());
            assertEquals(expectedColor.getBlockColor(), pane.getColor());
            assertEquals(meta, pane.getDamage());
            assertEquals(expectedId << Block.DATA_BITS | meta, pane.getFullId());
            assertEquals((meta & 1) != 0, pane.getBooleanValue("minecraft:connection_west"));
            assertEquals((meta & 2) != 0, pane.getBooleanValue("minecraft:connection_south"));
            assertEquals((meta & 4) != 0, pane.getBooleanValue("minecraft:connection_north"));
            assertEquals((meta & 8) != 0, pane.getBooleanValue("minecraft:connection_east"));

            Item item = pane.toItem();
            assertEquals(pane.getItemId(), item.getId());
            assertEquals(0, item.getDamage());
            assertEquals(meta, pane.getDamage(), "toItem must not mutate the placed block state");
        }
    }

    @ParameterizedTest(name = "palette {0}")
    @MethodSource("supportedProtocols")
    void everyProtocolPaletteContainsAllColoredPaneMetas(int protocol) {
        BlockPalette palette = GlobalBlockPalette.getPaletteByProtocol(protocol);

        for (ArgumentsData pane : paneData()) {
            Set<Integer> hashes = new HashSet<>();
            for (int meta = 0; meta < 16; meta++) {
                int fullId = pane.id() << Block.DATA_BITS | meta;
                assertTrue(palette.getLegacyToHashIdMap().containsKey(fullId),
                        () -> pane.identifier() + ':' + (fullId & Block.DATA_MASK) + " is missing in protocol " + protocol);
                hashes.add(palette.getLegacyToHashIdMap().get(fullId));
            }

            int expectedHashCount = protocol >= ProtocolInfo.v1_26_50 ? 16 : 1;
            assertEquals(expectedHashCount, hashes.size(),
                    pane.identifier() + " has an unexpected state layout in protocol " + protocol);
        }
    }

    @Test
    void registryAndCopperBarsPreserveConnectionMeta() {
        Registries.BLOCK.init();

        for (ArgumentsData pane : paneData()) {
            for (int meta = 0; meta < 16; meta++) {
                Block block = Block.get(pane.id(), meta);
                assertInstanceOf(pane.blockClass(), block);
                assertEquals(meta, block.getDamage());
            }
        }

        Block exposed = new BlockCopperBars(13)
                .getStateWithOxidizationLevel(cn.nukkit.block.properties.enums.OxidizationLevel.EXPOSED);
        assertInstanceOf(BlockCopperBarsExposed.class, exposed);
        assertEquals(13, exposed.getDamage());
    }

    @Test
    void hardGlassPanesKeepLegacyColorDamage() {
        BlockHardGlassPane hardPane = new BlockHardGlassPane();
        assertTrue(hardPane.getBlockProperties().getAllProperties().isEmpty());
        assertFalse(hardPane.updateConnections());

        BlockHardGlassPaneStained stained = new BlockHardGlassPaneStained(14);
        assertEquals(DyeColor.RED, stained.getDyeColor());
        assertEquals(14, stained.getDamage());
        assertEquals(14, stained.toItem().getDamage());
        assertEquals(14, stained.getDamage());
        assertFalse(stained.updateConnections());
    }

    private static Stream<Arguments> coloredPanes() {
        return paneData().stream().map(pane -> Arguments.of(
                pane.blockClass(), pane.id(), pane.identifier(), pane.color()));
    }

    private static Stream<Arguments> supportedProtocols() {
        return ProtocolInfo.SUPPORTED_PROTOCOLS.stream().map(Arguments::of);
    }

    private static java.util.List<ArgumentsData> paneData() {
        return java.util.List.of(
                new ArgumentsData(BlockGlassPaneStainedWhite.class, 160, "minecraft:white_stained_glass_pane", DyeColor.WHITE),
                new ArgumentsData(BlockGlassPaneStainedOrange.class, 898, "minecraft:orange_stained_glass_pane", DyeColor.ORANGE),
                new ArgumentsData(BlockGlassPaneStainedMagenta.class, 899, "minecraft:magenta_stained_glass_pane", DyeColor.MAGENTA),
                new ArgumentsData(BlockGlassPaneStainedLightBlue.class, 900, "minecraft:light_blue_stained_glass_pane", DyeColor.LIGHT_BLUE),
                new ArgumentsData(BlockGlassPaneStainedYellow.class, 901, "minecraft:yellow_stained_glass_pane", DyeColor.YELLOW),
                new ArgumentsData(BlockGlassPaneStainedLime.class, 902, "minecraft:lime_stained_glass_pane", DyeColor.LIME),
                new ArgumentsData(BlockGlassPaneStainedPink.class, 903, "minecraft:pink_stained_glass_pane", DyeColor.PINK),
                new ArgumentsData(BlockGlassPaneStainedGray.class, 904, "minecraft:gray_stained_glass_pane", DyeColor.GRAY),
                new ArgumentsData(BlockGlassPaneStainedLightGray.class, 905, "minecraft:light_gray_stained_glass_pane", DyeColor.LIGHT_GRAY),
                new ArgumentsData(BlockGlassPaneStainedCyan.class, 906, "minecraft:cyan_stained_glass_pane", DyeColor.CYAN),
                new ArgumentsData(BlockGlassPaneStainedPurple.class, 907, "minecraft:purple_stained_glass_pane", DyeColor.PURPLE),
                new ArgumentsData(BlockGlassPaneStainedBlue.class, 908, "minecraft:blue_stained_glass_pane", DyeColor.BLUE),
                new ArgumentsData(BlockGlassPaneStainedBrown.class, 909, "minecraft:brown_stained_glass_pane", DyeColor.BROWN),
                new ArgumentsData(BlockGlassPaneStainedGreen.class, 910, "minecraft:green_stained_glass_pane", DyeColor.GREEN),
                new ArgumentsData(BlockGlassPaneStainedRed.class, 911, "minecraft:red_stained_glass_pane", DyeColor.RED),
                new ArgumentsData(BlockGlassPaneStainedBlack.class, 912, "minecraft:black_stained_glass_pane", DyeColor.BLACK)
        );
    }

    private record ArgumentsData(
            Class<? extends BlockGlassPaneStained> blockClass,
            int id,
            String identifier,
            DyeColor color
    ) {
    }
}
