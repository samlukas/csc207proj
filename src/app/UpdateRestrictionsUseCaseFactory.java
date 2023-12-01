package app;
import entities.UserDietaryPreferences;
import entities.UserFactory;
import interface_adapter.ViewManagerModel;
import entities.User;
import app.MainMenuUseCaseFactory;
import interface_adapter.main_menu.MainMenuController;
import interface_adapter.update_restrictions.*;
import use_case.update_restrictions.*;
import view.UpdateRestrictionsView;
import interface_adapter.main_menu.MainMenuViewModel;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;

public class UpdateRestrictionsUseCaseFactory {
    private UpdateRestrictionsUseCaseFactory() {}

    public static UpdateRestrictionsView create(
            ViewManagerModel viewManagerModel,
            UpdateRestrictionsViewModel updateRestrictionsViewModel, UpdateRestrictionsDataAccessInterface updateRestrictionsDataAccessInterface, MainMenuController mainMenuController, MainMenuViewModel mainMenuViewModel) {

        try {
            UpdateRestrictionsController updateRestrictionsController = createUpdateRestrictionsUseCase(viewManagerModel, updateRestrictionsViewModel, updateRestrictionsDataAccessInterface);
            return new UpdateRestrictionsView(updateRestrictionsController, updateRestrictionsViewModel, mainMenuController, mainMenuViewModel);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not open user data file.");
        }

        return null;
    }

    private static UpdateRestrictionsController createUpdateRestrictionsUseCase(
            ViewManagerModel viewManagerModel,
            UpdateRestrictionsViewModel updateRestrictionsViewModel, UpdateRestrictionsDataAccessInterface updateRestrictionsDataAccessInterface) throws IOException {

        // Notice how we pass this method's parameters to the Presenter.
        UpdateRestrictionsOutputBoundary updateRestrictionsOutputBoundary = new UpdateRestrictionsPresenter(
                updateRestrictionsViewModel, viewManagerModel);


        //possibly temp
        UserDietaryPreferences user = new UserDietaryPreferences(new HashMap<>());
        UserFactory userfactory = new UserFactory();


        UpdateRestrictionsInteractor updateRestrictionsInteractor = new UpdateRestrictionsInteractor(updateRestrictionsDataAccessInterface, updateRestrictionsOutputBoundary, user, userfactory);

        return new UpdateRestrictionsController(updateRestrictionsInteractor);
    }
}