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

public class AI
{
   //this is for the image
   ImagePattern ptn;
   
   public AI()
   {
      Image img = new Image("bubble.png");  // you can do whatever, but it should be unique for your AI.
      ptn = new ImagePattern(img);
   }
   
   //give it a good name :). It will appear above you AI when I test it in class.
   public String getName()
   {
      return "Jack";
   }  
   
   
   t1_GraphB theGraph;
   
   // what the player is doing at the start of the game
   
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //setup code for your AI goes here.
   public void start(Level.LevelIterator currentLevel, double px, double py)
   {
      //reminder: px and py are the upper left coord of the player.
      //two classes you need to deal with:
      
      //Level.LevelIterator. It has two ways to get the tiles.
      //way #1 is iterate over all the tiles (in a non-specific order). See the graph's contructor for example usage
      //way #2 is to grab a specific tile from it's x and y. This is demonstrated in the graph's updateBreak method.

      //Level.TileWrapper. I wasn't going to let you have direct access to the tiles, so I package all the info for the tile into a "wrapper" class.
      //It has methods to get all the info you could want
      // getX - get X coord
      // getY - get Y coord
      // getName - get Tiletype's name
      // getIsStart - get if its the start tile
      // getIsEnd - get if its the goal tile
      // getIsBreak - get if it is a break tile
      // getBreakTimer - get the amount of time left on the break timer. -10 means not broken or not breakable. a num >= 0 means that is the time the tile has left.
      // getMaxBreakTimer - the Total time of the break timer.-10 means it doesn't break
      // getIsCollionable - whether a particular tile can be moved through.

      //creating a graph
      theGraph = new t1_GraphB(currentLevel);

   }
   
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //code that runs before each tick goes here.
   public void runEachTick(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded)
   {
      //reminder: px and py are the upper left coord of the player.
      //xv and yv is the current velocity
      //jump is how much "jump" will get added the next physics step (>0 is in the act of jumping)
      //isGrounded is whether the player is currently on the ground (i.e., will jump if you set jumpDown=true).


      theGraph.updateBreak(currentLevel);
   
      //set these variables if you want your AI to move in a particular way. Setting these vars is the only way to control the player. I.e., these are the only three controls.
      aDown = true;
      dDown = false;
      jumpDown = false;
      
      //so goes left
   }
   
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //whatever you want to draw should be put here.
   public void drawAIInfo(GraphicsContext gc)
   {
      theGraph.draw(gc);
   }
   

  
   //the game reads these three variables / methods to determine if the AI wishes to press the keys down. 
   protected boolean aDown;
   protected boolean dDown;
   protected boolean jumpDown;
   
   public boolean isADown()
   {
      return aDown;
   }
   
   public boolean isDDown()
   {
      return dDown;
   }
   
   public boolean isJumpDown()
   {
      return jumpDown;
   }
   
   
   //this method is called once the AI tries to jump in from GameObject. Will be called regardless of successful jump. Use isGrounded to determine if in the air.
   public void jumped()
   {
      jumpDown = false;
   }
   
   
   //this is used by a calling method to get the AI's image.
   public ImagePattern getFill()
   {
      return ptn;
   }
   
   //NOTE: this method is called in the Main. It is one of the AI's hooks. For if you want to use the mouse to debug
   public void clicked(int x, int y)
   {
      theGraph.clickPoint(x,y);
   }
   
   
   //I did these are inner classes, but you don't have to do. 
   // you must put your team name + underscore (like P1_ as a prefix to whatever your classes are)

   public class t1_GraphB
   {
 
      ArrayList<t1_Node> theNodes = new ArrayList<t1_Node>();
      ArrayList<t1_Node> breakNodes = new ArrayList<t1_Node>();
   
