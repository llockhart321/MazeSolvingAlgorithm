import javafx.event.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import java.util.*;
import javafx.scene.paint.Color;
import java.io.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

//this class was my player class for the larger project. This is what AIPlayer inherits from.

public class Player extends GameObject
{  

   public Player(double x, double y)
   {
      super(x,y);
   }
   
   public void drawMe(GraphicsContext gc)
   {
      gc.setFill(Color.GREEN);
      gc.fillRect(x,y,30,30);
   }
   
   boolean atGoal = false;
   
   public void atGoal()
   {
      atGoal = true;
   }
   
   public float getTime()
   {
      return totalTime;
   }
   
   public boolean getAtGoal()
   {
      return atGoal;
   }
}