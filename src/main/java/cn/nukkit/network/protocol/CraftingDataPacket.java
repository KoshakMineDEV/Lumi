package cn.nukkit.network.protocol;

import cn.nukkit.recipe.descriptor.DefaultDescriptor;
import cn.nukkit.recipe.descriptor.ItemDescriptor;
import cn.nukkit.recipe.descriptor.ItemTagDescriptor;
import cn.nukkit.recipe.impl.data.RecipeUnlockingRequirement;
import cn.nukkit.item.Item;
import cn.nukkit.item.material.tags.ItemTags;
import cn.nukkit.recipe.*;
import cn.nukkit.recipe.impl.*;
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

    public void addFurnaceRecipe(FurnaceRecipe... recipe) {
        Collections.addAll(entries, recipe);
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
        this.putUnsignedVarInt(protocol >= ProtocolInfo.v1_20_0_23 ? entries.size() + 1 : entries.size());//1.20.0+ 有额外的smithing_trim

        for (Recipe recipe : entries) {
            this.putVarInt(recipe.getType().getNetworkType());
            switch (recipe.getType()) {
                case SHAPELESS:
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
                    if (protocol >= 354) {
                        this.putString(CRAFTING_TAG_CRAFTING_TABLE);
                        if (protocol >= 361) {
                            this.putVarInt(shapeless.getPriority());
                            if (protocol >= 407) {
                                if (protocol >= ProtocolInfo.v1_21_0) {
                                    this.writeRequirement(shapeless);
                                }
                                this.putUnsignedVarInt(shapeless.getNetworkId());
                            }
                        }
                    }
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
                    if (protocol >= 361) {
                        this.putString(shaped.getRecipeId());
                    }
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
                    if (protocol >= 354) {
                        this.putString(CRAFTING_TAG_CRAFTING_TABLE);
                        if (protocol >= 361) {
                            this.putVarInt(shaped.getPriority());
                            if (this.protocol >= ProtocolInfo.v1_20_80) {
                                this.putBoolean(shaped.isAssumeSymetry());
                            }
                            if (protocol >= 407) {
                                if (protocol >= ProtocolInfo.v1_21_0) {
                                    this.writeRequirement(shaped);
                                }
                                this.putUnsignedVarInt(shaped.getNetworkId());
                            }
                        }
                    }
                    break;
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    Item input = furnace.getInput();
                    this.putVarInt(input.getId());
                    if (recipe.getType() == RecipeType.FURNACE_DATA) {
                        this.putVarInt(input.getDamage());
                    }
                    this.putSlot(protocol, furnace.getResult(), true);
                    if (protocol >= 354) {
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
        this.putUnsignedVarInt(1);

        if (protocol >= 388) {
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
        }

        this.putBoolean(cleanRecipes);
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
