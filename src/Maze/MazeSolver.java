package Maze;

import java.awt.Point;
import java.util.List;

/**
 * Interface que define o contrato para qualquer algoritmo resolvedor de labirinto.
 * Isso permite que a UI trate diferentes resolvedores (sequenciais, paralelos)
 * de maneira uniforme.
 */
public interface MazeSolver {

    /**
     * Prepara o resolvedor com os dados iniciais do labirinto.
     * @param maze A matriz do labirinto.
     * @param start O ponto de início.
     */
    void initialize(int[][] maze, Point start);

    /**
     * Executa um único passo do algoritmo para fins de visualização.
     * @param end O ponto final do labirinto.
     * @return true se o algoritmo ainda estiver em execução, false se tiver terminado.
     */
    boolean step(Point end);

    /**
     * Resolve o labirinto o mais rápido possível, sem delay para visualização.
     * Este método é usado para o benchmark.
     * @param maze A matriz do labirinto.
     * @param start O ponto de início.
     * @param end O ponto final.
     */
    void solve(int[][] maze, Point start, Point end);

    /**
     * Retorna o caminho explorado pelo algoritmo.
     * @return Uma lista de pontos explorados.
     */
    List<Point> getPath();

    /**
     * Retorna o caminho final da solução.
     * @return Uma lista de pontos que compõem a solução.
     */
    List<Point> getSolutionPath();
}