package cn.nukkit.registry;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.recipe.CraftingRecipe;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.Recipe;
import cn.nukkit.recipe.impl.*;
import cn.nukkit.recipe.impl.special.*;
import cn.nukkit.recipe.parser.RecipeParser;
import cn.nukkit.utils.RecipeUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

@Getter
public class RecipeRegistry implements IRegistry<Integer, Recipe, Recipe> {
    public static int NEXT_NETWORK_ID = 1;

    private final Int2ObjectOpenHashMap<BatchPacket> PACKETS = new Int2ObjectOpenHashMap<>();

    private final Map<String, List<ShapedRecipe>> SHAPED = new HashMap<>();
    private final Map<String, List<ShapelessRecipe>> SHAPELESS = new HashMap<>();
    private final Map<String, FurnaceRecipe> FURNACE = new HashMap<>();
    private final Map<String, BlastFurnaceRecipe> BLAST_FURNACE = new HashMap<>();

    private final Map<UUID, MultiRecipe> MULTI = new HashMap<>();
    private final Map<String, BrewingRecipe> BREWING = new HashMap<>();
    private final Map<String, ContainerRecipe> CONTAINER = new HashMap<>();
    private final Map<String, CampfireRecipe> CAMPFIRE = new HashMap<>();
    private final Map<UUID, SmithingRecipe> SMITHING = new Object2ObjectOpenHashMap<>();
    private final Map<String, StonecutterRecipe> STONECUTTER = new HashMap<>();
    private final Object2DoubleOpenHashMap<Recipe> FURNACE_XP = new Object2DoubleOpenHashMap<>();
    private final Collection<Recipe> RECIPES = new ArrayDeque<>();

    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        this.registerMultiRecipe(new RepairItemRecipe());
        this.registerMultiRecipe(new BookCloningRecipe());
        this.registerMultiRecipe(new MapCloningRecipe());
        this.registerMultiRecipe(new MapUpgradingRecipe());
        this.registerMultiRecipe(new MapExtendingRecipe());
        this.registerMultiRecipe(new BannerAddPatternRecipe());
        this.registerMultiRecipe(new BannerDuplicateRecipe());
        this.registerMultiRecipe(new FireworkRecipe());
        this.registerMultiRecipe(new DecoratedPotRecipe());
        this.registerMultiRecipe(new ShulkerBoxRecipe());

        final JsonObject brewing = JsonParser.parseReader(new InputStreamReader(Server.class.getClassLoader().getResourceAsStream("recipes/brewing_recipes.json"))).getAsJsonObject();

        brewing.get("potionMixes").getAsJsonArray().forEach((potionMix) -> {
            final JsonObject recipe = potionMix.getAsJsonObject();

            String fromPotionId = recipe.get("inputId").getAsString();
            int fromPotionMeta = recipe.get("inputMeta").getAsInt();
            String ingredient = recipe.get("reagentId").getAsString();
            int ingredientMeta = recipe.get("reagentMeta").getAsInt();
            String toPotionId = recipe.get("outputId").getAsString();
            int toPotionMeta = recipe.get("outputMeta").getAsInt();

            Registries.RECIPE.registerBrewingRecipe(new BrewingRecipe(Item.get(fromPotionId, fromPotionMeta), Item.get(ingredient, ingredientMeta), Item.get(toPotionId, toPotionMeta)));
        });

        brewing.get("containerMixes").getAsJsonArray().forEach((containerMix) -> {
            final JsonObject recipe = containerMix.getAsJsonObject();

            final Item ingredient = Item.get(recipe.get("reagentId").getAsString());

            final String fromPotionId = recipe.get("inputId").getAsString();
            final String toPotionId = recipe.get("outputId").getAsString();

            for (int meta : Registries.POTION.getPotionId2TypeMap().keySet()) {
                Registries.RECIPE.registerBrewingRecipe(new BrewingRecipe(
                        Item.get(fromPotionId, meta),
                        ingredient,
                        Item.get(toPotionId, meta)
                ));
            }
        });

