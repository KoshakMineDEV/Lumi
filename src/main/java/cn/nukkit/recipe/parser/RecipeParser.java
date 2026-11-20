package cn.nukkit.recipe.parser;

import cn.nukkit.Server;
import cn.nukkit.block.BlockShulkerBox;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.material.tags.ItemTags;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemTagDescriptor;
import cn.nukkit.recipe.impl.*;
import cn.nukkit.recipe.impl.furnace.BlastFurnaceRecipe;
import cn.nukkit.recipe.impl.furnace.FurnaceRecipe;
import cn.nukkit.recipe.impl.furnace.SmokerRecipe;
import cn.nukkit.registry.Registries;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RecipeParser {
    private static DefaultDescriptor parseItem(JsonObject item) {
        final Item result;
        if (item.has("id")) {
            result = Registries.ITEM.get(item.get("id").getAsString());
        } else if (item.has("itemId")) {
            result = Registries.ITEM.get(item.get("itemId").getAsString());
        } else {
            result = Registries.ITEM.get(item.get("name").getAsString());
            if (result.isNull()) throw new ComplexAliasException();
        }

        if (item.has("count")) {
            result.setCount(item.get("count").getAsInt());
        }

        int damage = result.getDamage();
        if (item.has("damage")) {
            damage = item.get("damage").getAsInt();
        }

        if (item.has("auxValue")) {
            damage = item.get("auxValue").getAsInt();
        }

        if (damage != 32767 && result.getDamage() == 0) result.setDamage(damage);

        return new DefaultDescriptor(result);
    }

    private static ItemDescriptor parseInput(JsonObject input) {
        if (input.has("itemTag")) {
            String id = input.get("itemTag").getAsString();
            return new ItemTagDescriptor(ItemTags.getTag(id), id);
        }
        return parseItem(input);
    }

    private static DefaultDescriptor parseOutput(JsonElement output, List<DefaultDescriptor> extra) {
        DefaultDescriptor result;
        if (output.isJsonArray()) {
            JsonArray array = output.getAsJsonArray();
            if (array.isEmpty()) {
                throw new RuntimeException("Output is empty");
            }

            for (int i = 1; i < array.size(); ++i) {
                extra.add(parseItem(array.get(i).getAsJsonObject()));
            }

            result = parseItem(array.get(0).getAsJsonObject());
        } else {
            result = parseItem(output.getAsJsonObject());
        }
        return result;
    }

    public static void loadRecipes(JsonArray recipes) {
        JsonObject furnaceXp = JsonParser.parseReader(new InputStreamReader(Server.class.getClassLoader().getResourceAsStream("recipes/furnace_xp.json"))).getAsJsonObject();

        recipes.forEach(json -> {
            final JsonObject recipe = json.getAsJsonObject();

            try {
                final int type = recipe.get("type").getAsInt();

                switch (type) {
                    case 4, 9 -> {
                    }

                    case 1 -> {
                        final String block = recipe.get("block").getAsString();

                        switch (block) {
                            case "crafting_table", "deprecated" -> {
                                final String[] shape = new String[recipe.get("height").getAsInt()];
                                final JsonArray shapeJson = recipe.get("shape").getAsJsonArray();
                                for (int i = 0; i < shape.length; i++) {
                                    shape[i] = shapeJson.get(i).getAsString();
                                }

                                final Map<Character, ItemDescriptor> items = new HashMap<>();
                                final JsonObject input = recipe.get("input").getAsJsonObject();

                                input.entrySet().forEach(entry -> {
                                    items.put(entry.getKey().charAt(0), parseInput(entry.getValue().getAsJsonObject()));
                                });

                                final List<DefaultDescriptor> extra = new ArrayList<>();

                                Registries.RECIPE.registerShapedRecipe(new ShapedRecipe(
                                        recipe.get("id").getAsString(),
                                        recipe.get("priority").getAsInt(),
                                        parseOutput(recipe.get("output"), extra).getItem(),
                                        shape,
                                        items,
                                        extra.stream()
                                                .map(DefaultDescriptor::getItem)
                                                .collect(Collectors.toList())
                                ));
                            }
                        }
                    }

                    case 8 -> {
                        final String block = recipe.get("block").getAsString();
                        switch (block) {
                            case "smithing_table" -> {
                                Registries.RECIPE.registerSmithingRecipe(new SmithingRecipe(
                                        recipe.get("id").getAsString(),
                                        0,
                                        List.of(
                                                parseItem(recipe.get("base").getAsJsonObject()),
                                                parseItem(recipe.get("addition").getAsJsonObject()),
                                                parseItem(recipe.get("template").getAsJsonObject())
                                        ),
                                        parseItem(recipe.get("result").getAsJsonObject()).getItem()
                                ));
                            }
                            default -> log.warn("Not support block type: {}", block);
                        }
                    }

                    case 0, 5 -> {
                        final String block = recipe.get("block").getAsString();

                        switch (block) {
                            case "soul_campfire" -> {
                            }
                            case  "deprecated" -> {
                            }
                            case "crafting_table" -> {
                                final Collection<ItemDescriptor> inputs = new ArrayList<>();

                                recipe.getAsJsonArray("input").getAsJsonArray().forEach(item -> {
                                    inputs.add(parseInput(item.getAsJsonObject()));
                                });

                                String id = recipe.get("id").getAsString();
                                if (!id.startsWith("paper_sulphur")) {
                                    int priority = recipe.get("priority").getAsInt();
                                    Item output = parseOutput(recipe.get("output"), List.of()).getItem();
                                    ShapelessRecipe shapeless = type == 5 && output instanceof ItemBlock outputBlock
                                            && outputBlock.getBlockUnsafe() instanceof BlockShulkerBox
                                            ? new ShulkerBoxDyeRecipe(id, priority, output, inputs)
                                            : new ShapelessRecipe(id, priority, output, inputs);
                                    Registries.RECIPE.registerShapelessRecipe(shapeless);
                                }
                            }

                            case "stonecutter" -> {
                                final String id = recipe.get("id").getAsString();
                                final Collection<Item> inputs = new ArrayList<>();
                                final Collection<Item> outputs = new ArrayList<>();

                                recipe.getAsJsonArray("input").getAsJsonArray().forEach(item -> {
                                    inputs.add(parseItem(item.getAsJsonObject()).getItem());
                                });

                                recipe.getAsJsonArray("output").getAsJsonArray().forEach(item -> {
                                    outputs.add(parseItem(item.getAsJsonObject()).getItem());
                                });

                                for (Item input : inputs) {
                                    for (Item output : outputs) {
                                        Registries.RECIPE.addStonecutterRecipe(new StonecutterRecipe(id, recipe.get("priority").getAsInt(), output, List.of(new DefaultDescriptor(input))));
                                    }
                                }
                            }

                            case "cartography_table" -> {
                            }

                            case "smoker" -> {
                                final ItemDescriptor descriptor = parseInput(recipe.getAsJsonArray("input").getAsJsonArray().get(0).getAsJsonObject());

                                double xp = 0;
                                if (descriptor instanceof DefaultDescriptor defaultDescriptor && furnaceXp.has(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage())) {
                                    xp = furnaceXp.get(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage()).getAsDouble();
                                }

                                addSmoker(descriptor, parseItem(recipe.getAsJsonArray("output").getAsJsonArray().get(0).getAsJsonObject()).getItem(), xp);
                            }

                            case "campfire" -> {
                                final ItemDescriptor descriptor = parseInput(recipe.getAsJsonArray("input").getAsJsonArray().get(0).getAsJsonObject());

                                double xp = 0;
                                if (descriptor instanceof DefaultDescriptor defaultDescriptor && furnaceXp.has(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage())) {
                                    xp = furnaceXp.get(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage()).getAsDouble();
                                }

                                addCampfire(descriptor, parseItem(recipe.getAsJsonArray("output").getAsJsonArray().get(0).getAsJsonObject()).getItem(), xp);
                            }

                            case "blast_furnace" -> {
                                final Item item = parseItem(recipe.getAsJsonArray("input").getAsJsonArray().get(0).getAsJsonObject()).getItem();

                                double xp = 0;
                                if (furnaceXp.has(item.getNamespaceId() + ":" + item.getDamage())) {
                                    xp = furnaceXp.get(item.getNamespaceId() + ":" + item.getDamage()).getAsDouble();
                                }

                                addBlastFurnace(
                                        new DefaultDescriptor(item),
                                        parseItem(recipe.getAsJsonArray("output").getAsJsonArray().get(0).getAsJsonObject()).getItem(),
                                        xp
                                );
                            }

                            case "furnace" -> {
                                final ItemDescriptor descriptor = parseInput(recipe.getAsJsonArray("input").getAsJsonArray().get(0).getAsJsonObject());

                                double xp = 0;
                                if (descriptor instanceof DefaultDescriptor defaultDescriptor && furnaceXp.has(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage())) {
                                    xp = furnaceXp.get(defaultDescriptor.getItem().getNamespaceId() + ":" + defaultDescriptor.getItem().getDamage()).getAsDouble();
                                }

                                addFurnace(
                                        descriptor,
                                        parseItem(recipe.getAsJsonArray("output").getAsJsonArray().get(0).getAsJsonObject()).getItem(),
                                        xp
                                );
                            }

                            default -> log.warn("Not support block type: {}", block);
                        }
                    }

                    default -> log.warn("Unknown recipe type: {}", type);
                }
            } catch (Exception e) {
                if (!(e instanceof ComplexAliasException)) {
                    e.printStackTrace();
                    log.error("Failed to load recipe {}, exception {}", recipe.get("id").toString(), e);
                }
            }
        });
    }

    private static void addFurnace(ItemDescriptor input, Item output, double xp) {
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(output, input);
        furnaceRecipe.setId(UUID.randomUUID());
        Registries.RECIPE.registerFurnaceRecipe(furnaceRecipe, xp);
    }

    private static void addBlastFurnace(ItemDescriptor input, Item output, double xp) {
        BlastFurnaceRecipe blastFurnaceRecipe = new BlastFurnaceRecipe(output, input);
        blastFurnaceRecipe.setId(UUID.randomUUID());
        Registries.RECIPE.registerBlastFurnaceRecipe(blastFurnaceRecipe, xp);
    }

    private static void addSmoker(ItemDescriptor input, Item output, double xp) {
        SmokerRecipe smokerRecipe = new SmokerRecipe(output, input);
        smokerRecipe.setId(UUID.randomUUID());
        Registries.RECIPE.registerSmokerRecipe(smokerRecipe, xp);
    }

    private static void addCampfire(ItemDescriptor input, Item output, double xp) {
        Registries.RECIPE.registerCampfireRecipe(new CampfireRecipe(
                output,
                input
        ), xp);
    }
}
