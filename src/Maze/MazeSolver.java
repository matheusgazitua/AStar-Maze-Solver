package Maze;

import java.awt.Point;
import java.util.List;

public interface MazeSolver {

    void initialize(int[][] maze, Point start);

    boolean step(Point end);

    void solve(int[][] maze, Point start, Point end);

    List<Point> getPath();

    List<Point> getSolutionPath();
}