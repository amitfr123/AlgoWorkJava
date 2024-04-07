import java.util.*;

public class SlidingPuzzle {
    public static class BoardState {
        private int [][] matrix;
        private int size;
        private int z_row;
        private int z_col;

        BoardState(int size) {
            matrix = new int[size][size];
            this.size = size;

            for (int i = 0; i < size*size - 1; i++) {
                matrix[i/size][i%size] = i + 1;
            }
            matrix[size - 1][size - 1] = 0;
            z_row = size - 1;
            z_col = size - 1;
        }

        BoardState(BoardState cp) {
            size = cp.size;
            matrix = new int[size][size];

            for (int row = 0; row < size; row++) {
                for(int col = 0; col < size; col++) { 
                    matrix[row][col] = cp.matrix[row][col];
                }    
            }
            
            z_row = cp.z_row;
            z_col = cp.z_col;
        }

        public void moveZRow(boolean up) {
            moveZero(((up)? (z_row + 1): (z_row - 1)), z_col);
        }

        public void moveZCol(boolean right) {
            moveZero(z_row, ((right)? (z_col + 1): (z_col - 1)));
        }

        public int getSize() {
            return size;
        }

        public int [][] getMatrix() {
            return matrix;
        }

        private void moveZero(int new_row, int new_col) throws ArrayIndexOutOfBoundsException {
            matrix[z_row][z_col] = matrix[new_row][new_col];
            matrix[new_row][new_col] = 0;
            z_row = new_row;
            z_col = new_col;
        }

