package Simulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.*;

public class DFAsimulator extends Application {
    ArrayList<StateCircle>statesList;
    TreeSet<Character>alphabetSet;
    String simulateString;

    static Status currentMode=Status.DEFAULT;

    StateCircle firstCircle;
    Font seoge16;
    Button playSimButton,finishButton,formalDefButton;
    FlowPane flowPane,flowPane2;
    Group root;
    AnchorPane anchorPane;
    Scene scene;

    public void start(Stage stage){
        stage.setWidth(870); 
        stage.setHeight(500);
        mainMenu(stage);
    	stage.setTitle("DFA Simulator");
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.show();
    }
   
    private void playSimulation(Button nextStateButton) {
        nextStateButton.setText("Next State");
        final int[] i = {-1};
        final StateCircle[] curCircle = {statesList.get(0)};

        Label textLabel=new Label();
        FlowPane fp=new FlowPane();
        fp.setLayoutX(20);
        fp.setLayoutY(20);
        Label simLabels[]=new Label[simulateString.length()];
        textLabel.setVisible(false);
          for(int j=0;j<simulateString.length();j++) {
            simLabels[j]=new Label(String.valueOf(simulateString.charAt(j)));
            simLabels[j].setFont(new Font("Arial",28));
            fp.getChildren().add(simLabels[j]);
        }

        nextStateButton.setOnAction(event -> {
            if(!textLabel.isVisible()) 
            	textLabel.setVisible(true);
            if(i[0]+1<simulateString.length()) 
            	i[0]++;
            else {
                Alert a=new Alert(Alert.AlertType.INFORMATION,"",ButtonType.YES,ButtonType.NO);
                if(curCircle[0].isFinal) a.setContentText("The String is Acepted!!\n"+
                                        "Try another String?");
                else a.setContentText("The String is Rejected!!\n"+
                        "Try another String?");
                a.showAndWait();
                if(a.getResult()==ButtonType.YES) {                  
                    root.getChildren().removeAll(fp,textLabel);
                    getString();
                    playSimulation(nextStateButton);
                    return;
                }
                else {                  
                    root.getChildren().removeAll(fp,textLabel);
                    nextStateButton.setText("Play Simulation");                    
                    flowPane.setVisible(true);
                    currentMode=Status.DEFAULT;
                    finishButton.setDisable(false);
                    playSimButton.setDisable(true);
                    formalDefButton.setDisable(true);
                    return;
                }
            }

            if(i[0]>0)simLabels[i[0]-1].setTextFill(Color.BLACK);
            simLabels[i[0]].setTextFill(Color.RED);
            textLabel.setText(String.valueOf(simulateString.charAt(i[0])));
        
            Character c= curCircle[0].Table.get(simulateString.charAt(i[0]));            
            for(StateCircle sc:statesList){
                if(sc.getCircleName()==c) {
                    curCircle[0] =sc;
                }
            }
        });

        root.getChildren().addAll(textLabel,fp);
    }

    private boolean getString() {
        TextInputDialog t=new TextInputDialog();
        t.setTitle("Input String for the Transition");
        t.setHeaderText(null);

        Optional<String> res=t.showAndWait();
        if(res.isPresent()) {
            if(res.get().isEmpty()) 
            	return getString();
            else {
                boolean flag=true;
                String s=res.get();
                for(int i=0;i<s.length();i++) {
                    if(!alphabetSet.contains(s.charAt(i))) {
                        flag=false;
                        break;
                    }
                }
                if(flag) {
                    simulateString=s;
                    return true;
                }
                else {
                    Alert b=new Alert(Alert.AlertType.ERROR,"Invalid Alphabet in the String", ButtonType.CLOSE);
                    b.setHeaderText(null);
                    b.showAndWait();
                    return getString();
                }
            }
        }
        else {            
            currentMode=Status.FINISHED;
            return false;
        }
    }

    private boolean isFinished() {
        int SZ=alphabetSet.size();
        if(SZ==0) return false;
        for(StateCircle sc:statesList) {
            if(sc.Table.size()!=SZ) 
            	return false;
        }
        return true;
    }

