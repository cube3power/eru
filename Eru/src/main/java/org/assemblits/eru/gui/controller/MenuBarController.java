package org.assemblits.eru.gui.controller;

import org.assemblits.eru.entities.Display;
import org.assemblits.eru.gui.exception.EruException;
import org.assemblits.eru.gui.model.ProjectModel;
import org.assemblits.eru.jfx.scenebuilder.SceneFxmlManager;
import org.assemblits.eru.persistence.ProjectRepository;
import org.assemblits.eru.preferences.EruPreferences;
import org.assemblits.eru.util.TagLinksManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuBarController {

    private final TagLinksManager tagLinksManager;
    private final ProjectRepository projectRepository;
    private final ApplicationContext applicationContext;
    private final SceneFxmlManager sceneFxmlManager;
    private final EruPreferences eruPreferences;

    private ProjectModel projectModel;

    public void saveMenuItemSelected() {
        log.info("Saving {}", projectModel);
        projectRepository.save(projectModel.get());
    }

    public void exitMenuItemSelected() {
        Platform.exit();
    }

    public void preferencesMenuItemSelected() {
        Stage preferencesStage = new Stage();
        preferencesStage.setTitle("Preferences");
        preferencesStage.setScene(new Scene(loadNode("/views/Preferences.fxml")));
        preferencesStage.getScene().getStylesheets().add(eruPreferences.getTheme().getValue().getStyleSheetURL());
        eruPreferences.getTheme().addListener((observable, oldTheme, newTheme) ->  {
            preferencesStage.getScene().getStylesheets().clear();
            preferencesStage.getScene().getStylesheets().add(newTheme.getStyleSheetURL());
        });
        preferencesStage.showAndWait();
    }

    public void launchScadaMenuItemSelected() {
        try {
            log.info("Launching displays.");
            final Display mainDisplay = projectModel.getDisplays().stream().filter(Display::isInitialDisplay).findAny().get();
            final File sceneFxmlFile = sceneFxmlManager.createSceneFxmlFile(mainDisplay);
            final URL fxmlFileUrl = sceneFxmlFile.toURI().toURL();
            final Parent mainNode = FXMLLoader.load(fxmlFileUrl);
            final Scene SCADA_SCENE = new Scene(mainNode);
            final Stage SCADA_STAGE = new Stage();
            SCADA_STAGE.setScene(SCADA_SCENE);
            SCADA_STAGE.show();
            tagLinksManager.linkToDisplay(mainNode); //TODO: Remove this and do it inside TagLinksManager using listeners
            log.info("Linking to scada was successful.");
        } catch (Exception e) {
            log.error("Error launching display", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error in SCADA launching process.");
            alert.setContentText("There is no Main display created.");
            alert.show();
        }
    }

    public void aboutMenuItemSelected() {
        Stage aboutStage = new Stage();
        aboutStage.setScene(new Scene(loadNode("/views/About.fxml")));
        aboutStage.showAndWait();
    }

    public void setProjectModel(ProjectModel projectModel) {
        this.projectModel = projectModel;
    }

    private Parent loadNode(String fxmlViewName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(fxmlViewName));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            return fxmlLoader.load();
        } catch (IOException e) {
            String errorMessage = format("Error loading %s screen", fxmlViewName);
            log.error(errorMessage);
            throw new EruException(errorMessage, e);
        }
    }
}