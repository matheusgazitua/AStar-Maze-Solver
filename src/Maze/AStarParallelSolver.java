package Maze;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;

public class AStarParallelSolver implements MazeSolver {

    private int[][] maze;
    private List<Point> path;
    private List<Point> solutionPath;
    private Set<Point> visited;
    private PriorityBlockingQueue<Node> openSet;
    private Map<Point, Point> cameFrom;

    private final int parallelism;

    public AStarParallelSolver(int parallelism) {
        // Garante que pelo menos 1 thread seja usada
        this.parallelism = Math.max(1, parallelism);

        this.path = Collections.synchronizedList(new ArrayList<>());
        this.solutionPath = Collections.synchronizedList(new ArrayList<>());
        this.visited = ConcurrentHashMap.newKeySet();
        this.openSet = new PriorityBlockingQueue<>(100, Comparator.comparingDouble(n -> n.fScore));
        this.cameFrom = new ConcurrentHashMap<>();
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
    public void solve(int[][] maze, Point start, Point end) {
        initialize(maze, start);

        // Cria um ForkJoinPool customizado com o número de threads especificado
        ForkJoinPool customPool = new ForkJoinPool(this.parallelism);

        try {
            // Executa a lógica de solução dentro do pool customizado
            customPool.submit(() -> {
                while (!openSet.isEmpty() && solutionPath.isEmpty()) {
                    Node current = openSet.poll();
                    if (current == null) continue;

                    if (current.point.equals(end)) {
                        reconstructPath(current.point);
                        return;
                    }
                    expandNeighborsInParallel(current, end);
                }
            }).get(); // .get() espera a conclusão da tarefa
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customPool.shutdown();
        }
    }

    private void expandNeighborsInParallel(Node current, Point end) {
        getNeighbors(current.point).parallelStream().forEach(neighbor -> {
            processNeighbor(current, neighbor, end);
        });
    }

    @Override
    public boolean step(Point end) {
        if (openSet.isEmpty()) return false;
        Node current = openSet.poll();
        if (current == null) return true;
        path.add(current.point);
        if (current.point.equals(end)) {
            reconstructPath(current.point);
            return false;
        }
        getNeighbors(current.point).forEach(neighbor -> processNeighbor(current, neighbor, end));
        return true;
    }

    private void processNeighbor(Node current, Point neighbor, Point end) {
        if (visited.add(neighbor)) {
            cameFrom.put(neighbor, current.point);
            double gScore = 1;
            double hScore = Math.abs(neighbor.x - end.x) + Math.abs(neighbor.y - end.y);
            openSet.add(new Node(neighbor, gScore + hScore));
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

    @Override
    public List<Point> getPath() { return path; }

    @Override
    public List<Point> getSolutionPath() { return solutionPath; }
}