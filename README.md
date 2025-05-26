# 2D Car Racing Game (Java Swing)

A modular, extensible 2D car racing game inspired by Road Rash, implemented in Java using the Swing GUI toolkit.

## Features
- Main game window with menus (Start, Options, Exit)
- 2D graphics for cars, tracks, obstacles
- Game loop for updating and rendering
- Car physics (acceleration, braking, steering, collisions)
- AI opponent cars
- Sound effects and background music
- Keyboard input for controls
- Game state management (menu, playing, paused, game over)
- Track generation/loading
- Scoring and race progression

## Project Structure
```
src/
  main/
    Game.java
    GameWindow.java
    GamePanel.java
    GameLoop.java
    GameState.java
    input/
      InputHandler.java
    audio/
      SoundManager.java
    graphics/
      Sprite.java
      Renderer.java
    physics/
      CarPhysics.java
    car/
      Car.java
      PlayerCar.java
      AICar.java
    track/
      Track.java
      TrackLoader.java
    obstacle/
      Obstacle.java
    ui/
      MenuPanel.java
      HUDPanel.java
resources/
  images/
  sounds/
  tracks/
```

## How to Run
1. Ensure you have Java 8 or higher installed.
2. Compile the source files in `src/main`.
3. Run `Game.java` as the main class.

## Libraries Used
- Java Swing (javax.swing)
- Java2D (java.awt.Graphics2D)
- javax.sound.sampled for audio

## Getting Started
- Start by exploring `Game.java` and `GameWindow.java` for the entry point and main window setup.
- Extend or modify components in a modular fashion as needed. 