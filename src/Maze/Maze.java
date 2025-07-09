package Maze;

import java.awt.*;
import java.util.*;

public class Maze {
    private int rows;
    private int cols;
    private int[][] maze; // (0 = parede, 1 = caminho)
    private Point start, end;
    private Stack<Point> stack;
    private Boolean isDone;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        resetMaze();

    }

    public void generate() {
        while (step()) {
            // Continua chamando o step até o labirinto estar completo
        }
    }

    // Reinicia o labirinto
    public void resetMaze() {
        maze = new int[rows][cols];
        stack = new Stack<>();
        start = new Point(1, 1); // O labirinto sempre começa no canto superior esquerdo
        end = new Point(rows - 3, cols - 3); // O labirinto sempre termina no canto inferior direito

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = 0;
            }
        }


        stack.push(start);
        maze[start.x][start.y] = 1;
        isDone = false;
    }

    // Geração do labirinto com DFS (Depth‑First Search)
    public boolean step() {
        if (stack.isEmpty()) {
            isDone = true;
            return false; // Geração do labirinto completa
        }

        Point current = stack.peek();
        int x = current.x;
        int y = current.y;

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        shuffleArray(directions); // Aleatoriza a direção da geração do labirinto

        boolean foundNewPath = false;
        for (int[] dir : directions) {
            int newX = x + dir[0] * 2;
            int newY = y + dir[1] * 2;

            // Verifica se está dentro dos limites do labirinto e se o novo ponto ainda não foi visitado
            if (newX > 0 && newX < rows - 1 && newY > 0 && newY < cols - 1 && maze[newX][newY] == 0) {
                maze[x + dir[0]][y + dir[1]] = 1; // Marca o caminho entre o ponto atual e novo ponto
                maze[newX][newY] = 1; // Marca o novo ponto como um caminho
                stack.push(new Point(newX, newY)); // Adiciona o novo ponto na pilha
                foundNewPath = true;
                break;
            }
        }

        // Implementação do backtracking se nenhum novo caminho foi encontrado
        if (!foundNewPath) {
            stack.pop();
        }

        return true; // Retorna "true" porque a geração ainda não terminou
    }

    // Função auxiliar para aleatorizar as direções de geração
    private void shuffleArray(int[][] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public int[][] getMaze() {
        return maze;
    } // Retorna a matriz do labirinto

    public Point getStart() {
        return start;
    } // Retorna o ponto inicial

    public Point getEnd() {
        return end;
    } // Retorna o ponto final

    public boolean isDone() {
        return isDone;
    } // Informa se o labirinto está finalizado

}
