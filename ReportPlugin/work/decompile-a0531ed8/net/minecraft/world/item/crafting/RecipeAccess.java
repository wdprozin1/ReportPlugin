package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceKey;

public interface RecipeAccess {

    RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourcekey);

    SelectableRecipe.b<RecipeStonecutting> stonecutterRecipes();
}
