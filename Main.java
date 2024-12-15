import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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

import java.util.HashMap;
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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Main extends Application {

    private int seconds = 0;
    private Label timerLabel;
    
    Canvas theCanvas = new Canvas(800,450);
    Level myLevel = new Level();
    
    ArrayList<Player> thePlayers = new ArrayList<Player>();
    Player thePlayer = null;
    private static boolean LOCK = false;
    private boolean isPaused = false;
    
    public static boolean isLocked()
    {
      return LOCK;
    }

    public void lock(){LOCK = true;}
    public void unlock(){LOCK=false;}
    private Main theMain;

    ArrayList<Tile> tileTemplates = new ArrayList<Tile>();
    
    ArrayList<InGameButton> buttons = new ArrayList<InGameButton>();

    @Override
    public void start(Stage primaryStage) {
    
         theMain = this;
      
        StackPane root = new StackPane(theCanvas);
        
        state = GameState.EDITOR;
        
        buttons.add(new InGameButton(350,0,100,40,-1));
        buttons.add(new InGameButton(720,95,80,260, -1));


        //file reading.
        try
        {
            Scanner scan = new Scanner(new File("tiles.txt"));
            
            while(scan.hasNext())
            {
               String name = scan.next();
               
               if(name.equals("tileplacements"))
                  break;
               
               float rf = scan.nextFloat();
               float gf = scan.nextFloat();
               float bf = scan.nextFloat();
               float af = scan.nextFloat();
               
               float ro = scan.nextFloat();
               float go = scan.nextFloat();
               float bo = scan.nextFloat();
               float ao = scan.nextFloat();
               
               String fname = scan.next();
               
               boolean isCol = scan.next().equals("true");
               
               TileDecoration head = null;
               
               int amt = scan.nextInt();
               
               for(int i=0;i<amt;i++)
               {
                  String dname = scan.next();
                  TileDecoration newdec= null;
                  switch(dname)
                  {
                     case "goal":
                        newdec = new GoalDecorator(scan);
                        break;
                     case "start":
                        newdec = new StartDecorator(scan);
                        break;
                     case "breaktile":
                        newdec = new BreakTileDecoration(scan);
                        break;                                                                                                                                 
                     default:
                        System.out.println(dname+" does not exist as a tile modifier");
                  }
                  
                  if(head != null)
                  {
                     newdec.setNext(head);
                     head = newdec;
                  }
                  else
                  {
                     head = newdec;
                  }
               }
               
               tileTemplates.add(new Tile(new Color(rf,gf,bf,af), new Color(ro,go,bo,ao), -1,-1,name, fname,isCol,head));
            }
            
            while(scan.hasNext())
            {
               String name = scan.next();
               int  x = scan.nextInt();
               int y = scan.nextInt();
            
               for(int i=0;i<tileTemplates.size();i++)
               {
                  if(name.equals(tileTemplates.get(i).getName()))
                  {
                     myLevel.addTile(new Tile(tileTemplates.get(i), x,y));  
                  }
               }
            }
            scan.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
         
        // Set up the scene
        Scene scene = new Scene(root, 800, 450);

        // Set up the stage (window) and add the scene
        primaryStage.setTitle("Bubble Maker 3000");
        primaryStage.setScene(scene);
        
        theCanvas.setOnKeyPressed(event -> handleKeyPress(event));
        theCanvas.setOnKeyReleased(event -> handleKeyRelease(event));
        theCanvas.setOnMouseMoved(new MouseMovedListener());
        theCanvas.setOnMousePressed(new MousePressedListener());

        // Show the window
        primaryStage.show();
        
        AnimationHandler ah = new AnimationHandler();
        ah.start();
         
        theCanvas.requestFocus();
    }

   boolean left_b = false;
   boolean right_b = false;
   boolean up_b = false;
   boolean down_b = false;
   boolean jump = false;
   
   private void handleKeyPress(KeyEvent event) 
   {
       switch (event.getCode()) 
       {
           case A:
              left_b = true;
               break;
           case D:
              right_b = true;
               break;
           case W:
              up_b = true;
               break;
           case S:
              down_b = true;
               break;
               
           case G:
              if(thePlayer != null) thePlayer.setDownA(true);
               break;
           case J:
              if(thePlayer != null) thePlayer.setDownD(true);
               break;
           case SPACE:
              jump = true;
              if(thePlayer != null) thePlayer.setDownJump(true);
              break;
        }
   }
   
   private void handleKeyRelease(KeyEvent event) 
   {
       switch (event.getCode()) 
       {
           case A:
              left_b = false;
               break;
           case D:
              right_b = false;
               break;
           case W:
              up_b = false;
               break;
           case S:
              down_b = false;
               break;
           case G:
              if(thePlayer != null) thePlayer.setDownA(false);
               break;
           case J:
              if(thePlayer != null) thePlayer.setDownD(false);
               break;
        }
   }

   float scrollx=0;
   float scrolly=0;
   
   float scrollspeed = 10;
   
   long lastTime;
   
   private static long startTime = System.nanoTime();
   private static int callCount = 0;

   public static void trackMethodCall() {
       callCount++;
       long elapsedTime = System.nanoTime() - startTime;
       if (elapsedTime >= 1_000_000_000) {
           System.out.println("Method was called " + callCount + " times in 1 second.");
           callCount = 0;
           startTime = System.nanoTime();
       }
   }

   int leftover=0;
   
   public class AnimationHandler extends AnimationTimer
   {
      public void handle(long currentTimeInNanoSeconds)
      {
         double timeElapsed = .001f;
         if(lastTime != 0)
         {
            timeElapsed = (currentTimeInNanoSeconds - lastTime)*.000000001;
            lastTime = currentTimeInNanoSeconds;
         }
         else
         {
            lastTime = currentTimeInNanoSeconds;
         }

         GraphicsContext gc = theCanvas.getGraphicsContext2D();
            
         gc.setFill(Color.BLACK);
         gc.fillRect(0,0,800,450);
            
         scrollx+=((left_b ? -1 : 0) + (right_b ? 1 : 0 ))*scrollspeed;
         scrolly+=((up_b ? -1 : 0) + (down_b ? 1 : 0 ))*scrollspeed;
        
         if(state == GameState.EDITOR)
         {
            gc.save();
            
            gc.translate(-scrollx,-scrolly);

            myLevel.drawAtPosition((int) scrollx/30,(int)scrolly/30, gc);
            
            int initialX = (int) scrollx/30;
            int initialY = (int) scrolly/30;
            
            for(int i=initialX-3;i<initialX+33;i++)
            {
               gc.setFill(partialGrey);
               gc.fillRect((i*30)-1,-50 + initialY*30,3,550);
               gc.setFill(partialBlack);
               gc.fillRect((i*30),-50 + initialY*30,1,550);
            }
            
            for(int i=initialY-3;i<initialY+33;i++)
            {
               gc.setFill(partialGrey);
               gc.fillRect(-30 + initialX*30,(i*30)-1, 880,3);
               gc.setFill(partialBlack);
               gc.fillRect(-30 + initialX*30,(i*30), 880,1);
            }
            
            if(overGameGrid)
            {
               int squarex = ((int)(mx+scrollx))/30;
               int squarey = ((int)(my+scrolly))/30;
               
               if(mx+scrollx < 0)
               {
                  squarex = (int)Math.floor((mx+scrollx)/30);
               }
                if(my+scrolly < 0)
               {
                  squarey = (int)Math.floor((my+scrolly)/30);
               }           
       
                    
               gc.setStroke(Color.WHITE);
               gc.strokeRect(squarex*30-1,squarey*30-1,33,33);
               gc.strokeRect(squarex*30,squarey*30,31,31);
            }
            
            gc.restore();       
            
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.GREY);
            gc.fillRect(350,0,100,40);
            gc.fillRect(720,95,80,260);
            
            gc.strokeRect(350,0,100,40);
            gc.strokeRect(720,95,80,260);
                        
            //play / save / pause buttons
            gc.setFill(Color.RED);
            gc.fillRect(360,5,30,30);
            gc.setFill(Color.GREEN);
            gc.fillRect(385,5,30,30);
            gc.setFill(Color.BLUE);
            gc.fillRect(410,5,30,30);
                        
            //up / down pages
            gc.setFill(Color.BLACK);
            gc.fillRect(725,315,30,30);
            gc.fillRect(765,315,30,30);
            
            for(int i=0;i<10;i++)
            {
               int tx = i%2*40;
               int ty = i/2*40;
               
               int index = i+editorpage*2;
               
               if(tileTemplates.size() > index && tileTemplates.get(i) != null)
               {
                  if(tileSelected == index)
                  {
                     tileTemplates.get(index).drawmeAbsoluteSelected(gc,tx+725,ty+100);
                  }
                  else
                  {
                     tileTemplates.get(index).drawmeAbsolute(gc,tx+725,ty+100);
                  }
                  
                  if(InGameButton.pointInsideOfStatic((int)mx,(int)my,tx+725,ty+100,30,30))
                  {
                     tileTemplates.get(index).drawmeAbsoluteHover(gc,tx+725,ty+100);
                  }
               }
            }

         }
         else if(state == GameState.GAME)
         {
            if(!isPaused) {
                leftover+=(int)(timeElapsed*100000);      
                int whole = leftover/1000;
                leftover = leftover%1000;
          
                for(int i=0;i<whole;i++)
                {
                    for(int j=0;j<thePlayers.size();j++)
                    {
                        playerLevels.get(j).runTiles(thePlayers, .01);
                     
                        thePlayers.get(j).move(playerLevels.get(j), thePlayers.get(j), theMain);
                        
                        if(thePlayers.get(j).getAtGoal())
                        {
                           System.out.println(thePlayers.get(j).getName()+" reached goal in "+thePlayers.get(j).getTime()+" ticks.");
                           thePlayers.remove(j);
                           if(thePlayers.size() != 0)
                              playerLevels.remove(j);
                           j--;
                        }
                    }
                }
            }
            
            gc.save();
            
            if(thePlayers.size() != 0)
            {
               gc.setFill(Color.WHITE);
               gc.fillText("Displaying level of player: "+thePlayers.get(0).getName(),0,20);
            }
            
            gc.translate(-scrollx,-scrolly);

            if (playerLevels.size() > 0) {
                playerLevels.get(0).drawAtPosition((int) scrollx/30,(int)scrolly/30, gc);
            }
            
            for(int i=0;i<thePlayers.size();i++)
            {
               thePlayers.get(i).drawMe(gc);
            }
            
            gc.restore();  
            
            // Draw buttons
            gc.setFill(Color.RED);
            gc.fillRect(360,5,30,30);
            gc.setFill(Color.GREEN);
            gc.fillRect(385,5,30,30);
            
            // Draw pause/play icon
            gc.setFill(Color.WHITE);
            if(isPaused) {
                // Draw play triangle
                double[] xPoints = {390, 390, 405};
                double[] yPoints = {10, 30, 20};
                gc.fillPolygon(xPoints, yPoints, 3);
            } else {
                // Draw pause bars
                gc.fillRect(392, 10, 6, 20);
                gc.fillRect(402, 10, 6, 20);
            }
         }
      }
      
      Color partialGrey = new Color(.5,.5,.5,.3);
      Color partialBlack = new Color(0,0,0,.3);
     
   }

    public static void main(String[] args) 
    {
        launch(args);
    }
    
    public void save()
    {
      String fname = "tiles.txt";
      
      try
      {
            PrintWriter writer = new PrintWriter(fname);
            
            for(int i=0;i<tileTemplates.size();i++)
            {
               tileTemplates.get(i).write(writer);
            }
            
            writer.println("tileplacements");
            
            for (Chunk value : myLevel.getMap().values()) 
            {
               for(int i=0;i<5;i++)
               {
                  for(int j=0;j<5;j++)
                  {
                     Tile t = value.getIndex(i,j);
                     if(t != null)
                     {
                        writer.println(t.getName()+" "+t.getX()+" "+t.getY());
                     }
                  }
               }
            }

            writer.close();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
    }
    
    public enum GameState {EDITOR, GAME};
    
    public static GameState state;
    
    float mx, my;
    
    boolean overGameGrid = false;
    
    int tileSelected = -1;
    int tileHover = -1;
    
    int editorpage=0;
    
   public class MouseMovedListener implements EventHandler<MouseEvent>  
   {
      public void handle(MouseEvent me)
      {
         mx = (int)me.getX();
         my = (int)me.getY(); 
         
         overGameGrid = true;
         
         for(int i=0;i<buttons.size();i++)
         {
            if(buttons.get(i).pointInsideOf((int)mx,(int)my))
            {
               overGameGrid = false;
            }
         }
      }
   }
   
   public class MousePressedListener implements EventHandler<MouseEvent>  
   {
      public void handle(MouseEvent me)
      {
         if(overGameGrid && state == GameState.EDITOR && tileSelected != -1)
         {
            int squarex = ((int)(mx+scrollx))/30;
            int squarey = ((int)(my+scrolly))/30;
            
            if(mx+scrollx < 0)
            {
               squarex = (int)Math.floor((mx+scrollx)/30);
            }
            
            if(my+scrolly < 0)
            {
               squarey = (int)Math.floor((my+scrolly)/30);
            }     
         
            if(tileTemplates.get(tileSelected).getName().equals("erase"))
            {
               myLevel.removeTile(squarex, squarey);
            }
            else
            {
               myLevel.addTile(new Tile(tileTemplates.get(tileSelected),squarex, squarey));
            }
         }
         else if (state == GameState.EDITOR)
         {
            for(int i=0;i<10;i++)
            {
               int tx = i%2*40;
               int ty = i/2*40;
               
               int index = i+editorpage*2;
               
               if(tileTemplates.size() > index && tileTemplates.get(index) != null)
               {
                  if(InGameButton.pointInsideOfStatic((int)mx,(int)my,tx+725,ty+100,30,30))
                  {
                     tileSelected = index;
                  }
               }
            }
            
            if(InGameButton.pointInsideOfStatic((int)mx,(int)my,725,315,30,30))
            {
               editorpage--;
               if(editorpage < 0)
               {
                  editorpage = 0;
               }
            }
            
            if(InGameButton.pointInsideOfStatic((int)mx,(int)my,765,315,30,30))
            {
               editorpage++;
               if(editorpage+10 > tileTemplates.size())
               {
                  editorpage--;
               }
            }
            
            if(InGameButton.pointInsideOfStatic((int)mx,(int)my,410,5,30,30))
            {
               save();
            }
            
            if(InGameButton.pointInsideOfStatic((int)mx,(int)my,360,5,30,30))
            {
               play();
            }
         }
         else if (state == GameState.GAME)
         {
            if(InGameButton.pointInsideOfStatic((int)mx,(int)my,360,5,30,30))
            {
               stopPlay();
            }
            else if(InGameButton.pointInsideOfStatic((int)mx,(int)my,385,5,30,30))
            {
               isPaused = !isPaused;
            }
            
            if(thePlayers.size() != 0 && !isPaused)
            {
               for(int i=0;i<thePlayers.size();i++)
               {
                  ((Player)thePlayers.get(i)).clicked((int)mx+(int)scrollx,(int)my+(int)scrolly);
               }   
            }     
         }
      }
   }

   ArrayList<Level> playerLevels = new ArrayList<Level>();
   
   public void play()
   {
      state = GameState.GAME;
      
      int playerx=-999999,playery=-999999;
      
      out:
      for (Chunk value : myLevel.getMap().values())
      {
         for(int i=0;i<5;i++)
         {
            for(int j=0;j<5;j++)
            {
               Tile t = value.getIndex(i,j);
               if(t != null && t.getIsStart())
               {
                  playerx = t.getX();
                  playery = t.getY();
                  
                  break out;
               }
            }
         }
      }
      
      if(playerx == -999999 && playery == -999999)
      {
         System.out.println("player not found");
         state = GameState.EDITOR;
         return;
      }
      
      thePlayers.clear();
      thePlayers.add(new AIPlayer(playerx*30,playery*30, new AI()));
      thePlayers.add(thePlayer = new Player(playerx*30,playery*30));
   
      playerLevels.clear();
   
      for(int i=0;i<thePlayers.size();i++)
      {
         playerLevels.add(new Level(myLevel));
      }
   
      for(int i=0;i<thePlayers.size();i++)
      {
         LOCK=true;
         ((Player)thePlayers.get(i)).start(playerLevels.get(i),thePlayers.get(i));
         LOCK=false;
      }

      isPaused = false;
   }
   
   public void stopPlay()
   {
      state = GameState.EDITOR;
   }
}