      //creating the graph as we talked about in class.
      public t1_GraphB(Level.LevelIterator graphToCreate)
      {
         HashMap<String,String> isThereATileThere = new HashMap<String,String>();
      
      
         //this is soooo much nice than the previous code we did in class. Iterators are your friends :)
         graphToCreate.resetIterator();
         while(graphToCreate.hasNext())
         {
            Level.TileWrapper tw = graphToCreate.getNext();
            
            isThereATileThere.put(tw.getX()+"_"+tw.getY(),"YES!");

         }
         
         graphToCreate.resetIterator();
         
         
         while(graphToCreate.hasNext())
         {
            Level.TileWrapper tw = graphToCreate.getNext();
            
            if(isThereATileThere.get(tw.getX()+"_"+(tw.getY()-1))== null)
            {
               
               theNodes.add(new t1_Node(tw.getX()*30, tw.getY()*30-30));
               
               //keep track of a list of break nodes as well
               if(tw.getIsBreak())
               {
                  breakNodes.add(theNodes.get(theNodes.size()-1));
                  
                  theNodes.get(theNodes.size()-1).setBreakMax(tw.getMaxBreakTimer());
               }
               else
               {
                  theNodes.get(theNodes.size()-1).setBreakAmount(-9); //-10 means not stepped on but breakable (according to my game) and -9 here means not breakable 

               }
            }
         }   
                         

         //N^2, could be better. Sort the nodes first by either x or y and just do the nodes that are nearby in the list.
         for(int i=0;i<theNodes.size();i++)
         {
            for(int j=0;j<theNodes.size();j++)
            {
               if(i != j)
               {
                  t1_Node n1 = theNodes.get(i);
                  t1_Node n2 = theNodes.get(j);
               
                  double d = Math.sqrt((n1.getX()-n2.getX())*(n1.getX()-n2.getX()) + (n1.getY()-n2.getY())*(n1.getY()-n2.getY()));
               
                  if(d < 35)
                  {
                     n1.addConnection(n2);
                     n2.addConnection(n1);
                  }
               }
            }
         }
     
      }
      
      
      //drawing the graph
      public void draw(GraphicsContext gc)
      {
         for(int i=0;i<theNodes.size();i++)
         {
            theNodes.get(i).draw(gc);
         }
      }
      
      public void updateBreak(Level.LevelIterator currentLevel)
      {
         //loop over all break nodes
         for(int i=0;i<breakNodes.size();i++)
         {
            //might have been better to leave the nodes in tileSpace.
            //I had to figure out what was wrong with my math. 
            //System.out.println(breakNodes.get(i).getX()/30+" "+(breakNodes.get(i).getY()+30)/30);
            
            //get a particular breakNode's tile wrapper.
            Level.TileWrapper tw = currentLevel.getSpecificTile(breakNodes.get(i).getX()/30,(breakNodes.get(i).getY()+30)/30);
            
            if(tw == null) //so the node no longer exsits. this means it broke.
            {
               breakNodes.remove(i);
               i--;
            }
            else //otherwise update the timer.
            {
               breakNodes.get(i).setBreakAmount(tw.getBreakTimer());
            }
         }
      }
      
      
      
      //takes in tilespace points
      public void clickPoint(int x, int y)
      {

         //this is because the the nodes are the upper left coords and the x and y are the center.
         x-=15; 
         y-=15; 
         
         for(int i=0;i<theNodes.size();i++)
         {
            t1_Node n1 = theNodes.get(i);

            double d = Math.sqrt((n1.getX()-x)*(n1.getX()-x) + (n1.getY()-y)*(n1.getY()-y));


            //if distance is within 20 px of the clicked tile.  
            if(d < 20)
            {
               
               
               if(start == null)
               {
                  start = n1;
                  n1.clicked(1);
               }
               else if(end == null)
               {
                  end = n1;
                  n1.clicked(0);
                  
                  dijkstra(start,end);
               }
               else
               {
                  n1.clicked(-1);
               }
            }
         }
      }
      
      
      t1_Node start=null;
      t1_Node end = null;
      
