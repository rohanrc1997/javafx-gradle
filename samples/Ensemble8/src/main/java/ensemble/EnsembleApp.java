/*
 * Copyright (c) 2008, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ensemble;


import ensemble.control.Popover;
import ensemble.control.SearchBox;
import ensemble.control.TitledToolBar;
import ensemble.generated.Samples;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Ensemble Application
 */
public class EnsembleApp extends Application {
    private static final String OS_NAME = System.getProperty("ensemble.os.name", System.getProperty("os.name"));
    private static final String OS_ARCH = System.getProperty("ensemble.os.arch", System.getProperty("os.arch"));
    public static final boolean IS_BEAGLE = "arm".equals(OS_ARCH);
    public static final boolean IS_IOS = "Darwin".equals(OS_NAME);
    public static final boolean IS_IPHONE = false;
    public static final boolean IS_DESKTOP = !IS_BEAGLE && !IS_IOS;
    public static final boolean IS_MAC = OS_NAME.startsWith("Mac");
    public static final boolean PRELOAD_PREVIEW_IMAGES = true;
    public static final boolean SHOW_HIGHLIGHTS = IS_DESKTOP;
    public static final boolean SHOW_MENU = IS_DESKTOP;
    public static final boolean SELECT_IOS_THEME = IS_MAC || IS_IOS;
    private static final int TOOL_BAR_BUTTON_SIZE = 30;
    private Scene scene;
    private Pane root;
    private TitledToolBar toolBar;
    private Button backButton;
    private Button forwardButton;
    private Button homeButton;
    private ToggleButton listButton;
    private ToggleButton searchButton;
    private SearchBox searchBox = new SearchBox();
    private PageBrowser pageBrowser;
    private Popover sampleListPopover;
    private SearchPopover searchPopover;
    private MenuBar menuBar;
    
    static {
        System.out.println("IS_IPHONE = " + IS_IPHONE);
        System.out.println("IS_MAC = " + IS_MAC);
        System.out.println("IS_IOS = " + IS_IOS);
        System.out.println("IS_BEAGLE = " + IS_BEAGLE);
        System.out.println("IS_DESKTOP = " + IS_DESKTOP);
        System.setProperty("http.proxyHost", "www-proxy.us.oracle.com");
        System.setProperty("http.proxyPort", "80");
        System.setProperty("https.proxyHost", "www-proxy.us.oracle.com");
        System.setProperty("https.proxyPort", "80");
    }

