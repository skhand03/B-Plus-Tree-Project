package source;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Shubham on 11/09/18.
 */

public class BPlusTreeUI extends Application {

    /* AddFile || SaveFile */
    Button addFile;
    Button saveFile;

    /* File - */
    File getFile;
    File finalFile;

    /* BPlusTreeUI Body Box */
    VBox mainBodyBox;

    /* Search */
    TextField searchField;
    Button searchButton;

    /* Insert */
    TextField insertKeyField;
    TextField insertValueField;
    Button insertButton;

    /* Update */
    TextField updateValueField;
    Button updateButton;

    /* Delete */
    TextField deleteField;
    Button deleteButton;

    /* The Next 10 Part Description */
    VBox nextPartBox;

    /* Status */
    Label statusHeader;
    Label statusBody;

    /* Description */
    HBox descriptionBox;
    VBox totalNumberOfSplitsVBox;
    VBox parentSplitsVBox;
    VBox fusionsVBox;
    VBox parentFusionsVBox;
    VBox theTreeDepthVBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {

        /* The B-tree will allow between 2 and 4 keys per index node. Each leaf node can store 16 records. */
        final BPlusTree<String, String> bPlusTree = new BPlusTree<String, String>(4);

        /* BPlusTreeUI Layout */
        final BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("body");

        /* Create a File Chooser */
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        /* Add File | Title | Save File */
        final HBox headerBox = new HBox(50);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20, 0, 20, 0));

        /* Add File - Button */
        addFile = new Button("Add File");
        addFile.getStyleClass().add("addFile");
        addFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                fileChooser.setTitle("Add File");
                getFile = fileChooser.showOpenDialog(primaryStage);
                if (getFile != null) {
                    try {
                        // Read the File
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(getFile));
                        String data;
                        while ((data = bufferedReader.readLine()) != null) {
                            // Columns
                            // 1-7 Part ID - Note: It was from 0 to 7
                            // 16-80 Part Description
                            // Parse the file and loading data from the flat-file into a B+-tree.
                            String partId = data.substring(0, 7).trim();
                            String partDescription = data.substring(15).trim();
                            bPlusTree.insert(partId, partDescription);
                        }

                        /* Get First Leaf Key */
                        statusHeader.setText("First Leaf Key");
                        statusBody.setText(bPlusTree.getFirstLeafKey());

//                        System.out.println("BPlusTree: " + bPlusTree.search("JYJ-355"));
//                        System.out.println("BPlusTree - toString: " + bPlusTree.toString());
                        System.out.println("BPlusTree - Splits: " + bPlusTree.getSplits());
                        System.out.println("BPlusTree - Fusions: " + bPlusTree.getFusions());
                        System.out.println("BPlusTree - Depth: " + bPlusTree.getDepth());
//                        System.out.println("BPlusTree - FirstLeafKey: " + bPlusTree.getFirstLeafKey());
//                        System.out.println("Test " + bPlusTree.searchRange("AAA-676", BPlusTree.RangePolicy.INCLUSIVE));
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        });
        headerBox.getChildren().add(addFile);

        /* Title */
        Label title = new Label("Parts Catalog");
        title.setFont(Font.font("Helvetica", FontWeight.NORMAL, 38));
        title.getStyleClass().add("title");
        title.setPadding(new Insets(0, 100, 0, 100));
        title.setTextAlignment(TextAlignment.CENTER);
        HBox.setHgrow(title, Priority.ALWAYS);
        headerBox.getChildren().add(title);

        /* Save File Button */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                saveFile = new Button("Save File");
                saveFile.getStyleClass().add("button");
                saveFile.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        closeProgram(bPlusTree, primaryStage);
                    }
                });
                headerBox.getChildren().add(saveFile);
            }
        });

        borderPane.setTop(headerBox); // Header to the top of the BorderPane

        /*
        Use the data to query
        Once loaded, the user can query for a particular part number , , ,
        */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainBodyBox = new VBox(15);
                mainBodyBox.setAlignment(Pos.CENTER);

                /* Search Box */
                HBox searchHbox = new HBox(20);
                searchHbox.setPadding(new Insets(0, 20, 0, 20));

                /* Search Label */
                Label searchLabel = new Label("Search: ");
                searchLabel.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 38));
                searchLabel.getStyleClass().add("descriptionHeader");
                searchLabel.setPadding(new Insets(0, 10, 0, 10));
                searchLabel.setAlignment(Pos.CENTER);

                /* Search Textfield */
                searchField = new TextField();
                HBox.setHgrow(searchField, Priority.ALWAYS);

                /* Search Button */
                searchButton = new Button("Search");
                searchButton.setPadding(new Insets(0, 10, 0, 10));
                searchButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String searchKey = searchField.getText();
                        String partDescription = bPlusTree.search(searchKey);

                        /* Remove the Next Part IDs if mainBodyBox Exists */
                        mainBodyBox.getChildren().remove(nextPartBox);

                        /* Update Status */
                        if (partDescription != null) {
                            search(searchKey, partDescription);

                            /* Title Label - Display the next 10 parts
                             */
                            nextPartBox = new VBox(15);
                            nextPartBox.setAlignment(Pos.CENTER);
                            nextPartBox.getStyleClass().add("description");

                            Label nextPartTitle = new Label("The Next 10 Part IDs");
                            VBox.setVgrow(nextPartTitle, Priority.ALWAYS);
                            nextPartTitle.getStyleClass().add("partHeader");
                            nextPartTitle.setPadding(new Insets(10, 0, 0, 0));
                            nextPartTitle.setTextAlignment(TextAlignment.CENTER);
                            HBox.setHgrow(nextPartTitle, Priority.ALWAYS);
                            nextPartBox.getChildren().add(nextPartTitle);

                            /* List for the next 10 parts */
                            final ListView<String> listView = new ListView<String>();
                            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e) {
                                    String searchKey = listView.getSelectionModel().getSelectedItem();
                                    String partDescription = bPlusTree.search(searchKey);

                                    search(searchKey, partDescription);
                                }
                            });
                            List<String> list = bPlusTree.searchRange(searchKey, BPlusTree.RangePolicy.INCLUSIVE);
                            listView.getItems().addAll(list);
                            nextPartBox.getChildren().add(listView);
                            mainBodyBox.getChildren().add(4, nextPartBox);
                        } else {
                            statusHeader.setText("Search Failed");
                            statusBody.setText("Part ID: " + searchKey + " - NOT FOUND");

                            /* Delete */
                            deleteField.setText("Part ID");

                            /* Insert */
                            insertKeyField.setText("Part ID");
                            insertValueField.setText("Part Description");

                            /* Update Value */
                            updateValueField.setText("Part Description");
                        }

                    }
                });
                searchHbox.getChildren().add(searchLabel);
                searchHbox.getChildren().add(searchField);
                searchHbox.getChildren().add(searchButton);
                mainBodyBox.getChildren().add(searchHbox);

                /* Insert Box - HBox
                 * Add new parts, and delete parts. */
                HBox insertHbox = new HBox(20);
                insertHbox.setPadding(new Insets(0, 20, 0, 20));

                /* Insert Label */
                Label insertLabel = new Label("Insert: ");
                insertLabel.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 38));
                insertLabel.getStyleClass().add("descriptionHeader");
                insertLabel.setPadding(new Insets(0, 10, 0, 10));
                insertLabel.setAlignment(Pos.CENTER);

                /* Insert Key Textfield */
                insertKeyField = new TextField("Part ID");
                HBox.setHgrow(insertKeyField, Priority.ALWAYS);

                /* Insert Value Textfield */
                insertValueField = new TextField("Part Description");
                HBox.setHgrow(insertValueField, Priority.ALWAYS);

                /* Insert Button */
                insertButton = new Button("Insert");
                insertButton.setPadding(new Insets(0, 10, 0, 10));
                insertButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String partId = insertKeyField.getText();
                        String partDescription = insertValueField.getText();

                        statusHeader.setText("Insertion Completed");
                        statusBody.setText("Part Description: " + partDescription);

                        bPlusTree.insert(partId, partDescription);

                        /* Search */
                        searchField.setText(partId);

                        /* Insert */
                        insertKeyField.setText(partId);
                        insertValueField.setText(partDescription);

                        /* Update Value */
                        updateValueField.setText(partDescription);

                        /* Delete */
                        deleteField.setText(partId);
                    }
                });
                insertHbox.getChildren().add(insertLabel);
                insertHbox.getChildren().add(insertKeyField);
                insertHbox.getChildren().add(insertValueField);
                insertHbox.getChildren().add(insertButton);
                mainBodyBox.getChildren().add(insertHbox);

                /* Update Box
                 * Modify the description of a part*/
                HBox updateHbox = new HBox(20);
                updateHbox.setPadding(new Insets(0, 20, 0, 20));

                /* Update Label */
                Label updateLabel = new Label("Update: ");
                updateLabel.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 38));
                updateLabel.getStyleClass().add("descriptionHeader");
                updateLabel.setPadding(new Insets(0, 10, 0, 10));
                updateLabel.setAlignment(Pos.CENTER);

                /* Update Textfield */
                updateValueField = new TextField("Part Description");
                HBox.setHgrow(updateValueField, Priority.ALWAYS);

                /* Update Button */
                updateButton = new Button("Update");
                updateButton.setPadding(new Insets(0, 10, 0, 10));
                updateButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String searchKey = searchField.getText();
                        String updateValue = updateValueField.getText();

                        if (bPlusTree.search(searchKey) != null) {
                            /* Update Status */
                            bPlusTree.delete(searchKey); // Delete

                            bPlusTree.insert(searchKey, updateValue); // Update Value

                            statusHeader.setText("Updated");
                            statusBody.setText("Part Description: " + updateValue);
                        } else {
                            /* Update Status */
                            statusHeader.setText("Update Failed");
                            statusBody.setText("Part ID: Not Found -> " + updateValue);
                        }

                        /* Remove the Next Part Descriptions */
                        mainBodyBox.getChildren().remove(nextPartBox);

                        /* Delete */
                        deleteField.setText(searchKey);

                        /* Insert */
                        insertKeyField.setText(searchKey);
                        insertValueField.setText(updateValue);

                        /* Update Value */
                        updateValueField.setText(updateValue);
                    }
                });
                updateHbox.getChildren().add(updateLabel);
                updateHbox.getChildren().add(updateValueField);
                updateHbox.getChildren().add(updateButton);
                mainBodyBox.getChildren().add(updateHbox);

                /* add new parts, and delete parts. */
                /* Delete Box */
                HBox deleteHbox = new HBox(20);
                deleteHbox.setPadding(new Insets(0, 20, 0, 20));

                /* Delete Label */
                Label deleteLabel = new Label("Delete: ");
                deleteLabel.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 38));
                deleteLabel.getStyleClass().add("descriptionHeader");
                deleteLabel.setPadding(new Insets(0, 10, 0, 10));
                deleteLabel.setAlignment(Pos.CENTER);

                /* Delete Textfield */
                deleteField = new TextField();
                HBox.setHgrow(deleteField, Priority.ALWAYS);

                /* Delete Button */
                deleteButton = new Button("Delete");
                deleteButton.setPadding(new Insets(0, 10, 0, 10));
                deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        String deleteKey = deleteField.getText();

                        if (bPlusTree.search(deleteKey) != null) {
                            /* Update Status */
                            bPlusTree.delete(deleteKey);

                            statusHeader.setText("Deleted");
                            statusBody.setText("Part ID: " + deleteKey);
                        } else {
                            /* Update Status */
                            statusHeader.setText("Deletion Failed");
                            statusBody.setText(deleteKey + " : Not Found -> ");
                        }

                        /* Remove the Next Part Descriptions */
                        mainBodyBox.getChildren().remove(nextPartBox);

                        /* Delete */
                        deleteField.setText("Part ID");

                        /* Insert */
                        insertKeyField.setText("Part ID");
                        insertValueField.setText("Part Description");

                        /* Update Value */
                        updateValueField.setText("Part Description");
                    }
                });
                deleteHbox.getChildren().add(deleteLabel);
                deleteHbox.getChildren().add(deleteField);
                deleteHbox.getChildren().add(deleteButton);
                mainBodyBox.getChildren().add(deleteHbox);

                /* Status Update */
                statusHeader = new Label("Status");
                VBox.setVgrow(statusHeader, Priority.ALWAYS);
                statusHeader.getStyleClass().add("statusHeader");
                mainBodyBox.getChildren().add(statusHeader);
                statusBody = new Label("-------  Attach a File  -------");
                statusBody.setPadding(new Insets(0, 0, 15, 0));
                statusBody.setAlignment(Pos.CENTER);
                statusBody.setTextAlignment(TextAlignment.CENTER);
                statusBody.getStyleClass().add("statusBody");
                VBox.setVgrow(statusBody, Priority.ALWAYS);
                mainBodyBox.getChildren().add(statusBody);

                borderPane.setCenter(mainBodyBox);
            }
        });

        /* Description Box */
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                descriptionBox = new HBox(20);
                descriptionBox.getStyleClass().add("description");
                descriptionBox.setAlignment(Pos.CENTER);

                /* The Total Number Of Splits */
                totalNumberOfSplitsVBox = displayBox("The Total Number Of Splits", "--");
                /* The Parent Splits */
                parentSplitsVBox = displayBox("The Parent Splits", "--");
                /* The Fusions */
                fusionsVBox = displayBox("The Fusions", "--");
                /* The Parent Fusions */
                parentFusionsVBox = displayBox("The Parent Fusions", "--");
                /* The Tree Depth */
                theTreeDepthVBox = displayBox("The Tree Depth", "--");

                descriptionBox.getChildren().add(totalNumberOfSplitsVBox);
                descriptionBox.getChildren().add(parentSplitsVBox);
                descriptionBox.getChildren().add(fusionsVBox);
                descriptionBox.getChildren().add(parentFusionsVBox);
                descriptionBox.getChildren().add(theTreeDepthVBox);
                borderPane.setBottom(descriptionBox);
            }
        });

        /*
        Save the Data to a .txt File
        Upon ending, the user should be asked if changes should be saved to the flat-file.
        Upon clicking on Close - Prompt Dialog to ask the user if he/she wants to save the file.
        */
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                closeProgram(bPlusTree, primaryStage);
            }
        });

        primaryStage.setTitle("Parts Catalog - Memory Management");
        Scene scene = new Scene(borderPane, 900, 750, Color.rgb(44, 62, 80));
        scene.getStylesheets().add("source/style.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
        Save the Data to a .txt File
        Upon ending, the user should be asked if changes should be saved to the flat-file.
        Upon clicking on Close - Prompt Dialog to ask the user if he/she wants to save the file.
        */
    private void closeProgram(final BPlusTree<String, String> bPlusTree, final Stage primaryStage) {
        final Stage dialogWindow = new Stage();

        dialogWindow.initModality(Modality.APPLICATION_MODAL);
        dialogWindow.setTitle("Save File");
        dialogWindow.setMinWidth(350);

        Label label = new Label();
        label.setPadding(new Insets(0, 25, 0, 25));
        label.getStyleClass().add("descriptionHeader");
        label.setText("Do you want to Save or Quit the Parts Catalog?");

        HBox saveCancelLayout = new HBox(10);
        saveCancelLayout.setAlignment(Pos.CENTER);
        saveCancelLayout.setPadding(new Insets(15, 15, 25, 15));

        /* Save Button for Alert Dialog */
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                /* Save the File */
                finalFile = new File(getFile.getAbsolutePath());
                getFile.delete();

                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(finalFile, true));

                    Map<String, String> data = bPlusTree.getData();
                    Iterator it = data.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        bufferedWriter.write(pair.getKey().toString()
                                + "                             ".substring(8, 16)
                                + pair.getValue().toString());
                        bufferedWriter.newLine();
                        it.remove(); // avoids a ConcurrentModificationException
                    }

                    bufferedWriter.flush();
                    bufferedWriter.close();

                    /* Close the Dialog */
                    dialogWindow.close();

                    /* Update the Status */
                    statusHeader.setText("SAVED");
                    statusBody.setText("------- ------- -------");

                    Thread.sleep(2000);

                    /* Quit the Main Program */
                    primaryStage.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });

        /* Cancel Button */
        Button cancel = new Button("Quit");
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                dialogWindow.close();

                /* Update the Status */
                statusHeader.setText("Closing");
                statusBody.setText("------- ------- -------");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                primaryStage.close();
            }
        });
        saveCancelLayout.getChildren().addAll(saveBtn, cancel);

        VBox layout = new VBox(10);
        layout.getStyleClass().add("body");
        layout.setPadding(new Insets(25, 0, 0, 0));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, saveCancelLayout);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("source/style.css");
        dialogWindow.setScene(scene);
        dialogWindow.showAndWait();
    }

    /* Status Header and Body Description update during Search */
    private void search(String searchKey, String partDescription) {
        statusHeader.setText("Search Completed");
        statusBody.setText("Part Description: " + partDescription);

        /* Search */
        searchField.setText(searchKey);

        /* Insert */
        insertKeyField.setText(searchKey);
        insertValueField.setText(partDescription);

        /* Update Value */
        updateValueField.setText(partDescription);

        /* Delete */
        deleteField.setText(searchKey);
    }

    /*
    Display the total number of splits
    Display the parent splits
    Display the fusions
    Display the parent fusions
    Display the Tree Depth  - Tree grows in depth only when root node is split.
    */
    private VBox displayBox(String headerText, String bodyText) {
        VBox vBox = new VBox(10);
        vBox.prefHeight(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(0, 10, 0, 10));
        Label header = new Label(headerText);
        header.setPadding(new Insets(15, 0, 0, 0));
        header.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 38));
        header.getStyleClass().add("descriptionHeader");
        vBox.getChildren().add(header);
        Label body = new Label(bodyText);
        body.setFont(Font.font("Helvetica", FontWeight.EXTRA_BOLD, 38));
        body.setPadding(new Insets(0, 0, 15, 0));
        body.setAlignment(Pos.CENTER);
        body.setTextAlignment(TextAlignment.CENTER);
        body.getStyleClass().add("descriptionBody");
        vBox.getChildren().add(body);
        return vBox;
    }
}