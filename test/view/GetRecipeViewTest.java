package view;

import data_access.FileUserDataAccessObject;
import data_access.RecipeGetter;
import data_access.RecipeParser;
import entities.*;
import interface_adapter.ViewManagerModel;
import interface_adapter.get_recipe.GetRecipeController;
import interface_adapter.get_recipe.GetRecipeState;
import interface_adapter.get_recipe.GetRecipeViewModel;
import interface_adapter.get_shopping_list.GetShoppingListController;
import interface_adapter.get_shopping_list.GetShoppingListState;
import interface_adapter.get_shopping_list.GetShoppingListViewModel;
import interface_adapter.main_menu.MainMenuController;
import interface_adapter.main_menu.MainMenuState;
import interface_adapter.main_menu.MainMenuViewModel;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.get_recipe.GetRecipeDataAccessInterface;
import use_case.get_recipe.GetRecipeInputBoundary;
import use_case.get_recipe.GetRecipeOutputBoundary;
import use_case.get_recipe.GetRecipeOutputData;
import use_case.main_menu.MainMenuInputBoundary;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GetRecipeView class
 */
class GetRecipeViewTest {
    private MainMenuInputBoundary mainMenuInputBoundary;
    private GetRecipeView getRecipeView;
    private MainMenuViewModel mainMenuViewModel;
    private MainMenuController mainMenuController;
    private ViewManagerModel viewManagerModel;
    private GetRecipeViewModel getRecipeViewModel;
    private GetRecipeController getRecipeController;
    private GetRecipeInputBoundary getRecipeInputBoundary;
    private GetShoppingListViewModel getShoppingListViewModel;
    private GetShoppingListController getShoppingListController;
    private GetRecipeDataAccessInterface getRecipeDataAccessInterface;
    private GetRecipeOutputBoundary getRecipeOutputBoundary;

    /**
     * A test implementation of the GetRecipeDataAccessInterface
     */
    private class TestGetRecipeDataAccessInterface implements GetRecipeDataAccessInterface {
        private User user;
        private final String key = "c741074bc5c14385ba37b35cf3416734";

        /**
         * Constructor for the TestGetRecipeDataAccessInterface
         * @param user: the user to be used for the test
         */
        public TestGetRecipeDataAccessInterface(User user) {
            this.user = user;
        }

        /**
         * Returns the user's dietary preferences
         */
        @Override
        public DietaryPreferences retrievePreferences() {
            return user.getDietaryRestrictions();
        }

        /**
         * Returns a list of recipes that meet the user's dietary preferences
         */
        @Override
        public List<Recipe> retrieveRecipes(DietaryPreferences preferences) {
            InventoryChecker checker = new InventoryChecker();
            RecipeGetter getter = new RecipeGetter();
            RecipeParser parser = new RecipeParser();

            List<FoodItem> expiresSoon = checker.weekCheck(user.getInventory());
            List<Object> settings = getter.preferenceConverter(expiresSoon, preferences);
            JSONObject recipeInfo = getter.getRecipe(key, settings);
            List<String> titles = parser.getNames(recipeInfo);
            List<Integer> ids = parser.getIds(recipeInfo);
            List<Recipe> res = new ArrayList<>();

            int i = 0;
            for (Integer id: ids) {
                List<FoodItem> ingredients = parser.parseIngredients(getter.getIngredients(id, key));
                Map<String, Float> macros = parser.parseMacros(getter.getNutrients(id, key));
                List<String> instructions = parser.parseInstructions(getter.getInstructions(id, key));
                res.add(new Recipe(titles.get(i), instructions, ingredients, macros));
                i++;
            }

            return res;
        }
    }

