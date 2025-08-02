# 🤖 Platformer AI Player

A graph-based AI agent for 2D tile-based platformer games, capable of intelligent pathfinding, dual movement strategies, and real-time navigation through complex levels.

---

## 🧠 Features

### 🔍 Intelligent Pathfinding
- Builds a navigation graph from level geometry
- Dijkstra-based pathfinding with custom heuristics
- Replans dynamically as the level state changes

### 🧗 Dual Movement Modes
**Safe Mode**  
- Conservative jumping and minimal vertical movement  
- Edge-aware positioning and smooth control handling

**Jump Mode**  
- Aggressive gap-crossing with optimized jump timing  
- Limits midair movement for realistic aerial control

### 🧩 Navigation Systems
- Breakable tile awareness and landing prediction  
- Gap detection, edge handling, and height management  
- Visual debug tools with color-coded node connections

---

## ⚙️ Technical Highlights

### Graph Construction
- Creates vertical, horizontal, landing, and break-specific nodes
- Classifies connections (bidirectional, unidirectional, etc.)
- Prunes unnecessary links for performance

### Path Execution
- Converts path nodes into movement inputs (A/D/Jump)
- Tracks grounded status and movement state
- Handles transition logic between movement modes

### Performance Optimization
- Spatial chunking (5x5 tile sections)
- Incremental graph updates
- Efficient path caching and reuse

---

## 🎮 How to Use

1. Launch the game and create/load a level
2. Press **Play** to start AI navigation
3. Green = start, Goal tile = destination
4. Use break tiles and gaps to test jump behavior

### Controls
**Editor Mode:**  
- `WASD` — move camera  
- Mouse — place tiles  
- `Play/Save` buttons — control simulation

**Game Mode:**  
- Mouse Click — debug path  
- `G/J/Space` — manual player test

---

## 🛠️ Configuration

- **Name:** `"Lisan al Gaib"` (in `getName()`)
- **Visual:** `bubble.png` sprite
- **Jump Distance:** Optimized for 3-tile gaps
- **Precision:** 2-pixel tolerance

---

## 🚧 Future Improvements

- Multi-agent coordination & team AI
- Momentum-based physics and learning feedback
- Procedural level adaptation and multi-goal navigation

---

## 📈 Performance Notes

- 60 FPS target with real-time responsiveness
- Efficient memory usage and computation minimization
- Designed for scalable level complexity

---

## 📌 Summary

This AI demonstrates advanced real-time pathfinding and state management for 2D platformers, balancing realism and responsiveness through a modular, extensible design.

---
