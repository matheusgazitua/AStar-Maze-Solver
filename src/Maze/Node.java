package Maze;

import java.awt.*;

// Função auxiliar para a classe de A*
public class Node {
    Point point;
    double fScore; // Guarda o custo total estimado para se chegar até o final

    public Node(Point point, double fScore) {
        this.point = point;
        this.fScore = fScore;
    }
}