      public void dijkstra(t1_Node start, t1_Node end)
      {
         //unweighted dijkstra
         t1_Node current = start;
         LinkedList<t1_Node> myQueue = new LinkedList<t1_Node>(); //use priority queue in weighted dijsktra
         myQueue.addLast(current);
         
         while(myQueue.size()>0 && current != end)
         {
            current = myQueue.getFirst(); //get and remove first element.
            myQueue.removeFirst();
            for(int i=0;i<current.getSize();i++)
            {
               t1_Node temp = current.get(i);
               if(!temp.isInQueue())
               {
                  myQueue.addLast(temp);
                  temp.setIsInQueue(true);
                  temp.setBackPointer(current);
                  //set distance in weighted dijkstra
               }
               else
               {
                  //have to check if the path distance is < current distance in weighted dijkstra
               }
            }
         }
       
       
        
         //trace back back in the graph. I put a little safety code in place.
         current = end;
         
         if(end != null)
         {
            
            while(current != start && current != null)    
            {
               //System.out.println(current.getName());
               current.clicked(2);
               current = current.getBackPointer();
            }
            //System.out.println(current.getName());
            
            if(current != null)
            {
               current.clicked(2);
            }
            else
            {
               System.out.println("Current is null in AI.java");
            }
                 
         }
         else
         {
            System.out.println("End is null in AI.java");
         }
      }
  
   
   
   }
   
   public class t1_Node
   {
      //connections between nodes
      ArrayList<t1_Node> connections = new ArrayList<t1_Node>();
   
      int x,y;
      
      //for IDs...
      int id;
      static int idgen=0;
      
      double currentBreakAmount=0; //in my program -10 on a tile means not broken or not breakable (use tw.getIsBreak() to deteremine the diff). -9 in my implementaion means not breakable. and a positive number is how much time is left
      double maxBreakAmount=0;
      
      public t1_Node(int _x, int _y)
      {
         x = _x;
         y = _y;
         
         id = idgen++;
      }
      
      public int getX()
      {
         return x;
      }
      
      public int getY()
      {
         return y;
      }
      
      //name of a tile is x_y
      public String getName()
      {
         return x+"_"+y;
      }
   
      public void addConnection(t1_Node toAdd)
      {
         connections.add(toAdd);
      }
      
      //for keeping track of break tiles.
      public void setBreakAmount(double d)
      {
         currentBreakAmount = d;
      }
      
      public void setBreakMax(double d)
      {
         maxBreakAmount = d;
      }
      
      //these methods were for dijsktra
      public int getSize()
      {
         return connections.size();
      }
      
      public boolean isInQueue()
      {
         return inQueue;
      }
      
      public void setIsInQueue(boolean val)
      {
         inQueue = val;
      }
      boolean inQueue = false;
      
      public t1_Node get(int i)
      {
         return connections.get(i);
      }
      
      Color fillColor = Color.YELLOW;
      
      
      //clicked method to change colors. this is really for debugging
      public void clicked(int option)
      {
      
         fillColor = new Color(0,1,0,1);
         
         if(option == 0)
         {
            fillColor = Color.PINK;
         }
         if(option == 1)
         {
            fillColor = Color.PURPLE;
         }
         if(option == 2)
         {
            fillColor = Color.BLUE;
         }
      }
   
      public void draw(GraphicsContext gc)
      {
         
         //draw all this nodes's connections. NOTE: my implementation doesn't remove connections from each node when a node breaks.
         for(int i=0;i<connections.size();i++)
         {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeLine(x+8+7,y+8+7,connections.get(i).x+8+7,connections.get(i).y+8+7);
         }
         
         if(currentBreakAmount == -10)
         { //-10 means not stepped on or not breakable. You can do getIsBreak() from the tw if you want.
            gc.setFill(fillColor);
         }
         else if(currentBreakAmount == -9)
         {
            gc.setFill(fillColor); //someoen really wanted Cyan, don't remember who.
         }
         else
         {
            gc.setFill(Color.BLACK.interpolate(Color.WHITE,currentBreakAmount/maxBreakAmount)); //color based on break amount %
         }
         
         gc.fillOval(x+8,y+8,14,14);
      }
      
      t1_Node backPointer=null;
      
      public void setBackPointer(t1_Node theThing)
      {
         backPointer = theThing;
      }
      
      public t1_Node getBackPointer()
      {
         return backPointer;
      }
      
   }
}