        RecipeParser.loadRecipes(JsonParser.parseReader(new InputStreamReader(Server.class.getClassLoader().getResourceAsStream("recipes/recipes.json"))).getAsJsonObject().get("recipes").getAsJsonArray());
    }

    @Override
    public void register(Integer key, Recipe value) {
        throw new UnsupportedOperationException("Not supported for RecipeRegistry");
    }

    @Override
    public Recipe get(Integer key) {
        throw new UnsupportedOperationException("Not supported for RecipeRegistry");
    }

    @Override
    public void trim() {

    }

    @Override
    public void reload() {
        isLoad.set(false);
        SHAPED.clear();
        SHAPELESS.clear();
        FURNACE.clear();
        BLAST_FURNACE.clear();
        MULTI.clear();
        BREWING.clear();
        CONTAINER.clear();
        CAMPFIRE.clear();
        SMITHING.clear();
        FURNACE_XP.clear();
        RECIPES.clear();
        init();
        buildPackets();
    }

    public void registerFurnaceRecipe(FurnaceRecipe recipe, double xp) {
        FURNACE.put(RecipeUtils.getItemHash(recipe.getInput()), recipe);
        FURNACE_XP.put(recipe, xp);
    }

    public void registerBlastFurnaceRecipe(BlastFurnaceRecipe recipe, double xp) {
        BLAST_FURNACE.put(RecipeUtils.getItemHash(recipe.getInput()), recipe);
        FURNACE_XP.put(recipe, xp);
    }

    public void registerShapedRecipe(ShapedRecipe recipe) {
        String resultHash = RecipeUtils.getItemHash(recipe.getResult());
        SHAPED.computeIfAbsent(resultHash, (key) -> new ArrayList<>()).add(recipe);
        RECIPES.add(recipe);
    }

    public void registerShapelessRecipe(ShapelessRecipe recipe) {
        String resultHash = RecipeUtils.getItemHash(recipe.getResult());
        SHAPELESS.computeIfAbsent(resultHash, (key) -> new ArrayList<>()).add(recipe);
        RECIPES.add(recipe);
    }

    public void addStonecutterRecipe(String id, String uuid, int priority, Item input, Item output) {
        registerShapelessRecipe(new StonecutterRecipe(id, priority, output, List.of(new DefaultDescriptor(input))));
        StonecutterRecipe recipe = STONECUTTER.computeIfAbsent(RecipeUtils.getItemHash(input), (key) -> {
            StonecutterRecipe stonecutterRecipe = new StonecutterRecipe(id, priority, output, new ArrayList<>());
            stonecutterRecipe.setId(UUID.fromString(uuid));
            return stonecutterRecipe;
        });
        recipe.addResult(new DefaultDescriptor(output));
    }

    public void registerSmithingRecipe(SmithingRecipe recipe) {
        this.SMITHING.put(UUID.randomUUID(), recipe);
    }

    public void registerBrewingRecipe(BrewingRecipe recipe) {
        Item input = recipe.getInput();
        Item ingredient = recipe.getIngredient();
        Item output = recipe.getResult();

        String recipeId = RecipeUtils.computeBrewingRecipeId(input, ingredient, output);
        this.BREWING.put(recipeId, recipe);
    }

    public void registerContainerRecipe(ContainerRecipe recipe) {
        Item input = recipe.getInput();
        Item ingredient = recipe.getIngredient();
        Item output = recipe.getResult();

        String recipeId = RecipeUtils.computeContainerRecipeId(input, ingredient, output);
        this.CONTAINER.put(recipeId, recipe);
    }

    public void registerCampfireRecipe(CampfireRecipe recipe, double xp) {
        Item input = recipe.getInput();
        this.CAMPFIRE.put(RecipeUtils.getItemHash(input), recipe);
        FURNACE_XP.put(recipe, xp);
    }

    public void registerMultiRecipe(MultiRecipe recipe) {
        this.MULTI.put(recipe.getId(), recipe);
        RECIPES.add(recipe);
    }

    public FurnaceRecipe matchBlastFurnaceRecipe(Item input) {
        BlastFurnaceRecipe recipe = BLAST_FURNACE.get(RecipeUtils.getItemHash(input));
        if (recipe == null) recipe = BLAST_FURNACE.get(RecipeUtils.getItemHash(input, 0));
        return recipe;
    }

    public FurnaceRecipe matchFurnaceRecipe(Item input) {
        FurnaceRecipe recipe = FURNACE.get(RecipeUtils.getItemHash(input));
        if (recipe == null) recipe = FURNACE.get(RecipeUtils.getItemHash(input, 0));
        return recipe;
    }

    public CraftingRecipe matchRecipe(List<Item> inputList, Item primaryOutput, List<Item> extraOutputList) {
        String outputHash = RecipeUtils.getItemHash(primaryOutput);
        if (SHAPED.containsKey(outputHash)) {
            List<ShapedRecipe> recipes = SHAPED.get(outputHash);

            if (recipes != null) {
                for (ShapedRecipe shapedRecipe : recipes) {
                    if (shapedRecipe.matchItems(inputList, extraOutputList)) {
                        return shapedRecipe;
                    }
                }
            }
        }

        if (SHAPELESS.containsKey(outputHash)) {
            List<ShapelessRecipe> recipes = SHAPELESS.get(outputHash);

            if (recipes != null) {
                for (ShapelessRecipe shapelessRecipe : recipes) {
                    if (shapelessRecipe.matchItems(inputList, extraOutputList)) {
                        return shapelessRecipe;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public SmithingRecipe matchSmithingRecipe(List<Item> inputList) {
        ArrayList<Item> list = new ArrayList<>();
        for (Item item : inputList) {
            Item clone = item.clone();
            clone.setCount(1);
            if ((item.isTool() || item.isArmor()) && item.getDamage() > 0) {
                clone.setDamage(0);
            }
            list.add(clone);
        }

        for (SmithingRecipe smithingRecipe : SMITHING.values()) {
            if (smithingRecipe.matchItems(list)) {
                return smithingRecipe;
            }
        }

        return null;
    }

    public BrewingRecipe findBrewingRecipe(Item input, Item potion) {
        for(BrewingRecipe recipe : BREWING.values()) {
            if (recipe.fastCheck(input, potion)) return recipe;
        }
        return null;
    }

    public ContainerRecipe findContainerRecipe(Item input, Item potion) {
        for(ContainerRecipe recipe : CONTAINER.values()) {
            if (recipe.fastCheck(input, potion)) return recipe;
        }
        return null;
    }

    public CampfireRecipe matchCampfireRecipe(Item input) {
        CampfireRecipe recipe = this.CAMPFIRE.get(RecipeUtils.getItemHash(input));
        if (recipe == null) recipe = this.CAMPFIRE.get(RecipeUtils.getItemHash(input, 0));
        return recipe;
    }

    public StonecutterRecipe matchStonecutterRecipe(Item input, List<Item> outputs) {
        StonecutterRecipe recipe = this.STONECUTTER.get(RecipeUtils.getItemHash(input));

        if(recipe != null) {
            boolean found = false;

            for(Item item : outputs) {
                for(ItemDescriptor descriptor : recipe.getIngredientList()) {
                    if(descriptor instanceof DefaultDescriptor defaultDescriptor && defaultDescriptor.getItem().getNamespaceId().equals(item.getNamespaceId())) {
                        found = true;
                        break;
                    }
                }
            }

            if(!found) {
                return null;
            }
        }

        return recipe;
    }

    public MultiRecipe getMultiRecipe(Player player, Item outputItem, Collection<ItemDescriptor> inputs) {
        if (outputItem == null) return null;
        return this.MULTI.values().stream().filter(multiRecipe -> multiRecipe.canExecute(player, outputItem, inputs)).findFirst().orElse(null);
    }

    public double getRecipeXp(Recipe recipe) {
        return FURNACE_XP.getOrDefault(recipe, 0.0);
    }

    public void setRecipeXp(Recipe recipe, double xp) {
        FURNACE_XP.put(recipe, xp);
    }

    public BatchPacket getPacket(int protocol) {
        return PACKETS.get(protocol);
    }

    @ApiStatus.Internal
    public void buildPackets() {
        ProtocolInfo.SUPPORTED_PROTOCOLS.parallelStream().forEach(protocol -> {
            CraftingDataPacket pk = new CraftingDataPacket();
            pk.protocol = protocol;

            for (FurnaceRecipe recipe : FURNACE.values()) {
                pk.addFurnaceRecipe(recipe);
            }

            for (MultiRecipe recipe : this.getMULTI().values()) {
                if(!recipe.hideRecipe()) {
                    pk.addMultiRecipe(recipe);
                }
            }

            for (Recipe recipe : this.getRECIPES()) {
                if (recipe instanceof ShapedRecipe) {
                    pk.addShapedRecipe((ShapedRecipe) recipe);
                } else if (recipe instanceof ShapelessRecipe) {
                    pk.addShapelessRecipe((ShapelessRecipe) recipe);
                }
            }

            for (SmithingRecipe recipe : this.getSMITHING().values()) {
                pk.addShapelessRecipe(recipe);
            }

            for (BrewingRecipe recipe : BREWING.values()) {
                pk.addBrewingRecipe(recipe);
            }

            for (ContainerRecipe recipe : CONTAINER.values()) {
                pk.addContainerRecipe(recipe);
            }

            pk.tryEncode();

            synchronized (PACKETS) {
                PACKETS.put(protocol, pk.compress(Deflater.BEST_COMPRESSION));
            }
        });
    }
}
