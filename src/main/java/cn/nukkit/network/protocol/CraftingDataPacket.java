package cn.nukkit.network.protocol;

import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.descriptor.ItemTagDescriptor;
import cn.nukkit.recipe.impl.data.RecipeUnlockingRequirement;
import cn.nukkit.item.Item;
import cn.nukkit.item.material.tags.ItemTags;
import cn.nukkit.recipe.*;
import cn.nukkit.recipe.impl.*;
import cn.nukkit.recipe.impl.furnace.BlastFurnaceRecipe;
import cn.nukkit.recipe.impl.furnace.FurnaceRecipe;
import cn.nukkit.recipe.impl.furnace.SmokerRecipe;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Nukkit Project Team
 */
@ToString
public class CraftingDataPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

    public static final int SMITHING_ARMOR_TRIM_NETWORK_ID = 1;

    public static final String CRAFTING_TAG_CRAFTING_TABLE = "crafting_table";
    public static final String CRAFTING_TAG_CARTOGRAPHY_TABLE = "cartography_table";
    public static final String CRAFTING_TAG_STONECUTTER = "stonecutter";
    public static final String CRAFTING_TAG_FURNACE = "furnace";
    public static final String CRAFTING_TAG_CAMPFIRE = "campfire";
    public static final String CRAFTING_TAG_BLAST_FURNACE = "blast_furnace";
    public static final String CRAFTING_TAG_SMOKER = "smoker";
    public static final String CRAFTING_TAG_SMITHING_TABLE = "smithing_table";

    private List<Recipe> entries = new ArrayList<>();
    private final List<BrewingRecipe> brewingEntries = new ArrayList<>();
    private final List<ContainerRecipe> containerEntries = new ArrayList<>();
    public boolean cleanRecipes = true;

    public void addShapelessRecipe(ShapelessRecipe... recipe) {
        for(ShapelessRecipe shapelessRecipe : recipe) {
            if (shapelessRecipe.isValidRecipe(protocol)) {
                this.entries.add(shapelessRecipe);
            }
        }
    }

    public void addShapedRecipe(ShapedRecipe... recipe) {
        for(ShapedRecipe shapedRecipe : recipe) {
            if (shapedRecipe.isValidRecipe(protocol)) {
                this.entries.add(shapedRecipe);
            }
        }
    }

    public void addFurnaceRecipe(FurnaceRecipe... recipes) {
        for (FurnaceRecipe recipe : recipes) {
            if (protocol >= ProtocolInfo.v1_26_20_26) {
                entries.add(recipe);
            } else if (recipe.getType() == RecipeType.FURNACE && recipe.getInput() instanceof DefaultDescriptor) {
                entries.add(recipe);
            }
        }
    }

    public void addBrewingRecipe(BrewingRecipe... recipe) {
        Collections.addAll(brewingEntries, recipe);
    }

    public void addMultiRecipe(MultiRecipe... recipe) {
        Collections.addAll(entries, recipe);
    }

    public void addContainerRecipe(ContainerRecipe... recipe) {
        Collections.addAll(containerEntries, recipe);
    }

    @Override
    public DataPacket clean() {
        entries = new ArrayList<>();
        return super.clean();
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        if (this.protocol >= ProtocolInfo.v1_26_40) {
            this.encodeV2168();
            return;
        }
        this.putUnsignedVarInt(entries.size() + 1);

        for (Recipe recipe : entries) {
            RecipeType networkType = recipe.getType();
            if ((networkType == RecipeType.FURNACE || networkType == RecipeType.FURNACE_DATA) && protocol >= ProtocolInfo.v1_26_20_26) {
                networkType = RecipeType.SHAPELESS;
            }
            this.putVarInt(networkType.getNetworkType());
            switch (recipe.getType()) {
                case STONECUTTER:
                    StonecutterRecipe stonecutterRecipe = (StonecutterRecipe) recipe;
                    this.putString(stonecutterRecipe.getRecipeId());
                    Collection<ItemDescriptor> ingredientsStonecutter = stonecutterRecipe.getIngredientList();

                    this.putUnsignedVarInt(ingredientsStonecutter.size());
                    for (ItemDescriptor ingredient : ingredientsStonecutter) {
                        ingredient.putRecipe(this, protocol);
                    }
                    this.putUnsignedVarInt(1); // Results length
                    this.putSlot(protocol, stonecutterRecipe.getResult(), true);
                    this.putUUID(stonecutterRecipe.getId());

                    this.putString(CRAFTING_TAG_STONECUTTER);
                    this.putVarInt(stonecutterRecipe.getPriority());

                    if (protocol >= ProtocolInfo.v1_21_0) {
                        this.writeRequirement(stonecutterRecipe);
                    }

                    this.putUnsignedVarInt(stonecutterRecipe.getNetworkId());
                    break;
                case SHAPELESS, SHULKER_BOX:
                    ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                    this.putString(shapeless.getRecipeId());
                    Collection<ItemDescriptor> ingredients = shapeless.getIngredientList();

                    this.putUnsignedVarInt(ingredients.size());
                    for (ItemDescriptor ingredient : ingredients) {
                        ingredient.putRecipe(this, protocol);
                    }
                    this.putUnsignedVarInt(1); // Results length
                    this.putSlot(protocol, shapeless.getResult(), true);
                    this.putUUID(shapeless.getId());

                    this.putString(CRAFTING_TAG_CRAFTING_TABLE);
                    this.putVarInt(shapeless.getPriority());

                    if (protocol >= ProtocolInfo.v1_21_0) {
                        this.writeRequirement(shapeless);
                    }

                    this.putUnsignedVarInt(shapeless.getNetworkId());
                    break;
                case SMITHING_TRANSFORM:
                    SmithingRecipe smithing = (SmithingRecipe) recipe;
                    this.putString(smithing.getRecipeId());
                    new DefaultDescriptor(smithing.getTemplate()).putRecipe(this, protocol);
                    new DefaultDescriptor(smithing.getEquipment()).putRecipe(this, protocol);
                    new DefaultDescriptor(smithing.getIngredient()).putRecipe(this, protocol);
                    this.putSlot(protocol, smithing.getResult(), true);
                    this.putString(CRAFTING_TAG_SMITHING_TABLE);
                    this.putUnsignedVarInt(smithing.getNetworkId());
                    break;
                case SHAPED:
                    ShapedRecipe shaped = (ShapedRecipe) recipe;
                    this.putString(shaped.getRecipeId());
                    this.putVarInt(shaped.getWidth());
                    this.putVarInt(shaped.getHeight());

                    for (int z = 0; z < shaped.getHeight(); ++z) {
                        for (int x = 0; x < shaped.getWidth(); ++x) {
                            shaped.getIngredient(x, z).putRecipe(this, protocol);
                        }
                    }
                    List<Item> outputs = new ArrayList<>();
                    outputs.add(shaped.getResult());
                    outputs.addAll(shaped.getExtraResults());
                    this.putUnsignedVarInt(outputs.size());
                    for (Item output : outputs) {
                        this.putSlot(protocol, output, true);
                    }
                    this.putUUID(shaped.getId());

                    this.putString(CRAFTING_TAG_CRAFTING_TABLE);
                    this.putVarInt(shaped.getPriority());

                    if (this.protocol >= ProtocolInfo.v1_20_80) {
                        this.putBoolean(shaped.isAssumeSymetry());
                    }

                    if (protocol >= ProtocolInfo.v1_21_0) {
                        this.writeRequirement(shaped);
                    }

                    this.putUnsignedVarInt(shaped.getNetworkId());
                    break;
                case SMOKER:
                case BLAST_FURNACE:
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    if (protocol >= ProtocolInfo.v1_26_20_26) {
                        this.putString(furnace.getRecipeId());
                        this.putUnsignedVarInt(1); // Ingredients length
                        furnace.getInput().putRecipe(this, protocol);
                        this.putUnsignedVarInt(1); // Results length
                        this.putSlot(protocol, furnace.getResult(), true);
                        this.putUUID(furnace.getId());
                        String craftingTag;
                        if (recipe instanceof BlastFurnaceRecipe) {
                            craftingTag = CRAFTING_TAG_BLAST_FURNACE;
                        } else if(recipe instanceof SmokerRecipe) {
                            craftingTag = CRAFTING_TAG_SMOKER;
                        } else {
                            craftingTag = CRAFTING_TAG_FURNACE;
                        }
                        this.putString(craftingTag);
                        this.putVarInt(0); // priority
                        this.putByte((byte) RecipeUnlockingRequirement.UnlockingContext.ALWAYS_UNLOCKED.ordinal());
                        this.putUnsignedVarInt(furnace.getNetworkId());
                    } else {
                        Item input = ((DefaultDescriptor) furnace.getInput()).getItem();

                        int runtimeId = 0;
                        int damage = 0;
                        if (!input.hasMeta()) {
                            try {
                                runtimeId = RuntimeItems.getMapping(protocol).toRuntime(input.getId(), 0).getRuntimeId();
                                damage = 0x7fff;
                            } catch (IllegalArgumentException ignored) {
                            }
                        } else {
                            try {
                                RuntimeItemMapping.RuntimeEntry runtimeEntry = RuntimeItems.getMapping(protocol).toRuntime(input.getId(), input.getDamage());
                                runtimeId = runtimeEntry.getRuntimeId();
                                damage = runtimeEntry.isHasDamage() ? 0 : input.getDamage();
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        this.putVarInt(runtimeId);
                        if (recipe.getType() == RecipeType.FURNACE_DATA) {
                            this.putVarInt(damage);
                        }
                        this.putSlot(protocol, furnace.getResult(), true);
                        this.putString(CRAFTING_TAG_FURNACE);
                    }
                    break;
                case MULTI:
                    this.putUUID(((MultiRecipe) recipe).getId());
                    this.putUnsignedVarInt(((MultiRecipe) recipe).getNetworkId());
                    break;
            }
        }

        // Identical smithing_trim recipe sent by BDS that uses tag-descriptors, as the client seems to ignore the
        // approach of using many default-descriptors (which we do for smithing_transform)
        this.putVarInt(RecipeType.SMITHING_TRIM.getNetworkType());
        this.putString("minecraft:smithing_armor_trim");
        new ItemTagDescriptor(ItemTags.TRIM_TEMPLATES, "minecraft:trim_templates").putRecipe(this, protocol);
        new ItemTagDescriptor(ItemTags.TRIMMABLE_ARMORS, "minecraft:trimmable_armors").putRecipe(this, protocol);
        new ItemTagDescriptor(ItemTags.TRIM_MATERIALS, "minecraft:trim_materials").putRecipe(this, protocol);
        this.putString(CRAFTING_TAG_SMITHING_TABLE);
        this.putUnsignedVarInt(SMITHING_ARMOR_TRIM_NETWORK_ID);


        this.putUnsignedVarInt(this.brewingEntries.size());
        for (BrewingRecipe recipe : brewingEntries) {
            this.putVarInt(recipe.getInput().getNetworkId(protocol));
            this.putVarInt(recipe.getInput().getDamage());
            this.putVarInt(recipe.getIngredient().getNetworkId(protocol));
            this.putVarInt(recipe.getIngredient().getDamage());
            this.putVarInt(recipe.getResult().getNetworkId(protocol));
            this.putVarInt(recipe.getResult().getDamage());
        }

        this.putUnsignedVarInt(this.containerEntries.size());
        for (ContainerRecipe recipe : containerEntries) {
            this.putVarInt(recipe.getInput().getNetworkId(protocol));
            this.putVarInt(recipe.getIngredient().getNetworkId(protocol));
            this.putVarInt(recipe.getResult().getNetworkId(protocol));
        }

        this.putUnsignedVarInt(0); // Material reducers size


        this.putBoolean(cleanRecipes);
    }

    private void encodeV2168() {
        List<Recipe> shaped = new ArrayList<>();
        List<Recipe> shapeless = new ArrayList<>();
        List<Recipe> multi = new ArrayList<>();
        List<Recipe> shulker = new ArrayList<>();
        List<Recipe> shapelessChemistry = new ArrayList<>();
        List<Recipe> shapedChemistry = new ArrayList<>();
        List<Recipe> smithingTransform = new ArrayList<>();
        for (Recipe recipe : entries) {
            switch (recipe.getType()) {
                case SHAPED -> shaped.add(recipe);
                case MULTI -> multi.add(recipe);
                case SHULKER_BOX -> shulker.add(recipe);
                case SHAPELESS_CHEMISTRY -> shapelessChemistry.add(recipe);
                case SHAPED_CHEMISTRY -> shapedChemistry.add(recipe);
                case SMITHING_TRANSFORM -> smithingTransform.add(recipe);
                case SMITHING_TRIM -> { }
                default -> shapeless.add(recipe);
            }
        }
        this.putUnsignedVarInt(shaped.size());
        for (Recipe recipe : shaped) this.writeShapedRecipeV2168((ShapedRecipe) recipe);
        this.putUnsignedVarInt(shapeless.size());
        for (Recipe recipe : shapeless) {
            if (recipe instanceof FurnaceRecipe furnace) this.writeFurnaceRecipeV2168(furnace);
            else this.writeShapelessRecipeV2168((ShapelessRecipe) recipe);
        }
        this.putUnsignedVarInt(multi.size());
        for (Recipe recipe : multi) {
            MultiRecipe value = (MultiRecipe) recipe;
            this.putUUID(value.getId());
            this.putUnsignedVarInt(value.getNetworkId());
        }
        this.putUnsignedVarInt(shulker.size());
        for (Recipe recipe : shulker) this.writeShapelessRecipeV2168((ShapelessRecipe) recipe);
        this.putUnsignedVarInt(shapelessChemistry.size());
        for (Recipe recipe : shapelessChemistry) this.writeShapelessRecipeV2168((ShapelessRecipe) recipe);
        this.putUnsignedVarInt(shapedChemistry.size());
        for (Recipe recipe : shapedChemistry) this.writeShapedRecipeV2168((ShapedRecipe) recipe);
        this.putUnsignedVarInt(smithingTransform.size());
        for (Recipe recipe : smithingTransform) {
            SmithingRecipe smithing = (SmithingRecipe) recipe;
            this.putString(smithing.getRecipeId());
            new DefaultDescriptor(smithing.getTemplate()).putRecipe(this, protocol);
            new DefaultDescriptor(smithing.getEquipment()).putRecipe(this, protocol);
            new DefaultDescriptor(smithing.getIngredient()).putRecipe(this, protocol);
            this.putSlot(protocol, smithing.getResult(), true);
            this.putString(CRAFTING_TAG_SMITHING_TABLE);
            this.putUnsignedVarInt(smithing.getNetworkId());
        }
        this.putUnsignedVarInt(1);
        this.putString("minecraft:smithing_armor_trim");
        new ItemTagDescriptor(ItemTags.TRIM_TEMPLATES, "minecraft:trim_templates").putRecipe(this, protocol);
        new ItemTagDescriptor(ItemTags.TRIMMABLE_ARMORS, "minecraft:trimmable_armors").putRecipe(this, protocol);
        new ItemTagDescriptor(ItemTags.TRIM_MATERIALS, "minecraft:trim_materials").putRecipe(this, protocol);
        this.putString(CRAFTING_TAG_SMITHING_TABLE);
        this.putUnsignedVarInt(SMITHING_ARMOR_TRIM_NETWORK_ID);
        this.putUnsignedVarInt(this.brewingEntries.size());
        for (BrewingRecipe recipe : brewingEntries) {
            this.putVarInt(recipe.getInput().getNetworkId(protocol));
            this.putVarInt(recipe.getInput().getDamage());
            this.putVarInt(recipe.getIngredient().getNetworkId(protocol));
            this.putVarInt(recipe.getIngredient().getDamage());
            this.putVarInt(recipe.getResult().getNetworkId(protocol));
            this.putVarInt(recipe.getResult().getDamage());
        }
        this.putUnsignedVarInt(this.containerEntries.size());
        for (ContainerRecipe recipe : containerEntries) {
            this.putVarInt(recipe.getInput().getNetworkId(protocol));
            this.putVarInt(recipe.getIngredient().getNetworkId(protocol));
            this.putVarInt(recipe.getResult().getNetworkId(protocol));
        }
        this.putUnsignedVarInt(0);
        this.putBoolean(cleanRecipes);
    }

    private void writeShapedRecipeV2168(ShapedRecipe recipe) {
        this.putString(recipe.getRecipeId());
        this.putVarInt(recipe.getWidth());
        this.putVarInt(recipe.getHeight());
        this.putUnsignedVarInt(recipe.getWidth() * recipe.getHeight());
        for (int z = 0; z < recipe.getHeight(); z++) for (int x = 0; x < recipe.getWidth(); x++) recipe.getIngredient(x, z).putRecipe(this, protocol);
        List<Item> outputs = new ArrayList<>();
        outputs.add(recipe.getResult());
        outputs.addAll(recipe.getExtraResults());
        this.putUnsignedVarInt(outputs.size());
        for (Item output : outputs) this.putSlot(protocol, output, true);
        this.putUUID(recipe.getId());
        this.putString(CRAFTING_TAG_CRAFTING_TABLE);
        this.putVarInt(recipe.getPriority());
        this.putBoolean(recipe.isAssumeSymetry());
        this.putBoolean(true);
        this.writeRequirementV2168(recipe);
        this.putUnsignedVarInt(recipe.getNetworkId());
    }

    private void writeShapelessRecipeV2168(ShapelessRecipe recipe) {
        this.putString(recipe.getRecipeId());
        this.putUnsignedVarInt(recipe.getIngredientList().size());
        for (ItemDescriptor ingredient : recipe.getIngredientList()) ingredient.putRecipe(this, protocol);
        this.putUnsignedVarInt(1);
        this.putSlot(protocol, recipe.getResult(), true);
        this.putUUID(recipe.getId());
        this.putString(recipe instanceof StonecutterRecipe ? CRAFTING_TAG_STONECUTTER : CRAFTING_TAG_CRAFTING_TABLE);
        this.putVarInt(recipe.getPriority());
        boolean writeRequirement = !(recipe instanceof StonecutterRecipe);
        this.putBoolean(writeRequirement);
        if (writeRequirement) this.writeRequirementV2168(recipe);
        this.putUnsignedVarInt(recipe.getNetworkId());
    }

    private void writeFurnaceRecipeV2168(FurnaceRecipe recipe) {
        this.putString(recipe.getRecipeId());
        this.putUnsignedVarInt(1);
        recipe.getInput().putRecipe(this, protocol);
        this.putUnsignedVarInt(1);
        this.putSlot(protocol, recipe.getResult(), true);
        this.putUUID(recipe.getId());
        String tag = recipe instanceof BlastFurnaceRecipe ? CRAFTING_TAG_BLAST_FURNACE : recipe instanceof SmokerRecipe ? CRAFTING_TAG_SMOKER : CRAFTING_TAG_FURNACE;
        this.putString(tag);
        this.putVarInt(0);
        this.putBoolean(false);
        this.putUnsignedVarInt(recipe.getNetworkId());
    }

    private void writeRequirementV2168(CraftingRecipe recipe) {
        RecipeUnlockingRequirement requirement = recipe.getRequirement();
        RecipeUnlockingRequirement.UnlockingContext context = requirement.getContext();
        this.putVarInt(context.ordinal());
        boolean present = context == RecipeUnlockingRequirement.UnlockingContext.NONE;
        this.putBoolean(present);
        if (present) this.putArray(requirement.getIngredients(), ingredient -> new DefaultDescriptor(ingredient).putRecipe(this, protocol));
    }
    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    protected void writeRequirement(CraftingRecipe recipe) {
        this.putByte((byte) recipe.getRequirement().getContext().ordinal());
        if (recipe.getRequirement().getContext().equals(RecipeUnlockingRequirement.UnlockingContext.NONE)) {
            this.putArray(recipe.getRequirement().getIngredients(), (ingredient) -> new DefaultDescriptor(ingredient).putRecipe(this, protocol));
        }
    }
}
