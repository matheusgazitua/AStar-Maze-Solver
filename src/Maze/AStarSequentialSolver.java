package Maze;

import java.awt.Point;
import java.util.*;


public class AStarSequentialSolver implements MazeSolver {

    private int[][] maze;
    private List<Point> path;
    private List<Point> solutionPath;
    private Set<Point> visited;
    private PriorityQueue<Node> openSet;
    private Map<Point, Point> cameFrom;

    public AStarSequentialSolver() {
        this.path = new ArrayList<>();
        this.solutionPath = new ArrayList<>();
        this.visited = new HashSet<>();
        this.openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        this.cameFrom = new HashMap<>();
    }

    @Override
    public void initialize(int[][] maze, Point start) {
        this.maze = maze;

        path.clear();
        solutionPath.clear();
        visited.clear();
        openSet.clear();
        cameFrom.clear();

        openSet.add(new Node(start, 0));
        visited.add(start);
    }

    @Override
    public boolean step(Point end) {
        if (openSet.isEmpty()) {
            return false;
        }

        Node current = openSet.poll();
        path.add(current.point);

        if (current.point.equals(end)) {
            reconstructPath(current.point);
            return false;
        }

        expandNeighbors(current, end);

        return true;
    }

    @Override
    public void solve(int[][] maze, Point start, Point end) {
        initialize(maze, start);
        while(step(end)) {
            // Continua executando os passos em loop até encontrar a solução
        }
    }

    private void expandNeighbors(Node current, Point end) {
        List<Point> neighbors = getNeighbors(current.point);
        for (Point neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                cameFrom.put(neighbor, current.point);
                double gScore = calculateGScore(current.point, neighbor);
                double hScore = heuristic(neighbor, end);
                openSet.add(new Node(neighbor, gScore + hScore));
            }
        }
    }

    private List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int rows = this.maze.length;
        int cols = this.maze[0].length;

        for (int[] dir : directions) {
            int newX = p.x + dir[0];
            int newY = p.y + dir[1];

            // Verifica os limites e se é um caminho (valor == 1) usando a matriz interna
            if (newX >= 0 && newX < rows && newY >= 0 && newY < cols && this.maze[newX][newY] == 1) {
                neighbors.add(new Point(newX, newY));
            }
        }
        return neighbors;
    }

    private void reconstructPath(Point current) {
        solutionPath.clear();
        while (current != null) {
            solutionPath.add(0, current);
            current = cameFrom.get(current);
        }
    }

    private double heuristic(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private double calculateGScore(Point from, Point to) {
        return 1;
    }

    @Override
    public List<Point> getPath() {
        return path;
    }

    @Override
    public List<Point> getSolutionPath() {
        return solutionPath;
    }
}