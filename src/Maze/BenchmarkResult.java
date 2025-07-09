package Maze;

/**
 * Uma classe de dados simples para armazenar o resultado
 * de um benchmark para um único labirinto e algoritmo.
 */
public class BenchmarkResult {
    private String mazeName;
    private String algorithmName;
    private long timeMillis;

    public BenchmarkResult(String mazeName, String algorithmName, long timeMillis) {
        this.mazeName = mazeName;
        this.algorithmName = algorithmName;
        this.timeMillis = timeMillis;
    }

    public String getMazeName() { return mazeName; }
    public String getAlgorithmName() { return algorithmName; }
    public long getTimeMillis() { return timeMillis; }
}