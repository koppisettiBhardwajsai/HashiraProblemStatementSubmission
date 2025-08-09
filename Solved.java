import java.io.IOException;
import java.nio.file.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class Solved {

    
    static class Rational {
        final BigInteger num;
        final BigInteger den; 

        Rational(BigInteger n, BigInteger d) {
            if (d.signum() == 0) throw new ArithmeticException("Zero denominator");
            if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
            BigInteger g = n.gcd(d);
            if (!g.equals(BigInteger.ONE)) { n = n.divide(g); d = d.divide(g); }
            this.num = n;
            this.den = d;
        }
        
        static Rational of(BigInteger n) { return new Rational(n, BigInteger.ONE); }
        static Rational zero() { return new Rational(BigInteger.ZERO, BigInteger.ONE); }

        Rational add(Rational o) {
            BigInteger n = this.num.multiply(o.den).add(o.num.multiply(this.den));
            BigInteger d = this.den.multiply(o.den);
            return new Rational(n, d);
        }
        Rational multiply(Rational o) {
            BigInteger n = this.num.multiply(o.num);
            BigInteger d = this.den.multiply(o.den);
            return new Rational(n, d);
        }
        Rational divide(Rational o) {
            if (o.num.equals(BigInteger.ZERO)) throw new ArithmeticException("Divide by zero");
            BigInteger n = this.num.multiply(o.den);
            BigInteger d = this.den.multiply(o.num);
            return new Rational(n, d);
        }
        @Override
        public String toString() {
            if (den.equals(BigInteger.ONE)) return num.toString();
            return num.toString() + "/" + den.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "input.json";
        String json = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");

        
        int n = extractInt(json, "\"n\"\\s*:\\s*(\\d+)");
        int k = extractInt(json, "\"k\"\\s*:\\s*(\\d+)");

        
        Pattern entryPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{([^}]*)\\}");
        Matcher entryMatcher = entryPattern.matcher(json);

        TreeMap<Integer, BigInteger> points = new TreeMap<>(); 

        while (entryMatcher.find()) {
            String keyStr = entryMatcher.group(1);
            String inner = entryMatcher.group(2);
            int x = Integer.parseInt(keyStr);

            String baseStr = extractGroup(inner, "\"base\"\\s*:\\s*\"([^\"]+)\"");
            String valueStr = extractGroup(inner, "\"value\"\\s*:\\s*\"([^\"]+)\"");

            if (baseStr == null || valueStr == null) continue;

            int base = Integer.parseInt(baseStr);
            BigInteger y;
            try {
                y = new BigInteger(valueStr, base);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse value '" + valueStr + "' in base " + base + " for key " + keyStr);
                throw e;
            }
            points.put(x, y);
        }

        if (points.size() < k) {
            System.err.println("Not enough points (" + points.size() + ") to pick k=" + k);
            return;
        }

        
        List<Integer> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Integer, BigInteger> e : points.entrySet()) {
            xs.add(e.getKey());
            ys.add(e.getValue());
            count++;
            if (count == k) break;
        }

        
        
        Rational secret = Rational.zero();
        for (int i = 0; i < k; i++) {
            Rational term = Rational.of(ys.get(i));
            for (int j = 0; j < k; j++) {
                if (j == i) continue;
                BigInteger numer = BigInteger.valueOf(-xs.get(j));       
                BigInteger denom = BigInteger.valueOf(xs.get(i) - xs.get(j)); 
                term = term.multiply(new Rational(numer, denom));
            }
            secret = secret.add(term);
        }

        
        if (secret.den.equals(BigInteger.ONE)) {
            System.out.println("Secret (c) = " + secret.num.toString());
        } else {
            System.out.println("Secret (c) (rational) = " + secret.toString());
            
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
}