    @Override public void init() throws Exception {
        // LOAD FONTS
    //    Font font = Font.loadFont(EnsembleApp.class.getResource("fonts/BreeSerif-Regular.ttf").toString(),10);
     //   System.out.println("font = " + font);
        // CREATE ROOT
        root = new Pane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                final double w = getWidth();
                final double h = getHeight();
                final double menuHeight = SHOW_MENU ? menuBar.prefHeight(w) : 0;
                final double toolBarHeight = toolBar.prefHeight(w);
                if (menuBar != null) {
                    menuBar.resize(w, menuHeight);
                }
                toolBar.resizeRelocate(0, menuHeight, w, toolBarHeight);
                pageBrowser.setLayoutY(toolBarHeight + menuHeight);
                pageBrowser.resize(w, h-toolBarHeight);
                pageBrowser.resize(w, h - toolBarHeight - menuHeight);
                sampleListPopover.autosize();
                Point2D listBtnBottomCenter = listButton.localToScene(listButton.getWidth()/2, listButton.getHeight());
                sampleListPopover.setLayoutX((int)listBtnBottomCenter.getX()-50);
                sampleListPopover.setLayoutY((int)listBtnBottomCenter.getY()+20);
                Point2D searchBoxBottomCenter = searchBox.localToScene(searchBox.getWidth()/2, searchBox.getHeight());
                searchPopover.setLayoutX((int)searchBoxBottomCenter.getX()-searchPopover.getLayoutBounds().getWidth()+50);
                searchPopover.setLayoutY((int)searchBoxBottomCenter.getY()+20);
            }
        };
        // CREATE MENUBAR/STATUSBAR SPACER
        if (IS_IOS) {
            Region statusSpacer = new Region();
            statusSpacer.setPrefHeight(20);
            root.getChildren().add(statusSpacer);
        }
        if (SHOW_MENU) {
            menuBar = new MenuBar();
            menuBar.setUseSystemMenuBar(true);
            Menu themeMenu = new Menu("Theme");
            final RadioMenuItem caspianThemeMenuItem = new RadioMenuItem("Caspian");
            final RadioMenuItem iOSThemeMenuItem = new RadioMenuItem("iOS");
            final ToggleGroup tg = new ToggleGroup();
            caspianThemeMenuItem.setToggleGroup(tg);
            iOSThemeMenuItem.setToggleGroup(tg);
            tg.selectToggle(SELECT_IOS_THEME ? iOSThemeMenuItem : caspianThemeMenuItem);
            tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override public void changed(ObservableValue<? extends Toggle> arg0, Toggle oldValue, Toggle newValue) {
                    if (newValue != null) {
                        setStylesheets(newValue == iOSThemeMenuItem);
                    }
                }
            });
            themeMenu.getItems().addAll(caspianThemeMenuItem,iOSThemeMenuItem);
            menuBar.getMenus().add(themeMenu);
            
            ToggleGroup screenSizeToggle = new ToggleGroup();
            menuBar.getMenus().add(
                    MenuBuilder.create()
                        .text("Screen size")
                        .items(
                            screenSizeMenuItem("iPad Landscape", 1024, 768, false, screenSizeToggle),
                            screenSizeMenuItem("iPad Portrait", 768, 1024, false, screenSizeToggle),
                            screenSizeMenuItem("Beagleboard", 1024, 600, false, screenSizeToggle),
                            screenSizeMenuItem("iPad Retina Landscape", 2048, 1536, true, screenSizeToggle),
                            screenSizeMenuItem("iPad Retina Portrait", 1536, 2048, true, screenSizeToggle),
                            screenSizeMenuItem("iPhone Landscape", 480, 320, false, screenSizeToggle),
                            screenSizeMenuItem("iPhone Portrait", 320, 480, false, screenSizeToggle),
                            screenSizeMenuItem("iPhone 4 Landscape", 960, 640, true, screenSizeToggle),
                            screenSizeMenuItem("iPhone 4 Portrait", 640, 960, true, screenSizeToggle),
                            screenSizeMenuItem("iPhone 5 Landscape", 1136, 640, true, screenSizeToggle),
                            screenSizeMenuItem("iPhone 5 Portrait", 640, 1136, true, screenSizeToggle)
                        )
                        .build());
            screenSizeToggle.selectToggle(screenSizeToggle.getToggles().get(0));
            
            root.getChildren().add(menuBar);
        }
        // CREATE TOOLBAR
        toolBar = new TitledToolBar();
        root.getChildren().add(toolBar);
        backButton = new Button();
        backButton.setId("back");
        backButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        forwardButton = new Button();
        forwardButton.setId("forward");
        forwardButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        homeButton = new Button();
        homeButton.setId("home");
        homeButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        listButton = new ToggleButton();
        listButton.setId("list");
        listButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        HBox.setMargin(listButton, new Insets(0, 0, 0, 7));
        searchButton = new ToggleButton();
        searchButton.setId("search");
        searchButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        searchBox.setPrefWidth(200);
        if (!IS_IOS) {
            backButton.setGraphic(new Region());
            forwardButton.setGraphic(new Region());
            homeButton.setGraphic(new Region());
            listButton.setGraphic(new Region());
            searchButton.setGraphic(new Region());
        }
        toolBar.addLeftItems(backButton,forwardButton,homeButton,listButton);
        toolBar.addRightItems(searchBox);

        // create PageBrowser
        pageBrowser = new PageBrowser();
        toolBar.titleTextProperty().bind(pageBrowser.currentPageTitleProperty());
        root.getChildren().add(0, pageBrowser);
        pageBrowser.goHome();
        // wire nav buttons
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                pageBrowser.backward();
            }
        });
        backButton.disableProperty().bind(pageBrowser.backPossibleProperty().not());
        forwardButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                pageBrowser.forward();
            }
        });
        forwardButton.disableProperty().bind(pageBrowser.forwardPossibleProperty().not());
        homeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                pageBrowser.goHome();
            }
        });
        homeButton.disableProperty().bind(pageBrowser.atHomeProperty());
        
        // create and setup list popover
        sampleListPopover = new Popover();
        sampleListPopover.setPrefWidth(440);
        root.getChildren().add(sampleListPopover);
        final SamplePopoverTreeList rootPage = new SamplePopoverTreeList(Samples.ROOT,pageBrowser);
        listButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if (sampleListPopover.isVisible()) {
                    sampleListPopover.hide();
                } else {
                    sampleListPopover.clearPages();
                    sampleListPopover.pushPage(rootPage);
                    sampleListPopover.show(new Runnable() {
                        @Override public void run() {
                            listButton.setSelected(false);
                        }
                    });
                }
            }
        });
        
        // create and setup search popover
        searchPopover = new SearchPopover(searchBox,pageBrowser);
        root.getChildren().add(searchPopover);
    }

    private RadioMenuItem screenSizeMenuItem(String text, final int width, final int height, final boolean retina, ToggleGroup tg) {
        return RadioMenuItemBuilder.create()
                .toggleGroup(tg)
                .text(text + " " + width + "x" + height)
                .onAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent t) {
                        double menuHeight = IS_IOS || IS_MAC ? 0 : menuBar.prefHeight(width);
                        scene.getWindow().setWidth(width + scene.getWindow().getWidth() - scene.getWidth());
                        scene.getWindow().setHeight(height + menuHeight + scene.getWindow().getHeight() - scene.getHeight());
                        if (retina) {
                            Parent root = scene.getRoot();
                            if (root instanceof Pane) {
                                Group newRoot = new Group();
                                newRoot.setAutoSizeChildren(false);
                                scene.setRoot(newRoot);
                                newRoot.getChildren().add(root);
                                root.getTransforms().add(new Scale(2, 2, 0, 0));
                                root.resize(width/2, height/2);
                            } else {
                                root.getChildrenUnmodifiable().get(0).resize(width/2, height/2);
                            }
                        } else {
                            Parent root = scene.getRoot();
                            if (root instanceof Group) {
                                Pane oldRoot = (Pane)root.getChildrenUnmodifiable().get(0);
                                ((Group)root).getChildren().clear();
                                oldRoot.getTransforms().clear();
                                scene.setRoot(oldRoot);
                            }
                        }
                    }
                })
                .build();
    }

    private void setStylesheets(boolean isIOsSelected) {
        List<String> stylesheets = new ArrayList<>(5);
        String base = "/ensemble/EnsembleStyles" + (isIOsSelected ? "IOS" : "Caspian");
        stylesheets.add("/ensemble/EnsembleStylesCommon.css");
        stylesheets.add(base + ".css");
        if (IS_DESKTOP) {
            stylesheets.add("/ensemble/EnsembleStylesCommonDesktop.css");
            stylesheets.add(base + "Desktop.css");
        }
        if (IS_MAC || IS_IOS) {
            stylesheets.add("/ensemble/EnsembleStylesHelvetica.css");
        }                    
        stylesheets.add("http://fonts.googleapis.com/css?family=Bree+Serif");
        scene.getStylesheets().setAll(stylesheets);
    }    
    
    @Override public void start(final Stage stage) throws Exception {
        // CREATE SCENE
        scene = new Scene(root, 1024, 768, Color.BLACK);
        if (IS_BEAGLE) {
            new ScrollEventSynthesizer(scene);
        }
        setStylesheets(SELECT_IOS_THEME);
        stage.setScene(scene);
        // SHOW STAGE
        if (IS_IOS) {
            setupIosStage(stage, scene);
        }
        // START FULL SCREEN IF WANTED
        if (PlatformFeatures.START_FULL_SCREEN) {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
        stage.setTitle("Ensemble");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void setupIosStage(Stage stage, Scene scene) {}
//    private void setupIosStage(final Stage stage,final Scene scene) {
//        // Synthisize Scroll Events from mouse drag events
//        IosScrollEventSynthesizer scrollEventSynthesizer = new IosScrollEventSynthesizer(scene);
//        javafx.ext.ios.IOSApplication.setStatusBarStyleAnimated(javafx.ext.ios.StatusBarStyle.DEFAULT, false);
//        final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
//        final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();
//        stage.setX(0.0); //to avoid centerOnScreen() calculations
//        stage.setY(0.0);
//        stage.setWidth(SCREEN_WIDTH);
//        stage.setHeight(SCREEN_HEIGHT);
//        Screen.getScreens().addListener(new ListChangeListener(){
//            @Override public void onChanged(ListChangeListener.Change change) {
//                if(Screen.getScreens().size() > 0) {
//                    switch(Screen.getPrimary().getOrientation()) {
//                        case Screen.OrientationPortraitUpsideDown:
//                        case Screen.OrientationPortrait:
//                            stage.setWidth(SCREEN_WIDTH);
//                            stage.setHeight(SCREEN_HEIGHT);
//                            break;
//                        case Screen.OrientationLandscapeLeft:
//                        case Screen.OrientationLandscapeRight:
//                            stage.setWidth(SCREEN_HEIGHT);
////                                stage.setHeight(SCREEN_WIDTH);
//                            break;
//                    }
//                    switch(Screen.getPrimary().getOrientation()) {
//                        case Screen.OrientationPortraitUpsideDown:
//                            javafx.ext.ios.IOSApplication.setStatusBarOrientationAnimated(javafx.ext.ios.StatusBarOrientation.PORTRAIT_UPSIDE_DOWN, false);
//                            break;
//                        case Screen.OrientationPortrait:
//                            javafx.ext.ios.IOSApplication.setStatusBarOrientationAnimated(javafx.ext.ios.StatusBarOrientation.PORTRAIT, false);
//                            break;
//                        case Screen.OrientationLandscapeLeft:
//                            javafx.ext.ios.IOSApplication.setStatusBarOrientationAnimated(javafx.ext.ios.StatusBarOrientation.LANDSCAPE_RIGHT, false);
//                            break;
//                        case Screen.OrientationLandscapeRight:
//                            javafx.ext.ios.IOSApplication.setStatusBarOrientationAnimated(javafx.ext.ios.StatusBarOrientation.LANDSCAPE_LEFT, false);
//                            break;
//                    }
//                }
//            }
//        });
//    }
}
