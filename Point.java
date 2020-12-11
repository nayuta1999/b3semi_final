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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;


public class Point {
    private Point2D point;
    private double time;

    public Point(double x, double y){
        this.point = new Point2D(x,y);
    }
    public Point(double x, double y, double time){
        this.point = new Point2D(x,y);
        this.time = time;
    }

    public Point2D getPoint(){
        return point;
    }
    public double getTime(){
        return time;
    }

}
