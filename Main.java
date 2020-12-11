/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Precision;

/**
 * @author takahashi
 */
public class Main extends Application {

    private ArrayList<Point2D> point = new ArrayList<Point2D>();
    private boolean move_flag;
    private int move_i;
    private boolean dragEvent;
    private List<Double> knotPoint = new ArrayList<Double>();
    private Canvas canvas = new Canvas(1270, 1270);
    private Stage primaryStage;
    private List<Point2D> cursolPoints = new ArrayList<Point2D>();
    private RealMatrix D;
    private RealMatrix N;
    private RealMatrix N_t;
    private RealMatrix P;
    private double start_time;
    private double end_time;
    private ArrayList<Point> points;
    private List<Double> d_i = new ArrayList<Double>();

    public Main() {
        this.move_i = -1;
        this.move_flag = false;
        this.dragEvent = false;
        this.points = new ArrayList<Point>();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        canvas.setOnMousePressed(this::drawPoint);
        canvas.setOnMouseDragged(this::movePoint);
        canvas.setOnDragDetected(this::getNearPoint);
        //canvas.setOnMouseDragReleased(this::finishDrag);
        canvas.setOnMouseReleased(this::finishDrag);
        canvas.setOnMouseMoved(this::setCursol);
        this.primaryStage = primaryStage;
        primaryStage.setTitle("b3semi");
        primaryStage.setScene(new Scene(new Pane(canvas)));
        primaryStage.show();
    }