    private void removeTransit(MouseEvent event) {
        boolean isDone=false;     
        for(StateCircle sc:statesList) {
            for(Curve cv:sc.PathMap.values()) {
                if(cv.getElements().isEmpty()) {
                        isDone=true;
                       for(Character cx:sc.PathMap.keySet()) {
                           if(sc.PathMap.get(cx)==cv) {
                               for(Character cc:sc.Table.keySet()) {
                                   if(sc.Table.get(cc)==cx) sc.Table.remove(cc);
                               }
                               sc.PathMap.remove(cx);
                           }
                       }
                }
            }
        }
        if(isDone) {
            currentMode=Status.DEFAULT;
        }
    }

    private void changeCursor() {
        Circle circle=new Circle(25,null);
        circle.setStroke(Color.BLACK);
        SnapshotParameters sp=new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        Image image=circle.snapshot(sp,null);
        scene.setCursor(new ImageCursor(image,512,512));
    }

    private void addNewState(MouseEvent event) {
        if(event.getSceneX()<40 || event.getSceneY()>scene.getWidth()-140
           || event.getSceneY()<140 || event.getSceneY()>scene.getHeight()-140){
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
        }

        StateCircle circle=new StateCircle(event.getSceneX()-20,event.getSceneY()-20,25);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.SILVER);

        if(statesList.isEmpty())
            circle.circleIndex=0;
        else
            circle.circleIndex=statesList.get(statesList.size()-1).circleIndex+1;

        circle.setCircleName((char)(circle.circleIndex+65));
        circle.showLabel();

        circle.setOnMouseDragged(event1 -> {
            if(currentMode!=Status.FINISHED) {
                circle.setCenterX(event1.getSceneX());
                circle.setCenterY(event1.getSceneY());
            }
        });

        circle.setOnMouseClicked(event1 -> {
            if(currentMode==Status.FIRST || currentMode==Status.SECOND) addNewTransit(circle);
            if(currentMode==Status.REMOVESTATE) removeState(circle);
            if(currentMode==Status.LOOP) createNewLoop(circle);
            if(currentMode==Status.MAKEFINAL) {
                circle.finalCircle=new StateCircle(circle.getCenterX(),circle.getCenterY(),20);
                circle.finalCircle.centerXProperty().bindBidirectional(circle.centerXProperty());
                circle.finalCircle.centerYProperty().bindBidirectional(circle.centerYProperty());
                circle.finalCircle.setStroke(Color.BLACK);
                circle.finalCircle.setFill(Color.GRAY);
                circle.isFinal=true;

                circle.finalCircle.setOnMouseDragged(event2 -> {
                    circle.finalCircle.setCenterX(event2.getSceneX());
                    circle.finalCircle.setCenterY(event2.getSceneY());
                });

                root.getChildren().addAll(circle.finalCircle);
                circle.nameLabel.toFront();

                currentMode=Status.DEFAULT;
            }
        });

        currentMode=Status.DEFAULT;
        scene.setCursor(Cursor.DEFAULT);
        statesList.add(circle);