    /**
     * Sets up the test environment
     */
    @BeforeEach
    void setUp() {
        getRecipeViewModel = new GetRecipeViewModel();
        getShoppingListViewModel = new GetShoppingListViewModel();
        getRecipeInputBoundary = new GetRecipeInputBoundary() {

            /**
             * Executes the use case
             */
            @Override
            public void execute() {
                DietaryPreferences dietaryPreferences = getRecipeDataAccessInterface.retrievePreferences();
                List<Recipe> recipes = getRecipeDataAccessInterface.retrieveRecipes(dietaryPreferences);

                if (recipes.isEmpty()) {
                    // when there is no existing recipe in the database that meets the user preferences
                    getRecipeOutputBoundary.prepareFailView("no available recipes");
                } else {
                    GetRecipeOutputData getRecipeOutputData = new GetRecipeOutputData(recipes, false);
                    getRecipeOutputBoundary.prepareSuccessView(getRecipeOutputData);
                }
            }
        };
        getRecipeOutputBoundary = new GetRecipeOutputBoundary() {
            /**
             * A test implementation of the GetRecipeOutputBoundary
             */
            @Override
            public void prepareSuccessView(GetRecipeOutputData name) {
                Recipe recipe = new Recipe("Cannellini Bean and Asparagus Salad with Mushrooms", new ArrayList<>(), new ArrayList<>(), new HashMap<>());
                List<Recipe> result = new ArrayList<>();
                result.add(recipe);
                GetRecipeOutputData getRecipeOutputData = new GetRecipeOutputData(result, false);
                GetRecipeState state = getRecipeViewModel.getState();
                state.updateState(getRecipeOutputData.getRecipeData(), null);
                getRecipeViewModel.setState(state);
                getRecipeViewModel.firePropertyChange();
            }

            /**
             * A test implementation of the GetRecipeOutputBoundary
             */
            @Override
            public void prepareFailView(String error) {
                GetRecipeState state = getRecipeViewModel.getState();
                state.setRecipeError(error);
                getRecipeViewModel.setState(state);
                getRecipeViewModel.firePropertyChange();
            }
        };
        getRecipeController = new GetRecipeController(getRecipeInputBoundary);

        mainMenuInputBoundary = new MainMenuInputBoundary() {
            /**
             * A test implementation of the MainMenuInputBoundary
             */
            @Override
            public void execute(String view_name) {
                MainMenuState currentState = mainMenuViewModel.getState();
                currentState.setView_name(view_name);
                mainMenuViewModel.firePropertyChange();
            }
        };

        mainMenuController = new MainMenuController(mainMenuInputBoundary);
        mainMenuViewModel = new MainMenuViewModel();

        getRecipeView = new GetRecipeView(viewManagerModel,
                getRecipeViewModel,
                getRecipeController,
                getShoppingListViewModel,
                getShoppingListController,
                mainMenuViewModel,
                mainMenuController);

        UserFactory userFactory = new UserFactory();
        Map<String, Float> pref = new HashMap<>();
        pref.put("maxCarbs", 100f);
        pref.put("minCarbs", 10f);
        pref.put("maxProtein", 100f);
        pref.put("minProtein", 10f);
        pref.put("maxCalories", 800f);
        pref.put("minCalories", 50f);
        pref.put("maxSaturatedFat", 100f);
        pref.put("minSaturatedFat", 0f);
        User user1 = userFactory.create(pref);
        getRecipeDataAccessInterface = new TestGetRecipeDataAccessInterface(user1);
    }

    /**
     * Tests if the view name is correct
     */
    @Test
    void testViewName() {
        assertEquals("get recipe", getRecipeView.viewName);
    }

    /**
     * Tests if the main menu button is correct
     */
    @Test
    void testMainMenuButton() {
        assertEquals("Main Menu", getRecipeView.MainMenu.getText());
    }

    /**
     * Tests if the generate button is correct
     */
    @Test
    void testGenerateButton() {
        assertEquals("Generate Recipe", getRecipeView.generate.getText());
    }

    /**
     * Tests if the recipes panel is correct
     */
    @Test
    void testRecipesPanel() {
        assertNotNull(getRecipeView.recipesPanel);
    }

    /**
     *  Tests if the main menu button is initialized correctly
     */
    @Test
    void testMainMenuButtonPressed() {
        getRecipeView.MainMenu.doClick();
        assertEquals("main menu", mainMenuViewModel.getState().getView_name());
    }

    /**
     * Tests if the generate button is initialized correctly
     */
    @Test
    void testGenerateButtonPressedNoError() {
        getRecipeView.generate.doClick();
        assert(!getRecipeViewModel.getState().getRecipes().isEmpty());
    }

    /**
     * Tests if the generate button is initialized correctly when there is an error
     */
    @Test
    void testGenerateButtonPressedWithError() {
        UserFactory userFactory = new UserFactory();
        Map<String, Float> pref = new HashMap<>();
        pref.put("maxCarbs", 100f);
        pref.put("minCarbs", 10f);
        pref.put("maxProtein", 100f);
        pref.put("minProtein", 10f);
        pref.put("maxCalories", 800f);
        pref.put("minCalories", 50f);
        pref.put("maxSaturatedFat", 100f);
        pref.put("minSaturatedFat", 0f);
        User user2 = userFactory.create(pref);
        user2.addItem(new FoodItem("banana", 2023, 12, 5, 1));
        user2.addItem(new FoodItem("lobster", 2023, 12, 5, 1));
        getRecipeDataAccessInterface = new TestGetRecipeDataAccessInterface(user2);
        getRecipeView.generate.doClick();
        assertEquals("no available recipes", getRecipeViewModel.getState().getError());
    }

    /**
     * Tests if the shopping list view is initialized correctly
     */
    @Test
    void testShoppingListView() {
        getRecipeView.generate.doClick();
        assertEquals("Get Shopping List", getShoppingListViewModel.getViewName());
    }

    /**
     * Tests if the shopping list button is initialized correctly when there is an error
     */
    @Test
    void testShoppingListButtonPressedError() {
        getRecipeView.generate.doClick();
        GetShoppingListState state = getShoppingListViewModel.getState();
        state.setShoppingListError("Test Error");
        getShoppingListViewModel.firePropertyChange();
        assertEquals("Test Error", getShoppingListViewModel.getState().getError());
    }

}