    public void setCursol(MouseEvent e) {
        boolean flag = false;
        int select_i = -1;
        int i = 0;
        for (Point2D point : this.point) {
            if (point.distance(e.getX() - 5, e.getY() - 5) <= 20) {
                this.primaryStage.getScene().setCursor(Cursor.CROSSHAIR);
                flag = true;
                select_i = i;
            }
            i++;
        }
        if (flag == true) {
            if (this.point.get(select_i).distance(e.getX() - 5, e.getY() - 5) <= 20) {
                select_i = -1;
                this.primaryStage.getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    public int getNearPoint(MouseEvent e) {
        for (int i = 0; i < point.size(); i++) {
            if (point.get(i).distance(e.getX() - 5, e.getY() - 5) <= 20) {
                this.move_i = i;
                return this.move_i;
            }
        }
        return -1;
    }

    public void movePoint(MouseEvent e) {
        //描いた点の入力
        this.points.add(new Point(e.getX(), e.getY(), System.nanoTime()/Math.pow(10,9)));
        this.clearCanvas();
        this.display_sketchPoint();
    }

    public void finishDrag(MouseEvent e) {
        this.calculateP();
        this.display_point();
        this.kinjiSpline();
        this.display_sketchPoint();
    }

    public void drawPoint(MouseEvent e) {

        //前回書いたデータの削除
        this.points.clear();
        this.point.clear();

    }

    public void calculateP() {
        int i = 0;
        int n = 3;
        int L = 3;
        int m = 10;
        double tmp_d[][] = new double[this.points.size()][2];
        for (Point p : this.points) {
            tmp_d[i][0] = p.getPoint().getX();
            tmp_d[i][1] = p.getPoint().getY();
            i++;
        }
        this.D = MatrixUtils.createRealMatrix(tmp_d);

        this.knotPoint.clear();

        this.start_time = points.get(0).getTime();
        this.end_time = points.get(points.size()-1).getTime();

        for (int l = 0; l < n + 1; l++) {
            this.knotPoint.add(this.start_time);
        }
        for (int l = 1; l <= m - 2 * n - 2; l++) {
            this.knotPoint.add(this.start_time+l * (double) (this.end_time - this.start_time) / 3);
        }
        for (int l = 0; l < n + 1; l++) {
            this.knotPoint.add((double) (this.end_time));
        }
        System.out.println(this.knotPoint);
        double tmp_n[][] = new double[this.points.size()][m - n - 1];
        int k = 0;
        for (Point u : this.points) {
            for (int j = 0; j < m - n - 1; j++) {
                tmp_n[k][j] = splineBase(n, j, u.getTime());
            }
            k++;
        }
        this.N = MatrixUtils.createRealMatrix(tmp_n);
        System.out.println(this.D);
        this.N_t = this.N.transpose();
        System.out.println("n:"+this.N);
        this.P = new QRDecomposition(this.N_t.multiply(this.N)).getSolver().solve(this.N_t.multiply(this.D));
        System.out.println(this.P);
        for (i = 0; i < 6; i++) {
            this.point.add(new Point2D(this.P.getRow(i)[0], this.P.getRow(i)[1]));
        }
    }



    public void kinjiSpline(){
        Point2D before_point = new Point2D(this.point.get(0).getX(), this.point.get(0).getY());

        ArrayList<Point2D> result_point = new ArrayList<Point2D>();
        int i = 0;
        int n = 3;
        int L = 3;
        int m = 10;

        for (double t = this.knotPoint.get(n); t <= this.knotPoint.get(m-1-n); t += 0.01) {
            result_point.add(splinePolynomial(t));
            canvas.getGraphicsContext2D().setFill(Color.BLUE);
            canvas.getGraphicsContext2D().fillOval(result_point.get(i).getX() - 5, result_point.get(i).getY() - 5, 10, 10);
            before_point = new Point2D(result_point.get(i).getX(), result_point.get(i).getY());

            //differentialSpline(t);
            i++;
        }


    }
    public void clearCanvas() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getHeight(), canvas.getWidth());
    }

    public void displayCursolPoint() {
        canvas.getGraphicsContext2D().setFill(Color.BLUE);
        for (Point2D point : this.cursolPoints) {
            canvas.getGraphicsContext2D()
                    .strokeOval(point.getX() - 5, point.getY() - 5, 10, 10);
            canvas.getGraphicsContext2D()
                    .fillOval(point.getX() - 5, point.getY() - 5, 10, 10);
        }
    }

    public void display_point() {
        canvas.getGraphicsContext2D().setFill(Color.YELLOW);
        for (Point2D point : this.point) {
            canvas.getGraphicsContext2D()
                    .strokeOval(point.getX() - 5, point.getY() - 5, 10, 10);
            canvas.getGraphicsContext2D()
                    .fillOval(point.getX() - 5, point.getY() - 5, 10, 10);
        }
    }

    public void display_sketchPoint() {
        canvas.getGraphicsContext2D().setFill(Color.RED);
        for (Point point : this.points) {
            canvas.getGraphicsContext2D()
                    .strokeOval(point.getPoint().getX() - 5, point.getPoint().getY() - 5, 3, 3);
            canvas.getGraphicsContext2D()
                    .fillOval(point.getPoint().getX() - 5, point.getPoint().getY() - 5, 3, 3);
        }

    }

    private Point2D splinePolynomial(double t) {
        int n = 3;
        int L = 3;
        int m = 10;
        Point2D result = point.get(0).multiply(splineBase(n,0,t));
        for(int j = 1;j<=L+n-1;j++){
            //System.out.println(splineBase(n,j,t));
            if(j == 5){
                //System.out.println(splineBase(n,j,t));
            }
            result = result.add(point.get(j).multiply(splineBase(n,j,t)));
        }
        return result;
    }

    private double splineBase(int k,int j,double t){
        if(Precision.equals(t, knotPoint.get(this.knotPoint.size()-k-1),1.0e-8)){
            if(j == this.knotPoint.size()-k-1){
                return 1;
            }
            return 0;
        }
        if(k == 0){
            if(this.knotPoint.get(j) <= t && t < this.knotPoint.get(j+1)){
                return 1;
            }
            else{
                return 0;
            }
        }
        else{
            double tmp_a = (t-this.knotPoint.get(j))*splineBase(k-1,j,t)/(this.knotPoint.get(j+k)-this.knotPoint.get(j));
            if(this.knotPoint.get(j+k)-this.knotPoint.get(j) == 0.0){
                tmp_a = 0.0;
            }
            double tmp_b = (this.knotPoint.get(j+k+1)-t)*splineBase(k-1,j+1,t)/(this.knotPoint.get(j+k+1)-this.knotPoint.get(j+1));
            if(this.knotPoint.get(j+k+1)-this.knotPoint.get(j+1) == 0.0){
                tmp_b = 0.0;
            }
            return tmp_a+tmp_b;
        }
    }



    public double differentialSpline(double t){
        int m=10;
        int k=0;
        int n=3;
        double result=0;
        ArrayList<Double> N_p = new ArrayList<Double>();
        for(int j = 0;j<=3+n-1;j++){
            N_p.add(differentialSplineBase(n,j,t));
        }
        for(int i=1;i<n+1;i++){
            result += N_p.get(i+1);
        }

        return result;
    }

    private double differentialSplineBase(int n,int j,double t){
        if(Precision.equals(t, knotPoint.get(this.knotPoint.size()-n-1),1.0e-8)){
            if(j == this.knotPoint.size()-n-1){
                return 1;
            }
            return 0;
        }
        if(n == 0){
            if(this.knotPoint.get(j) <= t && t < this.knotPoint.get(j+1)){
                return 1;
            }
            else{
                return 0;
            }
        }
        else{
            double tmp_a = (t-this.knotPoint.get(j))*differentialSplineBase(n-2,j,t)/(this.knotPoint.get(j+n)-this.knotPoint.get(j));
            if(this.knotPoint.get(j+n)-this.knotPoint.get(j) == 0.0){
                tmp_a = 0.0;
            }
            double tmp_b = (this.knotPoint.get(j+n+1)-t)*differentialSplineBase(n-2,j+1,t)/(this.knotPoint.get(j+n+1)-this.knotPoint.get(j+1));
            if(this.knotPoint.get(j+n+1)-this.knotPoint.get(j+1) == 0.0){
                tmp_b = 0.0;
            }
            return tmp_a+tmp_b;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}