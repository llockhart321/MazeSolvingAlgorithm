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
   ArrayList<t1_Node> currentPath = null;
   t1_Node lastPosition = null;
   t1_Node lastGoal = null;
   int currentPathIndex = 0;
   private boolean hasJumpedForHeight = false;
   private int horizontalMovesSinceJump = 0;
   private double lastX = 0;
   private boolean reachedMovementLimit = false;
   static boolean willRunSafe = true;
   
   
   public AI()
   {
      Image img = new Image("bubble.png");  // you can do whatever, but it should be unique for your AI.
      ptn = new ImagePattern(img);
   }
   
   //give it a good name :). It will appear above you AI when I test it in class.
   public String getName()
   {
      return "Lisan al Gaib";
   }  
   
   
   t1_GraphB theGraph;
   
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //setup code for your AI goes here.
   
   public void start(Level.LevelIterator currentLevel, double px, double py) {
      theGraph = new t1_GraphB(currentLevel);
      currentPath = null;
      lastPosition = null;
      lastGoal = null;
      currentPathIndex = 0;
   }
      /*public void start(Level.LevelIterator currentLevel, double px, double py)
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

   }*/
   
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //code that runs before each tick goes here.
   public void runEachTick(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) 
   {    
      if (runSafeTick(currentLevel, px, py, xv, yv, jump, isGrounded))
      {
         System.out.println("Running safe movement");
         runEachTickSafe(currentLevel, px, py, xv, yv, jump, isGrounded);
      } else
      {
         System.out.println("Running jump movement");
         runEachTickJump(currentLevel, px, py, xv, yv, jump, isGrounded);
      }
   }
   
   public boolean isNodeAboveTile(Level.LevelIterator levelIter, t1_Node node) {
       // Convert node coordinates from pixel space to tile space
       int nodeTileX = node.getX() / 30;
       int nodeTileY = node.getY() / 30;
       
       // Check the tile directly below the node
       Level.TileWrapper tileBelow = levelIter.getSpecificTile(nodeTileX, nodeTileY + 1);
       
       // Return true if there is a collidable tile directly below the node
       return tileBelow != null && tileBelow.getIsCollisionable();
   }
   
   public boolean runSafeTick(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) 
   {    
      int numberOfDownNodes = 0;
      if(currentPath != null)
      {
         numberOfDownNodes = currentPath.size();
         for(int i=0;i<currentPath.size();i++)
         {
            ArrayList<t1_MovementType> howToMove = currentPath.get(i).getHowToMove();
            for(int j=0; j<howToMove.size(); j++)
            {
               if(howToMove.get(j) == t1_MovementType.UP || isNodeAboveTile(currentLevel, currentPath.get(i)))
               {
                  numberOfDownNodes--;
                  break;
               }
            }
         }
      }
      System.out.println("Number of down nodes in path is " + numberOfDownNodes);
      if(numberOfDownNodes >= 2)
      {
         return false;
      }
      else
      {
         return true;
      }
    
   }
       
   public void runEachTickSafe(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) {
      theGraph.updateBreak(currentLevel);
    
      t1_Node whereIAm = theGraph.nodeAt(px, py);
      t1_Node goalNode = theGraph.getGoal();
    
      if(whereIAm != null && goalNode != null) {
        // Check if we need a new path
         boolean needNewPath = (currentPath == null || 
                             whereIAm != lastPosition || 
                             goalNode != lastGoal);
        
         if(needNewPath) {
            currentPath = theGraph.dijkstra(whereIAm, goalNode);
            lastPosition = whereIAm;
            lastGoal = goalNode;
            currentPathIndex = currentPath.size() - 1;
         }
        
         if(currentPath != null && currentPath.size() > 1) {
            t1_Node currentNode = currentPath.get(currentPathIndex);
            t1_Node targetNode = currentPath.get(currentPathIndex - 1);
            
            // Calculate distance to current target node
            double distanceToTarget = Math.sqrt(
                Math.pow((px - targetNode.getX()), 2) + 
                Math.pow((py - targetNode.getY()), 2)
               );
            
            // If we're close enough to current target, move to next node
            if(whereIAm == currentPath.get(currentPathIndex - 1) && currentPathIndex > 1) {
                currentPathIndex--;
                targetNode = currentPath.get(currentPathIndex - 1);
            }
            
            // Check for edges around target node
            double targetXOffset = 0;
            
            // Check for nodes to the left and right of target
            boolean hasLeftNode = false;
            boolean hasRightNode = false;
            double checkDistance = 35; // Distance to check for adjacent nodes
            
            for(t1_Node node : theGraph.theNodes) {
               if(node == targetNode) 
                  continue;
                
                // Check if node is at same Y level
               if(Math.abs(node.getY() - targetNode.getY()) < 1) {
                    // Check if node is to the left
                  if(Math.abs(node.getX() - (targetNode.getX() - 30)) < checkDistance) {
                     hasLeftNode = true;
                  }
                    // Check if node is to the right
                  if(Math.abs(node.getX() - (targetNode.getX() + 30)) < checkDistance) {
                     hasRightNode = true;
                  }
               }
            }
            
            // Adjust target position based on edge detection
            if(!hasLeftNode && !hasRightNode) {
                // No adjustment if no adjacent nodes or both sides have nodes
               targetXOffset = 0;
            } else if(!hasLeftNode) {
                // Move right if no left node
               targetXOffset = 15;
            } else if(!hasRightNode) {
                // Move left if no right node
               targetXOffset = -15;
            }
            
            // Determine movement direction considering the offset
            boolean needToJump = targetNode.getY() < currentNode.getY();
            double adjustedTargetX = targetNode.getX() + targetXOffset;
            
            // Expanded movement threshold to allow for smoother movement
            if(px < adjustedTargetX) {  // Reduced threshold from 5 to 2
               aDown = false;
               dDown = true;
            } else if(px > adjustedTargetX) {  // Reduced threshold from 5 to 2
               aDown = true;
               dDown = false;
            } else {
                // Only stop horizontal movement if very close to target
               if(Math.abs(px - adjustedTargetX) < 2) {
                  aDown = false;
                  dDown = false;
               }
            }
            
            // Always try to jump if we need to go up and we're grounded
            jumpDown = needToJump && isGrounded;
         }
      }
   } 

   public void runEachTickJump(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) {
      theGraph.updateBreak(currentLevel);
        
        // Reset states if grounded
      if(isGrounded) {
         hasJumpedForHeight = false;
         horizontalMovesSinceJump = 0;
         reachedMovementLimit = false;
      }
        
        // Track horizontal movement
      if(hasJumpedForHeight && !isGrounded) {
         if(Math.abs(px - lastX) > 5) { // If moved significantly horizontally
            horizontalMovesSinceJump++;
            if(horizontalMovesSinceJump >= 11) {
               reachedMovementLimit = true;
            }
            lastX = px;
         }
      }
        
      t1_Node whereIAm = theGraph.nodeAt(px, py);
      t1_Node goalNode = theGraph.getGoal();
        
      if(whereIAm != null && goalNode != null) {
         boolean needNewPath = (currentPath == null || 
                                 whereIAm != lastPosition || 
                                 goalNode != lastGoal);
            
         if(needNewPath) {
            currentPath = theGraph.dijkstra(whereIAm, goalNode);
            lastPosition = whereIAm;
            lastGoal = goalNode;
            currentPathIndex = currentPath.size() - 1;
         }
            
         if(currentPath != null && currentPath.size() > 1) {
            t1_Node currentNode = currentPath.get(currentPathIndex);
            t1_Node targetNode = currentPath.get(currentPathIndex - 1);
                
            double distanceToTarget = Math.sqrt(
                    Math.pow((px - targetNode.getX()), 2) + 
                    Math.pow((py - targetNode.getY()), 2)
                );
                
            if(whereIAm == currentPath.get(currentPathIndex - 1) && currentPathIndex > 1) {
                currentPathIndex--;
                targetNode = currentPath.get(currentPathIndex - 1);
            }
                
                // Check for gaps
            double checkDistance = 35;
            double gapCheckDistance = 60;
            boolean needToJumpForGap = false;
                
            boolean movingRight = px < targetNode.getX();
                
            int currentTileX = (int)(px / 30);
            int currentTileY = (int)(py / 30) + 1;
            int currentPlayerHeadY = (int)(py / 30);
                
            for(int i = 1; i <= 3; i++) {
               int checkX = currentTileX + (movingRight ? i : -i);
               Level.TileWrapper checkFloorTile = currentLevel.getSpecificTile(checkX, currentTileY);
               Level.TileWrapper checkWallTile = currentLevel.getSpecificTile(checkX, currentPlayerHeadY);
               Level.TileWrapper checkAboveWallTile = currentLevel.getSpecificTile(checkX, currentPlayerHeadY - 1);
                    
               boolean isGap = (checkFloorTile == null || !checkFloorTile.getIsCollisionable()) &&
                                   (checkWallTile == null || !checkWallTile.getIsCollisionable()) &&
                                   (checkAboveWallTile == null || !checkAboveWallTile.getIsCollisionable());
                    
               if(isGap) {
                  double distanceToGap = Math.abs((checkX * 30) - px);
                  if(distanceToGap < gapCheckDistance && isGrounded) {
                     needToJumpForGap = true;
                     break;
                  }
               }
            }
                
                // Check if we need to jump for height
            boolean needToJumpForHeight = targetNode.getY() < currentNode.getY();
                
                // Track when we start a height jump
            if(needToJumpForHeight && !hasJumpedForHeight && isGrounded) {
               hasJumpedForHeight = true;
               horizontalMovesSinceJump = 0;
               lastX = px;
               reachedMovementLimit = false;
            }
         
                // Handle horizontal movement
            if(!reachedMovementLimit) {
               if(px < targetNode.getX()) {
                  aDown = false;
                  dDown = true;
               } else if(px > targetNode.getX()) {
                  aDown = true;
                  dDown = false;
               } else {
                  if(Math.abs(px - targetNode.getX()) < 2) {
                     aDown = false;
                     dDown = false;
                  }
               }
            } else {
                    // Stop all horizontal movement if we've reached the limit
               aDown = false;
               dDown = false;
            }
                
                // Handle jumping
            jumpDown = (needToJumpForHeight || needToJumpForGap) && isGrounded;
         }
      }
   } 
  
   ////NOTE: this method is called in the Main. It is one of the AI's hooks.
   //whatever you want to draw should be put here.
   public void drawAIInfo(GraphicsContext gc) {
      theGraph.draw(gc);
      
      if(currentPath != null && currentPathIndex > 0) {
         t1_Node target = currentPath.get(currentPathIndex - 1);
         gc.setFill(Color.PURPLE);
         gc.fillOval(target.getX() + 4, target.getY() + 4, 22, 22);
      }
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
   public class t1_GraphB{
   
      ArrayList<t1_Node> theNodes = new ArrayList<t1_Node>();
      ArrayList<t1_Node> breakNodes = new ArrayList<t1_Node>();
      
      t1_Node goal;
   
      //creating the graph as we talked about in class.
      public t1_GraphB(Level.LevelIterator graphToCreate) {
         HashMap<String,String> isThereATileThere = new HashMap<String,String>();
         HashMap<t1_Node, Boolean> isVerticalNode = new HashMap<>();
         HashMap<t1_Node, Boolean> isDirectlyAboveBreak = new HashMap<>();
      
      // First pass: mark all existing tiles
         graphToCreate.resetIterator();
         while(graphToCreate.hasNext()) {
            Level.TileWrapper tw = graphToCreate.getNext();
            isThereATileThere.put(tw.getX()+"_"+tw.getY(),"YES!");
         }
      
      // Second pass: create nodes
         graphToCreate.resetIterator();
         while(graphToCreate.hasNext()) {
            Level.TileWrapper tw = graphToCreate.getNext();
         
         // Handle special tiles (start/end)
            if(tw.getIsEnd() || tw.getIsStart()) {
               t1_Node node = new t1_Node(tw.getX()*30, tw.getY()*30);
               theNodes.add(node);
               isVerticalNode.put(node, true);
            
               if(tw.getIsEnd()) {
                  goal = node;
               }
            
               if(tw.getIsBreak()) {
                  node.setBreakAmount(tw.getBreakTimer());
               } else {
                  node.setBreakAmount(-9);
               }
               continue;
            }
         
         // Handle break tiles
            if(tw.getIsBreak()) {
               t1_Node breakNode = new t1_Node(tw.getX()*30, (tw.getY()+1)*30);
               breakNode.setBreakMax(tw.getMaxBreakTimer());
               theNodes.add(breakNode);
               breakNodes.add(breakNode);
               isVerticalNode.put(breakNode, true);
            
            // Create node directly above break tile
               t1_Node nodeAboveBreak = new t1_Node(tw.getX()*30, (tw.getY())*30);
               theNodes.add(nodeAboveBreak);
               isVerticalNode.put(nodeAboveBreak, true);
               isDirectlyAboveBreak.put(nodeAboveBreak, true);
               continue;
            }
         
         // Generate vertical nodes with improved placement
            for(int xOffset = -1; xOffset <= 1; xOffset++) {
            // Standard vertical nodes
               for(int height = 1; height <= 6; height++) {
                  int checkX = tw.getX() + xOffset;
                  int checkY = tw.getY() - height;
                
                  if(isThereATileThere.get(checkX + "_" + checkY) != null) {
                     continue;
                  }
                
                  boolean nodeExists = false;
                  for(t1_Node existing : theNodes) {
                     if(existing.getX() == checkX*30 && existing.getY() == checkY*30) {
                        nodeExists = true;
                        isVerticalNode.put(existing, true);
                        break;
                     }
                  }
                
                  if(!nodeExists) {
                     t1_Node newNode = new t1_Node(checkX*30, checkY*30);
                     theNodes.add(newNode);
                     isVerticalNode.put(newNode, true);
                     newNode.setBreakAmount(-9);
                  }
               }
            
            // Add jump apex nodes at strategic heights
               for(int height = 3; height <= 8; height++) {
                  int checkX = tw.getX() + xOffset;
                  int checkY = tw.getY() - height;
                
                  if(isThereATileThere.get(checkX + "_" + checkY) == null) {
                     boolean nodeExists = false;
                     for(t1_Node existing : theNodes) {
                        if(existing.getX() == checkX*30 && existing.getY() == checkY*30) {
                           nodeExists = true;
                           break;
                        }
                     }
                    
                     if(!nodeExists) {
                        t1_Node jumpNode = new t1_Node(checkX*30, checkY*30);
                        theNodes.add(jumpNode);
                        isVerticalNode.put(jumpNode, true);
                        jumpNode.setBreakAmount(-9);
                     }
                  }
               }
            }
         
         // Generate horizontal spread nodes with landing points
            for(int xOffset = -7; xOffset <= 7; xOffset++) {
               if(xOffset >= -1 && xOffset <= 1) 
                  continue;
            
               for(int height = 1; height <= 6; height++) {
                  int checkX = tw.getX() + xOffset;
                  int checkY = tw.getY() - height;
                
                // Skip if there's a tile here
                  if(isThereATileThere.get(checkX + "_" + checkY) != null) {
                     continue;
                  }
                
                // Check if this could be a landing point
                  boolean isLandingPoint = false;
                  if(height == 1) {  // Only check for landing points at height 1
                     boolean hasGapBefore = true;
                     boolean hasTileAfter = false;
                    
                    // Check for gap before this point
                     for(int i = 1; i <= 2; i++) {
                        if(isThereATileThere.get((checkX - i) + "_" + tw.getY()) != null) {
                           hasGapBefore = false;
                           break;
                        }
                     }
                    
                    // Check for solid ground after this point
                     if(isThereATileThere.get(checkX + "_" + (tw.getY() + 1)) != null) {
                        hasTileAfter = true;
                     }
                    
                     isLandingPoint = hasGapBefore && hasTileAfter;
                  }
                
                  boolean nodeExists = false;
                  for(t1_Node existing : theNodes) {
                     if(existing.getX() == checkX*30 && existing.getY() == checkY*30) {
                        nodeExists = true;
                        if(isLandingPoint) {
                           isVerticalNode.put(existing, true);
                        }
                        break;
                     }
                  }
                
                  if(!nodeExists) {
                     t1_Node newNode = new t1_Node(checkX*30, checkY*30);
                     theNodes.add(newNode);
                     isVerticalNode.put(newNode, isLandingPoint);
                     newNode.setBreakAmount(-9);
                  }
               }
            }
         }
      
      // Create connections
         for(int i=0; i<theNodes.size(); i++) {
            for(int j=0; j<theNodes.size(); j++) {
               if(i == j) 
                  continue;
            
               t1_Node n1 = theNodes.get(i);
               t1_Node n2 = theNodes.get(j);
            
            // Skip any connection where n2 is a break node - break nodes should only have outgoing connections
               if(breakNodes.contains(n2)) {
                  continue;
               }
            
               double distance = Math.sqrt(
                  Math.pow(n1.getX()-n2.getX(), 2) + 
                  Math.pow(n1.getY()-n2.getY(), 2)
                  );
            
               if(distance < 35) {
                  if(breakNodes.contains(n1)) {
                  // Only create one-way connection FROM break node
                  // No bidirectional connections for break nodes
                     n1.addConnection(n2);
                     if(Math.abs(n1.getY() - n2.getY()) < 1) {
                     // For horizontal connections, direction based on relative position
                        n1.addMovementType(n1.getX() < n2.getX() ? t1_MovementType.LEFT : t1_MovementType.RIGHT);
                     } else {
                        n1.addMovementType(n1.getY() < n2.getY() ? t1_MovementType.DOWN : t1_MovementType.UP);
                     }
                     n1.addMovementTime(1);
                  }
                  // Handle regular node connections
                  else {
                     if(Math.abs(n1.getY() - n2.getY()) < 1) {
                        if(n1.getX() < n2.getX()) {
                        // Always add outgoing connection from n1
                           n1.addConnection(n2);
                           n1.addMovementType(t1_MovementType.RIGHT);
                           n1.addMovementTime(1);
                        
                        // Only add reverse connection if n1 is not a break node
                           if(!breakNodes.contains(n1)) {
                              n2.addConnection(n1);
                              n2.addMovementType(t1_MovementType.LEFT);
                              n2.addMovementTime(1);
                           }
                        }
                     }
                     // Vertical connections for regular nodes
                     else if(Math.abs(n1.getX() - n2.getX()) < 1) {
                        boolean n1Vertical = isVerticalNode.get(n1);
                        boolean n2Vertical = isVerticalNode.get(n2);
                     
                        int n1DistanceToTile = Integer.MAX_VALUE;
                        int n2DistanceToTile = Integer.MAX_VALUE;
                     
                        graphToCreate.resetIterator();
                        while(graphToCreate.hasNext()) {
                           Level.TileWrapper tw = graphToCreate.getNext();
                           if(Math.abs(tw.getX()*30 - n1.getX()) < 1 && tw.getY()*30 > n1.getY()) {
                              n1DistanceToTile = Math.min(n1DistanceToTile, 
                                 (int)Math.round((tw.getY()*30 - n1.getY()) / 30.0));
                           }
                           if(Math.abs(tw.getX()*30 - n2.getX()) < 1 && tw.getY()*30 > n2.getY()) {
                              n2DistanceToTile = Math.min(n2DistanceToTile, 
                                 (int)Math.round((tw.getY()*30 - n2.getY()) / 30.0));
                           }
                        }
                     
                        if(n1.getY() > n2.getY()) {
                           n2.addConnection(n1);
                           n2.addMovementType(t1_MovementType.DOWN);
                           n2.addMovementTime(1);
                        
                           if(n1Vertical && n2Vertical && 
                           (n1DistanceToTile <= 5 || n2DistanceToTile <= 5)) {
                              n1.addConnection(n2);
                              n1.addMovementType(t1_MovementType.UP);
                              n1.addMovementTime(1);
                           }
                        } else {
                           n1.addConnection(n2);
                           n1.addMovementType(t1_MovementType.DOWN);
                           n1.addMovementTime(1);
                        
                           if(n1Vertical && n2Vertical && 
                           (n1DistanceToTile <= 5 || n2DistanceToTile <= 5)) {
                              n2.addConnection(n1);
                              n2.addMovementType(t1_MovementType.UP);
                              n2.addMovementTime(1);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   
   
      public t1_Node getGoal()
      {
         return goal;
      }
      
      public double distance(double x1, double y1, double x2, double y2)
      {
         double d = Math.sqrt((x1 - x2)*(x1-x2) + (y1-y2)*(y1-y2));
         return d;  
      }
      
      public t1_Node nodeAt(double x, double y)
      {
         int shortestIndex = 0;
         double shortestDistance = distance(x,y,theNodes.get(0).getX(),theNodes.get(0).getY());
         
         for(int i=1;i<theNodes.size();i++)
         {
            double t;
            if((t = distance(x,y,theNodes.get(i).getX(),theNodes.get(i).getY())) < shortestDistance)
            {
               shortestIndex = i;
               shortestDistance = t;
            }
         }
         
         return theNodes.get(shortestIndex);
      }
      
      
      //drawing the graph
      public void draw(GraphicsContext gc)
      {
         for(int i=0;i<theNodes.size();i++)
         {
            theNodes.get(i).draw(gc);
         }
      }
      
      public void removeNodeFromGraph(t1_Node theNode)
      {
         //remove the node from the main list
         for(int i=0;i<theNodes.size();i++)
         {
            if(theNodes.get(i) == theNode)
            {
               theNodes.remove(i);
               break;
            }
         }
         
         //remove the connections
         theNode.destroy();
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
               removeNodeFromGraph(breakNodes.get(i));
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
      
      ArrayList<t1_Node> path = new ArrayList<t1_Node>();
      
      int runCounter=0;
      
      public ArrayList<t1_Node> dijkstra(t1_Node start, t1_Node end)
      {
         if (start == null || end == null) {
            return new ArrayList<>();
         }
      
      // Track visited nodes and costs
         HashSet<t1_Node> visited = new HashSet<>();
         HashMap<t1_Node, Double> costs = new HashMap<>();
         HashMap<t1_Node, Integer> upwardMoves = new HashMap<>();
      
      // Priority queue ordered by cost and number of upward moves
         PriorityQueue<t1_Node> pq = new PriorityQueue<>(
            (a, b) -> {
               double costA = costs.getOrDefault(a, Double.MAX_VALUE);
               double costB = costs.getOrDefault(b, Double.MAX_VALUE);
            
            // First compare by number of upward moves (prefer fewer)
               int upA = upwardMoves.getOrDefault(a, 0);
               int upB = upwardMoves.getOrDefault(b, 0);
               if (upA != upB) {
                  return Integer.compare(upA, upB);
               }
            
            // Then compare by path cost
               return Double.compare(costA, costB);
            });
      
      // Initialize start node
         costs.put(start, 0.0);
         upwardMoves.put(start, 0);
         pq.offer(start);
         start.setLastUsed(runCounter);
         int currentRun = runCounter++;
      
         while (!pq.isEmpty()) {
            t1_Node current = pq.poll();
         
            if (current == end) {
               break;
            }
         
            if (visited.contains(current)) {
               continue;
            }
            visited.add(current);
         
         // Process each neighbor
            for (int i = 0; i < current.getSize(); i++) {
               t1_Node neighbor = current.get(i);
               if (visited.contains(neighbor)) {
                  continue;
               }
            
            // Calculate new cost
               double currentCost = costs.get(current);
               double moveCost = current.timeToMove.get(i);
            
            // Add penalty for upward movement
               int newUpwardMoves = upwardMoves.get(current);
               if (current.howToMove.get(i) == t1_MovementType.UP) {
                  newUpwardMoves++;
               }
            
               double totalCost = currentCost + moveCost;
            
            // Update if this path is better
               if (totalCost < costs.getOrDefault(neighbor, Double.MAX_VALUE)) {
                  costs.put(neighbor, totalCost);
                  upwardMoves.put(neighbor, newUpwardMoves);
                  neighbor.setLastUsed(currentRun);
                  neighbor.setBackPointer(current);
                  pq.offer(neighbor);
               }
            }
         }
      
      // Reconstruct path
         path.clear();
         t1_Node current = end;
      
         if (current.getLastUsed() == currentRun) {
            while (current != start && current != null) {
               path.add(current);
               current = current.getBackPointer();
            }
            if (current != null) {
               path.add(current);
            }
         }
      
         return path;
      }  
   
   
      
   }
   
   public enum t1_MovementType {LEFT,RIGHT,NONE,UP,DOWN};
   
   public class t1_Node
   {
      //connections between nodes
      ArrayList<t1_Node> connections = new ArrayList<t1_Node>();
      ArrayList<t1_MovementType> howToMove = new ArrayList<t1_MovementType>();
      ArrayList<Double> timeToMove = new ArrayList<Double>();
   
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
      public double getBreakAmount() {
         return currentBreakAmount;
      }
         
      public ArrayList<t1_MovementType> getHowToMove()
      {
         return howToMove;
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
      
      public void addMovementType(t1_MovementType toAdd)
      {
         howToMove.add(toAdd);
      }
      
      public void addMovementTime(double d)
      {
         timeToMove.add(d);
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
      
      int lastUsed = -1;
      
      public int getLastUsed()
      {
         return lastUsed;
      }
      
      public void setLastUsed(int val)
      {
         lastUsed = val;
      }
      boolean inQueue = false;
      
      public t1_Node get(int i)
      {
         return connections.get(i);
      }
      
      Color fillColor = Color.YELLOW;
      
      boolean isClicked = false;
      
      //clicked method to change colors. this is really for debugging
      public void clicked(int option)
      {
         isClicked = true;
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
   
      public void draw(GraphicsContext gc) {
   // Draw all this node's connections
   for(int i=0; i<connections.size(); i++) {
      // Check if this connection is part of the current path
      boolean isPathConnection = false;
      if(AI.this.currentPath != null) {
         for(int j = 0; j < AI.this.currentPath.size() - 1; j++) {
            if((this == AI.this.currentPath.get(j) && connections.get(i) == AI.this.currentPath.get(j+1)) ||
             (this == AI.this.currentPath.get(j+1) && connections.get(i) == AI.this.currentPath.get(j))) {
               isPathConnection = true;
               break;
            }
         }
      }
   
      if(isPathConnection) {
         gc.setStroke(Color.WHITE);
      } else {
         // Check if this is a bidirectional horizontal connection
         boolean hasReverseConnection = false;
         t1_Node otherNode = connections.get(i);
         t1_MovementType thisDirection = howToMove.get(i);
      
         if(thisDirection == t1_MovementType.LEFT || thisDirection == t1_MovementType.RIGHT) {
            for(int j=0; j<otherNode.connections.size(); j++) {
               if(otherNode.connections.get(j) == this) {
                  hasReverseConnection = true;
                  break;
               }
            }
         }
      
         // Set colors based on movement type and direction
         if(thisDirection == t1_MovementType.RIGHT) {
            if(hasReverseConnection) {
               gc.setStroke(Color.ORANGE); // Bidirectional horizontal
            } else {
               gc.setStroke(Color.YELLOW); // Right only
            }
         } else if(thisDirection == t1_MovementType.LEFT) {
            if(hasReverseConnection) {
               gc.setStroke(Color.ORANGE); // Bidirectional horizontal
            } else {
               gc.setStroke(Color.GREEN); // Left only
            }
         } else if(thisDirection == t1_MovementType.UP) {
            gc.setStroke(Color.BLUE);
         } else if(thisDirection == t1_MovementType.DOWN) {
            gc.setStroke(Color.RED);
         }
      
         // Check for bidirectional vertical (purple)
         if(thisDirection == t1_MovementType.UP || thisDirection == t1_MovementType.DOWN) {
            for(int j=0; j<otherNode.connections.size(); j++) {
               if(otherNode.connections.get(j) == this &&
                 ((thisDirection == t1_MovementType.UP && 
                   otherNode.howToMove.get(j) == t1_MovementType.DOWN) ||
                  (thisDirection == t1_MovementType.DOWN && 
                   otherNode.howToMove.get(j) == t1_MovementType.UP))) {
                  gc.setStroke(Color.PURPLE);
                  break;
               }
            }
         }
      }
   
      gc.setLineWidth(3);
      gc.strokeLine(x+8+7, y+8+7, connections.get(i).x+8+7, connections.get(i).y+8+7);
   }

   // Check if this node has any incoming up connections
   boolean hasUpwardConnection = false;
   for(t1_Node otherNode : theGraph.theNodes) {
      for(int i = 0; i < otherNode.connections.size(); i++) {
         if(otherNode.connections.get(i) == this && 
            otherNode.howToMove.get(i) == t1_MovementType.UP) {
            hasUpwardConnection = true;
            break;
         }
      }
      if(hasUpwardConnection) break;
   }

   // Draw the node itself
   if(currentBreakAmount >= 0){
      // This is a break node, color it black
      gc.setFill(Color.BLACK);
   }else if(currentBreakAmount == -9) {
      // Non-breakable node
      gc.setFill(fillColor);
   } else {
      // Other nodes
      gc.setFill(fillColor);
   }

   gc.fillOval(x+8, y+8, 14, 14);
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
      
      public t1_MovementType howGetTo(t1_Node other)
      {
         for(int i=0;i<connections.size();i++)
         {
            if(connections.get(i) == other)
            {
               return howToMove.get(i);
            }
         }
         
         return t1_MovementType.NONE;
      }
      
      //this method should remove all the connections from corresponding arrayLists
      public void destroy()
      {
         for(int i=0;i<connections.size();i++)
         {
            t1_Node temp = connections.get(i);
            if(temp == this)
            {
               temp.connections.remove(i);
               temp.howToMove.remove(i);
               temp.timeToMove.remove(i);
            }
         }
      }
   }
}