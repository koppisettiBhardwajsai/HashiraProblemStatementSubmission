import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Solved {

    static class Point {
        final double x;
        final double y;
        Point(double x, double y) { this.x = x; this.y = y; }
        public String toString() { return "(" + x + ", " + y + ")"; }
    }

    public static void main(String[] args) throws Exception {
        String inputPath = "input.json";
        if (args.length > 0) inputPath = args[0];

        String json = new String(Files.readAllBytes(Paths.get(inputPath)), "UTF-8");

        
        int n = extractInt(json, "\"n\"\\s*:\\s*(\\d+)");
        int k = extractInt(json, "\"k\"\\s*:\\s*(\\d+)");

        // System.out.println("Parsed keys: n = " + n + ", k = " + k);

        
        
        Pattern entryPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{([^}]*)\\}");
        Matcher entryMatcher = entryPattern.matcher(json);

        List<Point> allPoints = new ArrayList<>();
        while (entryMatcher.find()) {
            String keyStr = entryMatcher.group(1);         
            String inner = entryMatcher.group(2);          
            int x = Integer.parseInt(keyStr);

            
            String baseStr = extractGroup(inner, "\"base\"\\s*:\\s*\"([^\"]+)\"");
            String valueStr = extractGroup(inner, "\"value\"\\s*:\\s*\"([^\"]+)\"");

            if (baseStr == null || valueStr == null) {
                System.err.println("Skipping key " + keyStr + " (missing base/value)");
                continue;
            }

            int base = Integer.parseInt(baseStr);
            
            BigInteger bigY;
            try {
                bigY = new BigInteger(valueStr, base);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse value '" + valueStr + "' in base " + base + " for key " + keyStr);
                throw e;
            }
            double y = bigY.doubleValue(); 

            allPoints.add(new Point(x, y));
        }

        if (allPoints.isEmpty()) {
            System.err.println("No numeric-key points found in JSON.");
            return;
        }

        
        allPoints.sort(Comparator.comparingDouble(p -> p.x));

        // System.out.println("Decoded points (all): " + allPoints);

        if (k < 1) {
            System.err.println("Invalid k (must be >= 1).");
            return;
        }
        if (allPoints.size() < k) {
            System.err.println("Not enough points (" + allPoints.size() + ") to pick k=" + k);
            return;
        }

        
        List<Point> pts = new ArrayList<>(allPoints.subList(0, k));
        // System.out.println("Selected points (first k sorted by x): " + pts);

        
        int m = k - 1;

        
        
        double[][] A = new double[k][k];
        double[] Y = new double[k];

        for (int i = 0; i < k; i++) {
            double x = pts.get(i).x;
            double pow = Math.pow(x, m);
            for (int j = 0; j < k; j++) {
                A[i][j] = pow;           
                pow = pow / x;           
            }
            
            if (pts.get(i).x == 0.0) {
                
                for (int j = 0; j < k - 1; j++) A[i][j] = 0.0;
                A[i][k - 1] = 1.0;
            }
            Y[i] = pts.get(i).y;
        }

        
        double[] coeffs = gaussianSolve(A, Y);

        
        
        
        
        
        

        double c = coeffs[coeffs.length - 1];
        
        long rounded = Math.round(c);
        if (Math.abs(c - rounded) < 1e-9) {
            System.out.println("\nThe value of C  = " + rounded);
        } else {
            System.out.println("\nThe value of C = " + c);
        }
    }

    
    private static String extractGroup(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    
    private static int extractInt(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));
        throw new IllegalArgumentException("Could not find integer for regex: " + regex);
    }

    private static double[] gaussianSolve(double[][] Aorig, double[] borig) {
        int n = borig.length;
        
        double[][] A = new double[n][n];
        double[] b = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(Aorig[i], 0, A[i], 0, n);
            b[i] = borig[i];
        }

        for (int col = 0; col < n; col++) {
            
            int pivotRow = col;
            double maxAbs = Math.abs(A[col][col]);
            for (int r = col + 1; r < n; r++) {
                double absVal = Math.abs(A[r][col]);
                if (absVal > maxAbs) {
                    maxAbs = absVal;
                    pivotRow = r;
                }
            }
            if (Math.abs(A[pivotRow][col]) < 1e-15) {
                throw new RuntimeException("Matrix is singular or nearly singular (pivot ~ 0).");
            }
            
            if (pivotRow != col) {
                double[] tmp = A[col]; 
                A[col] = A[pivotRow]; 
                A[pivotRow] = tmp;
                double tb = b[col]; 
                b[col] = b[pivotRow]; 
                b[pivotRow] = tb;
            }

            
            for (int r = col + 1; r < n; r++) {
                double factor = A[r][col] / A[col][col];
                
                for (int c = col; c < n; c++) {
                    A[r][c] -= factor * A[col][c];
                }
                b[r] -= factor * b[col];
            }
        }

        
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = b[i];
            for (int j = i + 1; j < n; j++) sum -= A[i][j] * x[j];
            x[i] = sum / A[i][i];
        }
        return x;
    }
}