        root.getChildren().addAll(circle,circle.nameLabel);
    }

    private void createNewLoop(StateCircle circle) {        
        if(circle.PathMap.containsKey(circle.getCircleName())) {
            currentMode=Status.DEFAULT;
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        String trasitString=askAlphabet();
        if(trasitString.isEmpty()) 
        	return;
        String[] sb=trasitString.split(",");
        boolean flag=false;
        for(String s:sb) {
            if(circle.Table.containsKey(s.charAt(0))) {
                flag=true;
            }
        }

        if(flag) {
            Alert b=new Alert(Alert.AlertType.ERROR,"One or More of the Transitions are already defined!"+
                    "\nRemove them First!", ButtonType.CLOSE);
            b.setHeaderText(null);
            b.showAndWait();
            currentMode=Status.DEFAULT;
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        else {
            for(String s:sb) {
                circle.Table.put(s.charAt(0), circle.getCircleName());
                alphabetSet.add(s.charAt(0));
            }
        }

        Curve p;

        p=new Curve(circle, trasitString);

        circle.PathMap.put(circle.getCircleName(), p);
        root.getChildren().addAll(p,p.nameLabel);
        p.toBack();

        scene.setCursor(Cursor.DEFAULT);
        currentMode=Status.DEFAULT;
}
    private void removeState(StateCircle circle) {
        currentMode=Status.DEFAULT;
        scene.setCursor(Cursor.DEFAULT);

        root.getChildren().removeAll(circle,circle.nameLabel);
        statesList.remove(circle);       
        for(Character c:circle.PathMap.keySet())
            root.getChildren().removeAll(circle.PathMap.get(c),circle.PathMap.get(c).nameLabel);
        for(StateCircle sc:statesList) {
            if(sc.PathMap.containsKey(circle.getCircleName())) {
                root.getChildren().removeAll(sc.PathMap.get(circle.getCircleName()),sc.PathMap.get(circle.getCircleName()).nameLabel);
                sc.PathMap.remove(circle.getCircleName());
            }
            for(Character c:sc.Table.values()) {
                if(c.equals(circle.getCircleName()))
                    sc.Table.remove(c,sc.Table.get(c));
            }
        }
        if(circle.isFinal) root.getChildren().removeAll(circle.finalCircle);
    }
   
    private void addNewTransit(StateCircle circle) {
        if(currentMode==Status.FIRST) {
            firstCircle=circle;
            currentMode=Status.SECOND;
            scene.setCursor(Cursor.CROSSHAIR);
        }

        else if(currentMode==Status.SECOND) {
            if(firstCircle==circle) return;
            if(firstCircle.PathMap.containsKey(circle.getCircleName())) {
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
            }

            String trasitString=askAlphabet();
            if(trasitString.isEmpty()) return;          
            String[] sb=trasitString.split(",");
            boolean flag=false;
            for(String s:sb) {
                if(firstCircle.Table.containsKey(s.charAt(0))) {
                    flag=true;
                }
            }
            if(flag) {
                Alert a=new Alert(Alert.AlertType.ERROR,"One or More of the Transitions are already defined!"+
                        "\nRemove them First!", ButtonType.CLOSE);
                a.setHeaderText(null);
                a.showAndWait();
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
            }
            else {
                for(String s:sb) {
                    firstCircle.Table.put(s.charAt(0), circle.getCircleName());
                    alphabetSet.add(s.charAt(0));
                }
            }

            Curve p;
            p=new Curve(firstCircle, circle, trasitString);               

            firstCircle.PathMap.put(circle.getCircleName(), p);
            root.getChildren().addAll(p,p.nameLabel);
            p.toBack();

            scene.setCursor(Cursor.DEFAULT);
            currentMode=Status.DEFAULT;
        }
    }  
    private String askAlphabet() {
        TextInputDialog t=new TextInputDialog();
        t.setTitle("Alphabet for the Transition");
        t.setHeaderText(null);

        Optional<String> res=t.showAndWait();
        if(res.isPresent()) {
            if(res.get().isEmpty()) 
            	return askAlphabet();
            else 
            	return res.get();
        }
        else 
        	return "";
    }

    private void mainMenu(Stage stage){
        BorderPane root=new BorderPane();
        root.setPadding(new Insets(100,20,20,20));
        VBox vBox=new VBox();
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setSpacing(50);

        Font seoge20=new Font("seoge",20);

        Label titleLabel=new Label("DFA Simulator");
        Button dfaButton=new Button("Create A DFA Machine");
        Button exitButton=new Button("Exit");

        dfaButton.setMinWidth(630);
        exitButton.setMinWidth(630);


        dfaButton.setFont(seoge20);
        titleLabel.setFont(new Font("Arial",35));
        exitButton.setFont(seoge20);

        dfaButton.setTooltip(new Tooltip("Create and Simulate the DFA"));
        exitButton.setTooltip(new Tooltip("Thank You"));
        
        dfaButton.setOnAction(event -> init(stage));
        exitButton.setOnAction(event -> Platform.exit());

        vBox.getChildren().addAll(dfaButton,exitButton);
        BorderPane.setAlignment(vBox,Pos.CENTER);
        BorderPane.setAlignment(titleLabel,Pos.CENTER);

        root.setTop(titleLabel);
        root.setCenter(vBox);

        Scene menuScene=new Scene(root,800,800, Color.BEIGE);

        stage.setScene(menuScene);
    }

    private void init(Stage stage) {
        MenuBar menuBar=new MenuBar();
        root=new Group();
        flowPane=new FlowPane();
        flowPane2=new FlowPane();
       
        anchorPane=new AnchorPane(flowPane,flowPane2,root,menuBar);
        scene=new Scene(anchorPane,stage.getWidth(),stage.getHeight());
        


        seoge16=new Font("Arial",14);

        flowPane.setMinSize(stage.getWidth()-50,50);
        flowPane2.setMinSize(stage.getWidth()-50,50);
        flowPane.setAlignment(Pos.BASELINE_LEFT);
        flowPane2.setAlignment(Pos.CENTER);
        flowPane.setHgap(800);  
        flowPane.setVgap(40);
        flowPane2.setHgap(5); 
        flowPane2.setVgap(5);
        flowPane.setPadding(new Insets(5,10,5,10));
        flowPane2.setPadding(new Insets(5,10,5,10));
     
        statesList=new ArrayList<>();
        alphabetSet=new TreeSet<>(); 

        menuBar.prefWidthProperty().bind(scene.widthProperty());

        Button addStateButton=new Button("Add a State");
        Button addTransitButton=new Button("Add a Transition");
        Button addLoopButton=new Button("Add a self Transition");
        Button removeStateButton=new Button("Remove a State");
        Button removeTransitButton=new Button("Remove a Transition");
        Button makeFinalButton=new Button("Make Final State");

        finishButton=new Button("Finish");
        playSimButton=new Button("Play Simulation");
        playSimButton.setDisable(true);

        addStateButton.setFont(seoge16);
        addTransitButton.setFont(seoge16);
        addLoopButton.setFont(seoge16);
        removeStateButton.setFont(seoge16);
        removeTransitButton.setFont(seoge16);
        makeFinalButton.setFont(seoge16);      
        finishButton.setFont(seoge16);
        playSimButton.setFont(seoge16);

        scene.setOnMouseClicked(event -> {
            if(event.getButton()== MouseButton.SECONDARY&& currentMode!=Status.FINISHED) {
                scene.setCursor(Cursor.DEFAULT);
                currentMode=Status.DEFAULT;
            }

            if(currentMode==Status.ADDSTATE) addNewState(event);
            if(currentMode==Status.REMOVETRANSIT) {
                removeTransit(event);
            }
        });
        addStateButton.setOnAction(event -> {
            currentMode=Status.ADDSTATE;
            changeCursor();
        });
        addTransitButton.setOnAction(event -> {
            currentMode=Status.FIRST;
            scene.setCursor(Cursor.DEFAULT);
        });
        addLoopButton.setOnAction(event -> {
            currentMode=Status.LOOP;
            scene.setCursor(Cursor.DEFAULT);
        });
        removeStateButton.setOnAction(event -> {
            scene.setCursor(Cursor.CLOSED_HAND);
            currentMode=Status.REMOVESTATE;
        });
        removeTransitButton.setOnAction(event -> {
            scene.setCursor(Cursor.DEFAULT);
            currentMode=Status.REMOVETRANSIT;
        });
        makeFinalButton.setOnAction(event -> {
            currentMode=Status.MAKEFINAL;
            scene.setCursor(Cursor.DEFAULT);
        });
        finishButton.setOnAction(event -> {
            if(isFinished()){
                flowPane.setVisible(false);              
                playSimButton.setDisable(false);
                finishButton.setDisable(true);
                currentMode=Status.FINISHED;
            }
            else {
                Alert a=new Alert(Alert.AlertType.ERROR,"Transition Missing!!!",ButtonType.CLOSE);
                a.showAndWait();               
            }
        });
        playSimButton.setOnAction(event -> {
            if(currentMode==Status.FINISHED)
                if(getString())
                    playSimulation(playSimButton);
        });

        flowPane.getChildren().addAll(addStateButton,addTransitButton,addLoopButton,
                removeStateButton,removeTransitButton,makeFinalButton);
        flowPane2.getChildren().addAll(finishButton,playSimButton);

        AnchorPane.setTopAnchor(menuBar,0.0);
        AnchorPane.setTopAnchor(flowPane,30.0);
        AnchorPane.setLeftAnchor(flowPane,20.0);
        AnchorPane.setBottomAnchor(flowPane2,0.0);
        AnchorPane.setLeftAnchor(flowPane2,20.0);

        stage.setScene(scene);
    }

    public static void main(String[] args) {
		launch(args);}
}

