package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class ConfigRemovedRecipes {
    public int type_id;
    public int data;


    public static void removeRecipes(FileConfiguration cfg, HashMap<Integer, ConfigRemovedRecipes> removedRecipies) {

        List<Map<?, ?>> configMaterials = cfg.getMapList("removed_recipes");
        for (Map<?, ?> b : configMaterials) {
            ConfigRemovedRecipes item = new ConfigRemovedRecipes();
            item.type_id = (Integer) b.get("type_id");
            item.data = (Integer) b.get("data");

            removedRecipies.put(item.type_id, item);

            ItemStack is = new ItemStack(ItemManager.getMaterial(item.type_id), 1, (short) item.data);
            List<Recipe> backup = new ArrayList<Recipe>();
            // Idk why you change scope, but why not
            Iterator<Recipe> a = Bukkit.getServer().recipeIterator();
            while (a.hasNext()) {
                Recipe recipe = a.next();
                ItemStack result = recipe.getResult();
                if (!result.getData().equals(is.getData()) || !result.getType().equals(is.getType())) {
                    backup.add(recipe);
                }
            }

            Bukkit.getServer().clearRecipes();
            for (Recipe r : backup) {
                Bukkit.getServer().addRecipe(r);
            }
        }
    }

}