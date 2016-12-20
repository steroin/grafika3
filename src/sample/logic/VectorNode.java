package sample.logic;


import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Created by Sergiusz on 10.11.2016.
 */
public class VectorNode {

    private Color color;
    private double[] points;
    private Shape shape;

    public VectorNode(Color color, double[] points){
        this.color = color;
        this.points = points;
        shape = new Polygon(points);
        shape.setStroke(color);
        shape.setFill(color);
    }

    public Color getColor(){
        return color;
    }

    public Shape getShape(){
        return shape;
    }

    public void setShape(Shape s){
        shape = s;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public double[] getPoints(){
        return points;
    }

    public void rotate(double angle){
        angle = Math.toRadians(angle);
        Transform t = Transform.getRotation(angle);

        for(int i=0;i<points.length;i+=2){
            double[] transformed = t.transformPoint((int)points[i], (int)points[i+1]);
            points[i] = transformed[0];
            points[i+1] = transformed[1];
        }
    }

    public void rotateRelativeToPoint(double angle, double x, double y){
        angle = Math.toRadians(angle);
        Transform t = Transform.getTranslate(-x, -y);
        t.addRotation(angle);
        t.addTranslation(x,y);
        for(int i=0;i<points.length;i+=2){
            double[] transformed = t.transformPoint(points[i], points[i+1]);
            points[i] = transformed[0];
            points[i+1] = transformed[1];
        }
    }

    public void scale(double sx, double sy){
        Transform t = Transform.getScale(sx, sy);

        for(int i=0;i<points.length;i+=2){
            double[] transformed = t.transformPoint(points[i], points[i+1]);
            points[i] = transformed[0];
            points[i+1] = transformed[1];
        }
    }

    public void scaleRelativeToPoint(double sx, double sy, double x, double y){
        Transform t = Transform.getTranslate(-x, -y);
        t.addScale(sx, sy);
        t.addTranslation(x,y);
        for(int i=0;i<points.length;i+=2){
            double[] transformed = t.transformPoint(points[i], points[i+1]);
            points[i] = transformed[0];
            points[i+1] = transformed[1];
        }
    }

    public void translate(double x, double y){
        Transform t = Transform.getTranslate(x, y);

        for(int i=0;i<points.length;i+=2){
            double[] transformed = t.transformPoint(points[i], points[i+1]);
            points[i] = transformed[0];
            points[i+1] = transformed[1];
        }
    }

    public double getCenterX(){
        double sum = 0;

        for(int i=0;i<points.length;i+=2) {
            sum+=points[i];
        }
        return sum/(points.length/2);
    }

    public double getCenterY(){
        double sum = 0;

        for(int i=1;i<points.length;i+=2) {
            sum+=points[i];
        }
        return sum/(points.length/2);
    }
}
