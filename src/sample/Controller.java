package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.logic.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    @FXML
    private Pane vector_canvas;
    @FXML
    private Pane raster_canvas;
    @FXML
    private Label coords;
    @FXML
    private TextArea complex_transform;
    @FXML
    private TextField rotate_angle;
    @FXML
    private TextField scale_sx;
    @FXML
    private TextField scale_sy;
    @FXML
    private TextField translate_x;
    @FXML
    private TextField translate_y;
    @FXML
    private ComboBox combobox;

    private VectorGraphic g;
    private Image r;
    private VectorNode selected;
    private Shape tempNet;
    private double dragStartX;
    private double dragStartY;
    private double currentX;
    private double currentY;
    private boolean move;
    private boolean rotate;
    private boolean scale;
    private int mode;
    private ImageView iv;

    public void Controller(){
        g = null;
        selected = null;
        tempNet = null;
        dragStartX = 0;
        dragStartY = 0;
        move = false;
        rotate = false;
        scale = false;
        mode = 0;
        iv = null;
    }

    public void drawVectorGraphic(VectorGraphic g, Pane c){
        ArrayList<VectorNode> nodes = g.getNodes();

        c.getChildren().setAll();
        for(VectorNode node : nodes){
            Shape shape = new Polygon(node.getPoints());
            node.setShape(shape);
            shape.setStroke(node.getColor());
            shape.setFill(node.getColor());

            shape.setOnMousePressed(new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                    select(node);
                    dragStartX = currentX;
                    dragStartY = currentY;

                    if (event.isControlDown()) {
                        rotate = true;
                        Image image = new Image("sample/res/rotate.png");
                        changeCursor(new ImageCursor(image, image.getWidth()/2, image.getHeight()/2));
                    }
                    else if(event.isShiftDown()){
                        scale = true;
                        changeCursor(Cursor.CROSSHAIR);
                    }
                    else{
                        move = true;
                        changeCursor(Cursor.MOVE);
                    }
                }
            });

            shape.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    rotate = false;
                    move = false;
                    scale = false;
                    changeCursor(Cursor.DEFAULT);
                }
            });
            shape.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (move) {
                        dragStartX = currentX;
                        dragStartY = currentY;
                    }
                }
            });
            shape.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    double canvX = event.getSceneX();
                    double canvY = event.getSceneY()-54;
                    currentX = canvX;
                    currentY = canvY;
                    coords.setText("["+canvX+","+canvY+"]");

                    if(rotate){
                        double centerX = node.getCenterX();
                        double centerY = node.getCenterY();
                        double angle = Math.toDegrees(Math.atan2(currentY - dragStartY, currentX - dragStartX)/(2*Math.PI));
                        node.rotateRelativeToPoint(angle/10, centerX, centerY);
                        recalculateNet(node);
                        refreshVectorCanvas();
                        dragStartX = currentX;
                        dragStartY = currentY;
                    }
                    else if(scale){
                        double scaleX = (currentX-dragStartX)*0.02+1;
                        double scaleY = (dragStartY-currentY)*0.02+1;
                        if(scaleX<0)scaleX = 0;
                        if(scaleY<0)scaleY = 0;
                        node.scaleRelativeToPoint(scaleX, scaleY, node.getCenterX(), node.getCenterY());
                        recalculateNet(node);
                        dragStartX = currentX;
                        dragStartY = currentY;
                        refreshVectorCanvas();
                    }
                    else if(move){
                        double x = currentX-dragStartX;
                        double y = currentY-dragStartY;
                        System.out.println("x: "+currentX+" - "+dragStartX+" = "+x);
                        System.out.println("y: "+currentY+" - "+dragStartY+" = "+y);
                        node.translate(x,y);
                        recalculateNet(node);
                        dragStartX = currentX;
                        dragStartY = currentY;
                        refreshVectorCanvas();
                    }
                }
            });
            c.getChildren().add(shape);
    }
        if(tempNet!=null)c.getChildren().add(tempNet);
        if(selected!=null){
            c.getChildren().add(c.getChildren().remove(c.getChildren().lastIndexOf(selected.getShape())));
        }
    }

    public void refreshVectorCanvas(){
        vector_canvas.setPrefWidth(g.getWidth());
        vector_canvas.setPrefHeight(g.getHeight());
        addCanvasHandlers(vector_canvas);
        drawVectorGraphic(g, vector_canvas);
    }

    public void refreshRasterCanvas(){
        raster_canvas.setPrefWidth(2000);
        raster_canvas.setPrefHeight(2000);
        addCanvasHandlers(raster_canvas);
        raster_canvas.getChildren().setAll(new ImageView(r));

        combobox.getItems().setAll("Interpolacja dwuliniowa", "Najbliższy sąsiad");
    }
    public void openVectorGraphicFromFile(File f){
        try {
            g = GraphicParser.readVectorGraphic(f);
            //addLine();
            mode = 1;
            refreshVectorCanvas();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openVectorGraphic(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(vector_canvas.getScene().getWindow());
        openVectorGraphicFromFile(file);
    }

    public void openRasterGraphicFromFile(File f){
        r = new Image(f.toURI().toString());
        iv = new ImageView(r);
        mode = 2;
        refreshRasterCanvas();
    }

    public void openRasterGraphic(ActionEvent actionEvent){
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(raster_canvas.getScene().getWindow());
        openRasterGraphicFromFile(file);
    }

    private void select(VectorNode node){
        selected = node;
        makeTempNet(node);
        refreshVectorCanvas();
    }

    private void recalculateNet(VectorNode node){
        double startX = 0;
        double startY = 0;
        double width = 0;
        double height = 0;

        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;

        double[] points = node.getPoints();
        for(int i=0;i<points.length;i+=2){
            Point2D point = node.getShape().localToParent(points[i], points[i+1]);
            if(point.getX()<minX)minX = point.getX();
            if(point.getX()>maxX)maxX = point.getX();
            if(point.getY()<minY)minY = point.getY();
            if(point.getY()>maxY)maxY = point.getY();
        }
        startX = minX;
        startY = minY;
        width = Math.abs(maxX-minX);
        height = Math.abs(maxY-minY);


        ((Rectangle)tempNet).setX(startX-1);
        ((Rectangle)tempNet).setY(startY-1);
        ((Rectangle)tempNet).setWidth(width+2);
        ((Rectangle)tempNet).setHeight(height+2);
    }
    private void makeTempNet(VectorNode node){
        tempNet = new Rectangle();
        recalculateNet(node);
        tempNet.setMouseTransparent(true);
        tempNet.setStrokeWidth(1);
        tempNet.setFill(Color.TRANSPARENT);
        tempNet.setStroke(Color.BLACK);
        tempNet.getStrokeDashArray().addAll(3.0, 3.0, 3.0, 3.0);
    }

    private void addCanvasHandlers(Pane canvas){
        canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getSceneX();
                double y = event.getSceneY()-54;
                currentX = x;
                currentY = y;
                coords.setText("["+x+","+y+"]");
            }
        });

        canvas.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                coords.setText("[x,y]");
            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getSceneX();
                double y = event.getSceneY()-54;
                currentX = x;
                currentY = y;
                coords.setText("["+x+","+y+"]");
            }
        });
    }

    public void complexTransform(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        String text = complex_transform.getText();
        String[] temp_arr = text.split(";");

        for(String s : temp_arr){
            String first = s.substring(0,1);

            if(first.equals("S")){
                int parseStart = s.indexOf('(')+1;
                int parseEnd = s.indexOf(')');
                String toParse = s.substring(parseStart, parseEnd);
                String[] temp_arr2 = toParse.split(",");
                double x = Double.parseDouble(temp_arr2[0]);
                double y = Double.parseDouble(temp_arr2[1]);
                if(mode==1)scaleAll(x, y);
                else if(mode==2)applyScale(x,y);
            }
            else if(first.equals("R")){
                int parseStart = s.indexOf('(')+1;
                int parseEnd = s.indexOf(')');
                String toParse = s.substring(parseStart, parseEnd);
                double angle = Double.parseDouble(toParse);
                if(mode==1)rotateAll(angle);
                else if(mode==2)applyRotation(angle,true);
            }
            else if(first.equals("T")){
                int parseStart = s.indexOf('(')+1;
                int parseEnd = s.indexOf(')');
                String toParse = s.substring(parseStart, parseEnd);
                String[] temp_arr2 = toParse.split(",");
                double x = Double.parseDouble(temp_arr2[0]);
                double y = Double.parseDouble(temp_arr2[1]);
                if(mode==1)translateAll(x, y);
                else if(mode==2)applyTranslation(x,y);
            }
            complex_transform.setText("");
        }
    }

    private void changeCursor(Cursor type){
        vector_canvas.getScene().setCursor(type);
    }

    private void scaleAll(double x, double y){
        ArrayList<VectorNode> nodes = g.getNodes();

        for(VectorNode node : nodes)
            node.scale(x, y);

        if(selected!=null)recalculateNet(selected);
        refreshVectorCanvas();
    }

    private void rotateAll(double angle){
        ArrayList<VectorNode> nodes = g.getNodes();

        for(VectorNode node : nodes)
            node.rotate(angle);

        if(selected!=null)recalculateNet(selected);
        refreshVectorCanvas();
    }

    private void translateAll(double x, double y){
        ArrayList<VectorNode> nodes = g.getNodes();

        for(VectorNode node : nodes)
            node.translate(x, y);

        if(selected!=null)recalculateNet(selected);
        refreshVectorCanvas();
    }

    private void openDialogBox(String fileName, String title){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName));
        loader.setController(this);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setTitle(title);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void openScaleBox(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        openDialogBox("scale_box.fxml", "Skalowanie");
    }

    public void openRotateBox(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        openDialogBox("rotate_box.fxml", "Obrót");
    }

    public void openTranslateBox(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        openDialogBox("translate_box.fxml", "Translacja");
    }

    public void changeModeToVector(){
        mode = 1;
    }

    public void changeModeToRaster(){
        mode = 2;
    }

    public void doScaling(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        double sx = Double.parseDouble(scale_sx.getText());
        double sy = Double.parseDouble(scale_sy.getText());
        if(mode==2)applyScale(sx,sy);
        else if(mode==1)scaleAll(sx, sy);
        ((Stage)scale_sx.getScene().getWindow()).close();
    }

    public void doRotating(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        double angle = Double.parseDouble(rotate_angle.getText());
        if(mode==2)applyRotation(angle,true);
        else if(mode==1)rotateAll(angle);
        ((Stage) rotate_angle.getScene().getWindow()).close();

    }

    public void doTranslating(){
        if(!((mode==1 && g!=null)||(mode==2 && r!=null)))return;
        double x = Double.parseDouble(translate_x.getText());
        double y = Double.parseDouble(translate_y.getText());
        if(mode==2)applyTranslation(x,y);
        else if(mode==1)translateAll(x, y);
        ((Stage)translate_x.getScene().getWindow()).close();
    }

    private void applyRotation(double angle){
        applyRotation(angle, false);
    }
    private void applyRotation(double angle, boolean relativeToCenter){
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();
        double radianAngle = Math.toRadians(angle);
        double width = Math.sqrt(Math.cos(Math.abs(radianAngle))*oldWidth*oldWidth+Math.sin(Math.abs(radianAngle))*oldHeight*oldHeight);
        double height = Math.sqrt(Math.sin(Math.abs(radianAngle))*oldWidth*oldWidth+Math.cos(Math.abs(radianAngle))*oldHeight*oldHeight);

        Transform rotation = Transform.getRotation(radianAngle);

        if(relativeToCenter){
            rotation = Transform.getTranslate(-oldWidth/2, -oldHeight/2);
            rotation.addRotation(radianAngle);
            rotation.addTranslation(oldWidth/2, oldHeight/2);
        }

        WritableImage newR = new WritableImage((int) width+1, (int) height+1);
        PixelWriter writer = newR.getPixelWriter();
        PixelReader reader = r.getPixelReader();
        int alg = combobox.getSelectionModel().getSelectedIndex();

        //System.out.println(oldWidth+" - "+oldHeight);
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                double[] point = rotation.retransformPoint(i,j);

                if(alg==1) {
                    writer.setColor(i, j, getNearestNeighbour(point[0], point[1], reader, r));
                }
                else
                    writer.setColor(i, j, getLineInterpolation(point[0], point[1], reader, r));
            }
        }
        r = newR;
        refreshRasterCanvas();
    }

    private void applyTranslation(double x, double y){
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();
        double width = oldWidth+x;
        double height = oldHeight+y;
        Transform translation = Transform.getTranslate(x, y);
        WritableImage newR = new WritableImage((int) width, (int) height);
        PixelWriter writer = newR.getPixelWriter();
        PixelReader reader = r.getPixelReader();

        int alg = combobox.getSelectionModel().getSelectedIndex();
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                double[] point = translation.retransformPoint(i,j);

                if(alg==1) {
                    writer.setColor(i, j, getNearestNeighbour(point[0], point[1], reader, r));
                }
                else
                    writer.setColor(i, j, getLineInterpolation(point[0], point[1], reader, r));
            }
        }
        r = newR;
        refreshRasterCanvas();
    }

    private void applyScale(double sx, double sy){
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();
        double width = oldWidth*sx;
        double height = oldHeight*sy;
        Transform scale = Transform.getScale(sx, sy);

        WritableImage newR = new WritableImage((int)width+1, (int) height+1);
        PixelWriter writer = newR.getPixelWriter();
        PixelReader reader = r.getPixelReader();
        int alg = combobox.getSelectionModel().getSelectedIndex();

        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                double[] point = scale.retransformPoint(i,j);
                if(alg==1) {
                    writer.setColor(i, j, getNearestNeighbour(point[0], point[1], reader, r));
                }
                else
                    writer.setColor(i, j, getLineInterpolation(point[0], point[1], reader, r));
            }
        }
        r = newR;
        refreshRasterCanvas();
    }

    private Color getLineInterpolation(double x, double y, PixelReader reader, Image img){
        double width = img.getWidth();
        double height = img.getHeight();
        if(x<0 || y<0 || x>=width || y>=height)return Color.WHITE;
        int[] a = {(int) Math.floor(x), (int) Math.floor(y)};
        int[] b = {(a[0] + 1)>=width?a[0]:(a[0] + 1), a[1]};
        int[] c = {a[0], (a[1] + 1)>=height?a[1]:(a[1] + 1)};
        int[] d = {(a[0] + 1)>=width?a[0]:(a[0] + 1), (a[1] + 1)>=height?a[1]:(a[1] + 1)};
        double alfa = x - a[0];
        double beta = y - a[1];
        double red = alfa * (beta * reader.getColor(b[0], b[1]).getRed() + (1 - beta) * reader.getColor(d[0], d[1]).getRed()) +
                (1 - alfa) * (beta * reader.getColor(c[0], c[1]).getRed() + (1 - beta) * reader.getColor(a[0], a[1]).getRed());
        double green = alfa * (beta * reader.getColor(b[0], b[1]).getGreen() + (1 - beta) * reader.getColor(d[0], d[1]).getGreen()) +
                (1 - alfa) * (beta * reader.getColor(c[0], c[1]).getGreen() + (1 - beta) * reader.getColor(a[0], a[1]).getGreen());
        double blue = alfa * (beta * reader.getColor(b[0], b[1]).getBlue() + (1 - beta) * reader.getColor(d[0], d[1]).getBlue()) +
                (1 - alfa) * (beta * reader.getColor(c[0], c[1]).getBlue() + (1 - beta) * reader.getColor(a[0], a[1]).getBlue());

        return new Color(red, green, blue, 1.0);
    }

    private Color getNearestNeighbour(double x, double y, PixelReader reader, Image img){
        double width = img.getWidth();
        double height = img.getHeight();
        if(x<0 || y<0 || x>=width || y>=height)return Color.WHITE;
        int[] a = {(int) Math.floor(x), (int) Math.floor(y)};
        int[] b = {(a[0] + 1)>=width?a[0]:(a[0] + 1), a[1]};
        int[] c = {a[0], (a[1] + 1)>=height?a[1]:(a[1] + 1)};
        int[] d = {(a[0] + 1)>=width?a[0]:(a[0] + 1), (a[1] + 1)>=height?a[1]:(a[1] + 1)};
        double alfa = x - a[0];
        double beta = y - a[1];
        Color color = null;
        if(alfa<0.5)color = reader.getColor(a[0], a[1]);
        else color = reader.getColor(d[0], d[1]);

        return color;
    }

    public void doMirrorImage(){
        double x1 = 280;
        double y1 = 10;
        double x2 = 200;
        double y2 = 360;

        double a  = (y1-y2)/(x1-x2);
        double b = y1-a*x1;

        ArrayList<VectorNode> nodes = g.getNodes();

        for(VectorNode node : nodes) {
            node.translate(0,-b);
            node.rotate(Math.toDegrees(-Math.atan(a)));
            //System.out.println("obracam: "+Math.toDegrees(Math.atan(a)));
            node.scale(1,-1);
            node.rotate(Math.toDegrees(Math.atan(a)));
            node.translate(0,b);

            Shape shape = new Polygon(node.getPoints());
            node.setShape(shape);
            shape.setStroke(node.getColor());
            shape.setFill(node.getColor());
        }
        refreshVectorCanvas();
    }
}
