package Maze;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BenchmarkManager {
    private List<BenchmarkResult> results;
    private int mazeCounter;

    private String lastMazeSize = "N/A";
    private int lastBatchCount = 0;
    private int lastThreadCount = 0;

    public BenchmarkManager() {
        this.results = new ArrayList<>();
        this.mazeCounter = 0;
    }

    public void configureNewBatch(String mazeSize, int batchCount, int threadCount) {
        this.results.clear();
        this.mazeCounter = 0;
        this.lastMazeSize = mazeSize;
        this.lastBatchCount = batchCount;
        this.lastThreadCount = threadCount;
    }

    public void nextMaze() {
        this.mazeCounter++;
    }

    public void addResult(String algorithmName, long timeMillis) {
        String mazeName = "Labirinto " + mazeCounter;
        results.add(new BenchmarkResult(mazeName, algorithmName, timeMillis));
    }

    public String generateTxtReport() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Resultados do Benchmark com %d labirintos, de tamanho %s e com %d threads:\n\n",
                this.lastBatchCount, this.lastMazeSize, this.lastThreadCount));

        sb.append(String.format("%-20s %-25s %-25s\n", "Número do labirinto", "Tempo sequencial (ms)", "Tempo paralelo (ms)"));
        sb.append(String.format("%-20s %-25s %-25s\n", "--------------------", "-------------------------", "-------------------------"));

        Map<String, List<BenchmarkResult>> groupedByMaze = results.stream()
                .collect(Collectors.groupingBy(
                        BenchmarkResult::getMazeName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<BenchmarkResult>> entry : groupedByMaze.entrySet()) {
            String mazeName = entry.getKey();
            List<BenchmarkResult> mazeResults = entry.getValue();

            long sequentialTime = mazeResults.stream()
                    .filter(r -> r.getAlgorithmName().toLowerCase().contains("sequencial"))
                    .findFirst()
                    .map(BenchmarkResult::getTimeMillis)
                    .orElse(0L);

            long parallelTime = mazeResults.stream()
                    .filter(r -> r.getAlgorithmName().toLowerCase().contains("paralelo"))
                    .findFirst()
                    .map(BenchmarkResult::getTimeMillis)
                    .orElse(0L);

            sb.append(String.format("%-20s %-25d %-25d\n", mazeName, sequentialTime, parallelTime));
        }

        return sb.toString();
    }

    public String generateCsvReport() {
        StringBuilder sb = new StringBuilder("LABIRINTO,TEMPO SEQUENCIAL (ms),TEMPO PARALELO (ms),TAMANHO_LABIRINTO,QTD_TOTAL_LABIRINTOS,NUM_THREADS\n");

        Map<String, List<BenchmarkResult>> groupedByMaze = results.stream()
                .collect(Collectors.groupingBy(
                        BenchmarkResult::getMazeName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<BenchmarkResult>> entry : groupedByMaze.entrySet()) {
            String mazeName = entry.getKey();
            List<BenchmarkResult> mazeResults = entry.getValue();
            long sequentialTime = mazeResults.stream()
                    .filter(r -> r.getAlgorithmName().toLowerCase().contains("sequencial"))
                    .findFirst()
                    .map(BenchmarkResult::getTimeMillis)
                    .orElse(0L);
            long parallelTime = mazeResults.stream()
                    .filter(r -> r.getAlgorithmName().toLowerCase().contains("paralelo"))
                    .findFirst()
                    .map(BenchmarkResult::getTimeMillis)
                    .orElse(0L);
            sb.append(String.format("%s,%d,%d,%s,%d,%d\n",
                    mazeName, sequentialTime, parallelTime, this.lastMazeSize, this.lastBatchCount, this.lastThreadCount));
        }
        return sb.toString();
    }

    public void saveReportsToFile() {
        try (FileWriter txtWriter = new FileWriter("benchmark_results.txt")) {
            txtWriter.write(generateTxtReport());
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo TXT: " + e.getMessage());
        }
        try (FileWriter csvWriter = new FileWriter("benchmark_results.csv")) {
            csvWriter.write(generateCsvReport());
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
        }
    }
}