package sample.logic;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Sergiusz on 10.11.2016.
 */
public class GraphicParser {

    public static VectorGraphic readVectorGraphic(File f) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        VectorGraphic g = null;

        int width = 0;
        int height = 0;

        String line;
        String[] temp_arr;
        try {
            line = br.readLine();
            if(line!=null){
                temp_arr = line.split(";");
                width = Integer.parseInt(temp_arr[0]);
                height = Integer.parseInt(temp_arr[1]);
                g = new VectorGraphic(width, height);
                line = br.readLine();
            }
            else return null;

            while(line!=null){
                temp_arr = line.split(";");
                Color color = Color.web(temp_arr[0]);

                int pointsNum = temp_arr.length-1;
                double[] points = new double[pointsNum];

                for(int i=1;i<temp_arr.length;i++){
                    points[i-1] = Double.parseDouble(temp_arr[i]);
                }
                g.addNode(new VectorNode(color, points));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return g;
    }

    public static VectorGraphic readVectorGraphic(String file) throws FileNotFoundException {
        return readVectorGraphic(new File(file));
    }

    public static void saveVectorGraphic(VectorGraphic g, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));

        bw.write(g.getWidth()+";"+g.getHeight());
        bw.newLine();

        Iterator<VectorNode> it = g.getNodes().iterator();

        while(it.hasNext()){
            VectorNode node = it.next();
            Color color = node.getColor();
            String colorCode = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

            double[] points = node.getPoints();

            bw.write(colorCode+";");

            for(int i=0;i<points.length;i++){
                bw.write((int) points[i]);
                if(i!=points.length-1)bw.write(";");
            }
            bw.newLine();
        }
    }
    public static void saveVectorGraphic(VectorGraphic g, String file) throws IOException {
        saveVectorGraphic(g, new File(file));
    }
}