        private void shuffle(int moves)
        {
            Random rnd = new Random();
            int i = 0;
            while (i < moves) {
                try {
                    switch (rnd.nextInt(0, 4)) {
                        case 0:
                            moveZRow(true);
                            break;
                        case 1:
                            moveZRow(false);
                            break;
                        case 2:
                            moveZCol(true);
                            break;
                        case 3:
                            moveZCol(false);
                            break;
                        default:
                            break;
                    }
                    i++;    
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }

        public void initFromString(String str) {
            String[] parts = str.split(",");

            for (int i = 0; i < size * size; i++) {
                int num = Integer.parseInt(parts[i]);
                if (num > size*size - 1 || num < 0) {
                    throw new IllegalArgumentException("number must be between zero and size -1");
                }

                for (int j = i + 1; j < size * size; j++) {
                    if (matrix[j/size][j%size] == num) {
                        throw new IllegalArgumentException("numbers may not repeat");
                    }
                }

                matrix[i/size][i%size] = num;
                if (num == 0) {
                    z_row = i/size;
                    z_col = i%size;
                }
            }
        }

        public void initToNegative() {
            for (int row = 0; row < size; row++) {
                for(int col = 0; col < size; col++) { 
                    matrix[row][col] = -1;
                }    
            }
        }

        public void printBoard() {
            Arrays.stream(matrix).forEach((row) -> {
            System.out.print("[");
            Arrays.stream(row).forEach((el) -> System.out.print("\t" + el + "\t"));
            System.out.println("]");
            });
        }

        public List<BoardState> getNeighbors() {
            List<BoardState> n_list = new LinkedList<BoardState>();
            if (z_row < size - 1) {
                BoardState new_bs = new BoardState(this);
                new_bs.moveZRow(true);
                n_list.add(new_bs);
            }

            if (z_row > 0) {
                BoardState new_bs = new BoardState(this);
                new_bs.moveZRow(false);
                n_list.add(new_bs);
            }

            if (z_col < size - 1) {
                BoardState new_bs = new BoardState(this);
                new_bs.moveZCol(true);
                n_list.add(new_bs);
            }
    
            if (z_col > 0) {
                BoardState new_bs = new BoardState(this);
                new_bs.moveZCol(false);
                n_list.add(new_bs);
            }
    
            return n_list;
        }

        public boolean solved() {
            for (int i = 0; i < size*size - 1; i++) {
                if (matrix[i/size][i%size] != i + 1) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            BoardState that = (BoardState) o;
            return Arrays.deepEquals(that.matrix, this.matrix);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(matrix);
        }
    }

    public static class PuzzlePathNode {
        protected BoardState self;
        protected PuzzlePathNode parent;

        PuzzlePathNode(BoardState self, PuzzlePathNode parent) {
            this.self = self;
            this.parent = parent;
        }

        public BoardState getSelf() {
            return self;
        }

        public PuzzlePathNode getParent() {
            return parent;
        }

        public int printPath() {
            int i = 0;

            if (parent != null) {
                i = parent.printPath();
            }

            System.out.println("Node number:" + i);
            self.printBoard();
            return i+1;
        }

        public int pathSize() {
            int i = 0;
            PuzzlePathNode tempPNode = parent;
            while (tempPNode != null) {
                i++;
                tempPNode = tempPNode.parent;
            }
            return i;
        }
    }

    public static class SolveResult {
        public PuzzlePathNode path;
        public int numOfNodes;
        SolveResult(PuzzlePathNode tree, int numOfNodes) {
            this.path = tree;
            this.numOfNodes = numOfNodes;
        }
    }

    public interface PuzzleSolver {
        SolveResult solve(BoardState start);
        String getName();
    }

    static public class BFSPuzzleSolver implements PuzzleSolver {
        public SolveResult solve(BoardState start) {
            Queue<PuzzlePathNode> q = new LinkedList<PuzzlePathNode>();
            HashSet<BoardState> known = new HashSet<BoardState>();
            int count = 0;
            q.add(new PuzzlePathNode(start, null));
            known.add(start);
    
            while (!q.isEmpty()) {
                count++;

                PuzzlePathNode node = q.remove();
                BoardState bs = node.getSelf();
    
                if (bs.solved()) {
                    return new SolveResult(node, count);
                }
    
                List<BoardState> nList = bs.getNeighbors();
                for (BoardState neighbor : nList) {
                    if (!known.contains(neighbor)) {
                        known.add(neighbor);
                        q.add(new PuzzlePathNode(neighbor, node));
                    }
                }
            }
            return null;
        }

        public String getName() {
            return "BFS";
        }
    }

    public interface HeuristicFunction {
        int calc(BoardState bs);
        String getName();
    }

    static public class ZeroFunction implements HeuristicFunction {
        public int calc(BoardState bs) {
            return 0;
        }

        public String getName() {
            return "Zero function";
        }
    }

    static public class ManhattanFunction implements HeuristicFunction {
        public int calc(BoardState bs) {
            int distance = 0;
            int size = bs.getSize();
            int[][] matrix = bs.getMatrix();

            for (int i = 0; i < size * size; i++) {
                int cur_row = i / size;
                int cur_col = i % size;

                int cur_val = matrix[cur_row][cur_col];

                if (cur_val != 0) {
                    int expected_row = (cur_val - 1) / size;
                    int expected_col = (cur_val - 1) % size;
                    distance += Math.abs(cur_row - expected_row) + Math.abs(cur_col - expected_col);
                }
            }
            return distance;
        }

        public String getName() {
            return "Manhattan function";
        }
    }

    static public class InvalidHeuristicFunction extends ManhattanFunction {
        static int extra = 50;
        public int calc(BoardState bs) {
            int distance = 10 + super.calc(bs);
            extra *= -1;
            return distance * distance + extra;
        }

        public String getName() {
            return "Invalid Heuristic function";
        }
    }

    static public class AStarNode extends PuzzlePathNode implements Comparable<AStarNode> {
        private int gScore;
        private int hScore;

        AStarNode(BoardState self, AStarNode parent, int gScore, int hScore) {
            super(self, parent);
            this.gScore = gScore;
            this.hScore = hScore;
        }

        public void setParent(AStarNode parent, int gScore) {
            this.parent = parent;
            this.gScore = gScore;
        }

        public int getGScore() {
            return gScore;
        }
        
        public int compareTo(AStarNode cmp) {
            return getFScore() - cmp.getFScore();
        }

        public int getFScore() {
            return gScore + hScore;
        }
    }

    static public class AStarPuzzleSolver implements PuzzleSolver {
        protected HeuristicFunction func;
        AStarPuzzleSolver(HeuristicFunction func) {
            this.func = func;
        }

        public SolveResult solve(BoardState start) {
            Queue<AStarNode> q = new PriorityQueue<AStarNode>();
            HashMap<BoardState, Integer> known = new HashMap<BoardState, Integer>();
            int count = 0;
            AStarNode startNode = new AStarNode(start, null, 0, func.calc(start));

            q.add(startNode);
            known.put(start, startNode.getGScore());
    
            while (!q.isEmpty()) {
                count++;

                AStarNode node = q.remove();
                BoardState bs = node.getSelf();
    
                if (bs.solved()) {
                    return new SolveResult(node, count);
                }
    
                List<BoardState> nList = bs.getNeighbors();
                for (BoardState neighbor : nList) {
                    if (known.containsKey(neighbor)) {
                        if (node.getGScore() + 1 < known.get(neighbor)) {
                            AStarNode nNode = new AStarNode(neighbor, node, node.getGScore() + 1, func.calc(neighbor));
                            known.replace(neighbor, nNode.getGScore());
                            q.add(nNode);
                        }
                    } else {
                        AStarNode nNode = new AStarNode(neighbor, node, node.getGScore() + 1, func.calc(neighbor));
                        known.put(neighbor, nNode.getGScore());
                        q.add(nNode);
                    }
                }
            }
            return null;
        }

        public String getName() {
            return "AStar-"+func.getName();
        }
    }

    
    public static void main(String[] args) {
        boolean quit = false;

        Scanner sc = new Scanner(System.in);
        printTheMenu();

        while (!quit) {
            String str = sc.nextLine();
            switch (str) {
                case "1":
                    singleRun(true);
                    break;
                case "2":
                    singleRun(false);
                    break;
                case "3":
                    autoTestAvg(4, 5);
                    break;
                case "4":
                    autoTestAvg(5, 5);
                    break;
                case "5":
                    autoTestAvg(4,50);
                    break;
                case "6":
                    autoTestAvg(5,50);
                    break;
                case "q":
                    quit = true;
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
            printTheMenu();    
        }
    }


    public static void printTheMenu() {
        System.out.println("Enter a command, the options are:");  
        System.out.println("1) create board by hand and choose algorithm to solve the puzzle with");
        System.out.println("2) create board randomly (using 10 moves) and choose algorithm to solve the puzzle with");
        System.out.println("3) automatic test for puzzle with 15 pieces (5 runs using 10 moves)");
        System.out.println("4) automatic test for puzzle with 24 pieces (5 runs using 10 moves)");
        System.out.println("5) automatic test for puzzle with 15 pieces (avg of 50 runs using 10 moves)");
        System.out.println("6) automatic test for puzzle with 24 pieces (avg of 50 runs using 10 moves)");
        System.out.println("q) to quit");
    }

    public static void singleRun(boolean manual) {
        System.out.println("Enter a size for the board (15/24)");
        Scanner sc = new Scanner(System.in);
        int size = sc.nextInt();

        if (size != 15 && size != 24) {
            System.out.println("Error: Invalid size, returning to main menu");
            return;
        } else if (size == 15) {
            size = 4;
        } else {
            size = 5;
        }

        BoardState bs = new BoardState(size);
        if (manual) {
            System.out.println("Solved board for reference:");  
            bs.printBoard();

            bs.initToNegative();
            System.out.println("Enter a string containing all the numbers from 0-" + size*size + " with ',' between them");
            System.out.println("For example: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0");
            String str = sc.next();

            try {
                bs.initFromString(str);    
            } catch (Exception e) {
                System.out.println("Error: Invalid init string, returning to main menu");
                return;
            }

            System.out.println("Resulting board");
            bs.printBoard();
        } else {
            System.out.println("Board before shuffle");  
            bs.printBoard();
            bs.shuffle(10);
            System.out.println("Board after shuffle");  
            bs.printBoard();
        }

        System.out.println("Choose algorithm from the following options: BFS, DIJKSTRA, A_STAR_MANHATTAN, A_STAR_INVALID_HEURISTIC");  
        PuzzleSolver solver = null;
        String algoString = sc.next();

        switch (algoString) {
            case "BFS":
                solver = new BFSPuzzleSolver();
                break;
            case "DIJKSTRA":
                solver = new AStarPuzzleSolver(new ZeroFunction());
                break;
            case "A_STAR_MANHATTAN":
                solver = new AStarPuzzleSolver(new ManhattanFunction());
                break;
            case "A_STAR_INVALID_HEURISTIC":
                solver = new AStarPuzzleSolver(new InvalidHeuristicFunction());
                break;
            default:
                System.out.println("Error: Invalid algo string, returning to main menu");
                return;
        }

        long startTime = System.nanoTime();
        SolveResult result = solver.solve(bs);
        double elapsedTime = (System.nanoTime() - startTime) / 1000000.0;
        
        System.out.println("The solution is:");
        result.path.printPath();
        System.out.println("Total steps: " + result.path.pathSize() + ", Total developed nodes: " + result.numOfNodes + ", Total MS:" + elapsedTime);
        System.out.println("press enter to return to main menu");
        sc.next();
    }

    public static void autoTestAvg(int size, int numberOfTests) {
        PuzzleSolver[] solvers = {
            new BFSPuzzleSolver(), 
            new AStarPuzzleSolver(new ZeroFunction()), 
            new AStarPuzzleSolver(new ManhattanFunction()),
            new AStarPuzzleSolver(new InvalidHeuristicFunction())
        };

        class AutoTestResult {
            public double time;
            public double developed;
            public double pathSize;
            AutoTestResult() {
                time = 0;
                developed = 0;
                pathSize = 0;
            }
        }

        AutoTestResult [] results = {new AutoTestResult(), new AutoTestResult(), new AutoTestResult(), new AutoTestResult()};

        for (int i = 0; i < numberOfTests; i++) {
            BoardState bs = new BoardState(size);
            bs.shuffle(10);
            for (int j = 0; j < solvers.length; j++) {
                long startTime = System.nanoTime();
                SolveResult result = solvers[j].solve(bs);
                results[j].time += (System.nanoTime() - startTime) / 1000000.0;
                results[j].developed += result.numOfNodes;
                results[j].pathSize += result.path.pathSize();
            }   
        }

        System.out.println("Average results for " + numberOfTests + " tests:");
        for (int j = 0; j < solvers.length; j++) {
            System.out.println(solvers[j].getName() + ": avg time MS: " + results[j].time / numberOfTests + ", avg developed points:" + results[j].developed / numberOfTests + ", avg result path size:" + results[j].pathSize / numberOfTests);
        }

        System.out.println("press enter to return to main menu");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
    }
}