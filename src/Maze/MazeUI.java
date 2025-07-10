package Maze;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MazeUI extends JFrame {
    private int rows = 50;
    private int cols = 50;
    private int cellSize = 15;
    private Maze maze;
    private MazePanel mazePanel;
    private Timer generationTimer;
    private Timer solveTimer;

    private MazeSolver currentSolver;
    private BenchmarkManager benchmarkManager;

    private JButton generateButton;
    private JButton solveSeqButton;
    private JButton solveParButton;
    private JButton saveResultsButton;
    private JButton batchTestButton;
    private JLabel statusLabel;

    public MazeUI() {
        setTitle("Maze Benchmark");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(Color.BLACK);

        benchmarkManager = new BenchmarkManager();

        maze = new Maze(rows, cols);
        mazePanel = new MazePanel();

        generateButton = new JButton("Gerar Labirinto");
        solveSeqButton = new JButton("A* Sequencial");
        solveParButton = new JButton("A* Paralelo");
        saveResultsButton = new JButton("Salvar Resultados");
        batchTestButton = new JButton("Executar Lote de Testes");
        statusLabel = new JLabel("", SwingConstants.CENTER);

        generateButton.addActionListener(e -> startMazeGeneration());
        solveSeqButton.addActionListener(e -> runSolver(new AStarSequentialSolver(), "A* Sequencial"));
        solveParButton.addActionListener(e -> {
            int threads = askForThreadCount();
            if (threads > 0) {
                runSolver(new AStarParallelSolver(threads), "A* Paralelo (" + threads + " threads)");
            }
        });
        batchTestButton.addActionListener(e -> runBatchTests());
        saveResultsButton.addActionListener(e -> {
            benchmarkManager.saveReportsToFile();
            JOptionPane.showMessageDialog(this, "Relatórios salvos!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        generationTimer = new Timer(0, e -> {
            if (maze.isDone()) {
                generationTimer.stop();
                updateButtonStates(State.MAZE_GENERATED);
            } else {
                maze.step();
            }
            mazePanel.repaint();
        });

        solveTimer = new Timer(0, e -> {
            if (currentSolver != null && !currentSolver.step(maze.getEnd())) {
                solveTimer.stop();
                updateButtonStates(State.SOLVED);
                mazePanel.repaint();
            }
            mazePanel.repaint();
        });

        setupLayout();
        updateButtonStates(State.INITIAL);

        setSize(800, 890);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void runBatchTests() {
        String numTestsStr = JOptionPane.showInputDialog(this, "Quantos labirintos deseja testar em lote?", "30");
        if (numTestsStr == null) return;
        int numTests;
        try {
            numTests = Integer.parseInt(numTestsStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um número válido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int threads = askForThreadCount();
        if (threads <= 0) return;

        String mazeSize = rows + "x" + cols;
        benchmarkManager.configureNewBatch(mazeSize, numTests, threads);

        updateButtonStates(State.SOLVING);

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                for (int i = 1; i <= numTests; i++) {
                    publish("Gerando e testando labirinto " + i + " de " + numTests + "...");

                    Maze testMaze = new Maze(rows, cols);
                    testMaze.generate();
                    benchmarkManager.nextMaze();

                    // Teste Sequencial
                    AStarSequentialSolver seqSolver = new AStarSequentialSolver();
                    long startTimeSeq = System.nanoTime();
                    seqSolver.solve(testMaze.getMaze(), testMaze.getStart(), testMaze.getEnd());
                    long endTimeSeq = System.nanoTime();
                    benchmarkManager.addResult("A* Sequencial", (endTimeSeq - startTimeSeq) / 1_000_000);

                    // Teste Paralelo
                    AStarParallelSolver parSolver = new AStarParallelSolver(threads);
                    long startTimePar = System.nanoTime();
                    parSolver.solve(testMaze.getMaze(), testMaze.getStart(), testMaze.getEnd());
                    long endTimePar = System.nanoTime();
                    benchmarkManager.addResult("A* Paralelo (" + threads + " threads)", (endTimePar - startTimePar) / 1_000_000);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                statusLabel.setText("Testes em lote concluídos! " + numTests + " labirintos testados.");
                updateButtonStates(State.SOLVED);
                JOptionPane.showMessageDialog(MazeUI.this, "Testes em lote finalizados! Clique em 'Salvar Resultados' para obter o arquivo.", "Concluído", JOptionPane.INFORMATION_MESSAGE);
            }
        }.execute();
    }

    private void runSolver(MazeSolver solver, String algorithmName) {
        this.currentSolver = solver;
        updateButtonStates(State.SOLVING);
        statusLabel.setText("Resolvendo com " + algorithmName + "...");

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() {
                long startTime = System.nanoTime();
                solver.solve(maze.getMaze(), maze.getStart(), maze.getEnd());
                long endTime = System.nanoTime();
                return (endTime - startTime) / 1_000_000;
            }

            @Override
            protected void done() {
                try {
                    long executionTimeMs = get();
                    statusLabel.setText(String.format("%s levou %d ms", algorithmName, executionTimeMs));
                    solver.initialize(maze.getMaze(), maze.getStart());
                    solveTimer.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Erro ao resolver.");
                }
            }
        }.execute();
    }

    private void styleButton(JButton button) {
        Color defaultColor = new Color(50, 50, 50);
        Color hoverColor = new Color(80, 80, 80);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultColor);
            }
        });
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5, 5, 5));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        styleButton(generateButton);
        styleButton(solveSeqButton);
        styleButton(solveParButton);
        styleButton(batchTestButton);
        styleButton(saveResultsButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(solveSeqButton);
        buttonPanel.add(solveParButton);
        buttonPanel.add(batchTestButton);
        buttonPanel.add(saveResultsButton);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(statusLabel, BorderLayout.NORTH);
        add(mazePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startMazeGeneration() {
        if(solveTimer.isRunning()) solveTimer.stop();
        mazePanel.clearSolution();
        maze.resetMaze();
        updateButtonStates(State.GENERATING);
        generationTimer.start();
    }

    private int askForThreadCount() {
        int cores = Runtime.getRuntime().availableProcessors();
        String threadsStr = JOptionPane.showInputDialog(this, "Quantas threads para o modo paralelo? (Recomendado: " + cores + ")", Integer.toString(cores));
        if (threadsStr == null) return 0;
        try {
            return Integer.parseInt(threadsStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Número de threads inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    private enum State { INITIAL, GENERATING, MAZE_GENERATED, SOLVING, SOLVED }

    private void updateButtonStates(State state) {
        boolean enableActions = (state == State.MAZE_GENERATED || state == State.SOLVED);
        boolean enableBatch = (state != State.GENERATING && state != State.SOLVING);
        generateButton.setEnabled(state != State.GENERATING && state != State.SOLVING);
        solveSeqButton.setEnabled(enableActions);
        solveParButton.setEnabled(enableActions);
        batchTestButton.setEnabled(enableBatch);
        saveResultsButton.setEnabled(state != State.INITIAL);
    }

    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            int[][] mazeData = maze.getMaze();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (mazeData[i][j] == 1) g.setColor(Color.WHITE);
                    else g.setColor(Color.BLACK);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
            if (currentSolver != null) {
                g.setColor(Color.GRAY);
                for (Point p : currentSolver.getPath()) g.fillRect(p.y * cellSize, p.x * cellSize, cellSize, cellSize);
                g.setColor(Color.RED);
                for (Point p : currentSolver.getSolutionPath()) g.fillRect(p.y * cellSize, p.x * cellSize, cellSize, cellSize);
            }
            g.setColor(Color.GREEN);
            g.fillRect(maze.getStart().y * cellSize, maze.getStart().x * cellSize, cellSize, cellSize);
            g.setColor(Color.RED);
            g.fillRect(maze.getEnd().y * cellSize, maze.getEnd().x * cellSize, cellSize, cellSize);
        }

        public void clearSolution(){
            if(currentSolver != null){
                currentSolver.getPath().clear();
                currentSolver.getSolutionPath().clear();
            }
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeUI::new);
    }
}