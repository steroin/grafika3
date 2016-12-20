package sample.logic;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Sergiusz on 10.11.2016.
 */
public class VectorGraphic {
    private ArrayList<VectorNode> nodes;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int width;
    private int height;

    public VectorGraphic(int width, int height){
        this.width = width;
        this.height = height;
        nodes = new ArrayList<>();
    }

    public void resize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void addNode(VectorNode node){
        nodes.add(node);
    }

    public ArrayList<VectorNode> getNodes(){
        return nodes;
    }
}
