package sample.logic;

/**
 * Created by Sergiusz on 19.11.2016.
 */
public class Transform {
    private double[] matrix;

    public Transform(double a, double b, double c, double d, double e, double f, double g, double h, double i){
        matrix = new double[]{a,b,c,d,e,f,g,h,i};
    }

    public Transform(){
        this(1,0,0,0,1,0,0,0,1);
    }

    public void addRotation(double angle){
        double[] newMatrix = new double[]{1,0,0,0,1,0,0,0,1};
        newMatrix[0] = matrix[0]*Math.cos(angle)+matrix[1]*(-Math.sin(angle));
        newMatrix[1] = matrix[0]*Math.sin(angle)+matrix[1]*Math.cos(angle);
        newMatrix[2] = matrix[2];
        newMatrix[3] = matrix[3]*Math.cos(angle)+matrix[4]*(-Math.sin(angle));
        newMatrix[4] = matrix[3]*Math.sin(angle)+matrix[4]*Math.cos(angle);
        newMatrix[5] = matrix[5];
        newMatrix[6] = matrix[6]*Math.cos(angle)+matrix[7]*(-Math.sin(angle));
        newMatrix[7] = matrix[6]*Math.sin(angle)+matrix[7]*Math.cos(angle);
        newMatrix[8] = matrix[8];
        matrix = newMatrix;
    }

    public void addScale(double sx, double sy){
        matrix[0] = matrix[0]*sx;
        matrix[1] = matrix[1]*sy;
        matrix[3] = matrix[3]*sx;
        matrix[4] = matrix[4]*sy;
        matrix[6] = matrix[6]*sx;
        matrix[7] = matrix[7]*sy;
    }

    public void addTranslation(double x, double y){
        matrix[0] = matrix[0]+matrix[2]*x;
        matrix[1] = matrix[1]+matrix[2]*y;
        matrix[3] = matrix[3]+matrix[5]*x;
        matrix[4] = matrix[4]+matrix[5]*y;
        matrix[6] = matrix[6]+matrix[8]*x;
        matrix[7] = matrix[7]+matrix[8]*y;
    }

    public double[] getMatrix(){
        return matrix;
    }

    public double[] getInverseMatrix(){
        double det = matrix[0]*matrix[4]*matrix[8]+
                     matrix[1]*matrix[5]*matrix[6]+
                     matrix[2]*matrix[3]*matrix[7]-
                     matrix[2]*matrix[4]*matrix[6]-
                     matrix[0]*matrix[5]*matrix[7]-
                     matrix[1]*matrix[3]*matrix[8];

        if(det==0)return null;
        double[] inverse = new double[9];

        inverse[0] = (matrix[4]*matrix[8]-matrix[5]*matrix[7])/det;
        inverse[3] = -(matrix[3]*matrix[8]-matrix[5]*matrix[6])/det;
        inverse[6] = (matrix[3]*matrix[7]-matrix[4]*matrix[6])/det;
        inverse[1] = -(matrix[1]*matrix[8]-matrix[2]*matrix[7])/det;
        inverse[4] = (matrix[0]*matrix[8]-matrix[2]*matrix[6])/det;
        inverse[7] = -(matrix[0]*matrix[7]-matrix[1]*matrix[6])/det;
        inverse[2] = (matrix[1]*matrix[5]-matrix[2]*matrix[4])/det;
        inverse[5] = -(matrix[0]*matrix[5]-matrix[2]*matrix[3])/det;
        inverse[8] = (matrix[0]*matrix[4]-matrix[1]*matrix[3])/det;
        return inverse;
    }

    public double[] transformPoint(double x, double y){
        double third = x*matrix[2]+y*matrix[5]+matrix[8];
        //System.out.println("Transforming point: ["+x+","+y+","+third+"]");
        //System.out.println("Matrix:");
        //printMatrix();
        double[] newPoint = new double[]{(x*matrix[0]+y*matrix[3]+matrix[6])/third, (x*matrix[1]+y*matrix[4]+matrix[7])/third};

        //System.out.println("result: ["+newPoint[0]+","+newPoint[1]+",1]");
        return newPoint;
    }

    public double[] retransformPoint(double x, double y){
        double[] inverse = getInverseMatrix();
        double third = x*inverse[2]+y*inverse[5]+inverse[8];
        return new double[]{(x*inverse[0]+y*inverse[3]+inverse[6])/third, (x*inverse[1]+y*inverse[4]+inverse[7])/third};
    }
    public static Transform getRotation(double angle){
        Transform t = new Transform();
        t.addRotation(angle);
        return t;
    }

    public static Transform getScale(double sx, double sy){
        Transform t = new Transform();
        t.addScale(sx, sy);
        return t;
    }

    public static Transform getTranslate(double x, double y){
        Transform t = new Transform();
        t.addTranslation(x, y);
        return t;
    }

    public void printMatrix(){
        System.out.println(matrix[0]+" "+matrix[1]+" "+matrix[2]);
        System.out.println(matrix[3]+" "+matrix[4]+" "+matrix[5]);
        System.out.println(matrix[6]+" "+matrix[7]+" "+matrix[8]);
    }

    public void printInverseMatrix(){
        double[] matrix = getInverseMatrix();
        System.out.println(matrix[0]+" "+matrix[1]+" "+matrix[2]);
        System.out.println(matrix[3]+" "+matrix[4]+" "+matrix[5]);
        System.out.println(matrix[6]+" "+matrix[7]+" "+matrix[8]);
    }
}
