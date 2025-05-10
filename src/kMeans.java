import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class kMeans {

    public static Path dataFilePath;
    public static List<DataPoint> dataPoints;
    public static int kGroups = 3;
    public static List<Centroid> centroids;
    public static double E = Double.MAX_VALUE;

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select data file:");
        int option = fileChooser.showOpenDialog(null);
        if (option != JFileChooser.APPROVE_OPTION) {
            throw new RuntimeException("File was not selected properly.");
        }

        dataFilePath = fileChooser.getSelectedFile().toPath();

        System.out.println("Enter K groups:");
        kGroups = Integer.parseInt(scanner.nextLine());


        dataPoints = new ArrayList<>();
        centroids = new ArrayList<>();
        loadData();

        boolean succesfullyInitialized = false;

        while (!succesfullyInitialized) {
            initializeCentroids();
            clusterDataPoints();
            succesfullyInitialized = true;

            for (Centroid centroid : centroids) {
                if (centroid.dataPoints.isEmpty()) {
                    System.out.println("Centroid has no data points. REINIT");
                    centroids.clear();
                    succesfullyInitialized = false;
                    break;
                }
            }
        }

        boolean shouldRecalibrate = true;
        int iter = 0;
        while (shouldRecalibrate) {
            System.out.println("Iteration " + iter++);
            shouldRecalibrate = recalibrateCentroids();
            countE();
        }
        System.out.println();
        System.out.println("Finished after iters: " + iter);
        System.out.println("====== E: ");
        countE();
        System.out.println();

        for (Centroid centroid : centroids) {
            System.out.println("CENTROID: " + Arrays.toString(centroid.values) + " - SIZE: " + centroid.dataPoints.size());
            for (int j = 0; j < centroid.dataPoints.size(); j++) {
                System.out.println(Arrays.toString(centroid.dataPoints.get(j).getFeatures()) + " " + centroid.dataPoints.get(j).getLabel());
            }
            System.out.println();
        }
    }

    public static void countE() {
        E = 0;
        for (Centroid centroid : centroids) {
            double sum = 0;

            for (int i = 0; i < centroid.dataPoints.size(); i++) {
                sum += Math.pow(euclideanDistance(centroid.values, centroid.dataPoints.get(i).getFeatures()), 2);
            }
            System.out.println("For centroid: " + Arrays.toString(centroid.values) + " - E : " + sum);
            E += sum;
        }

        System.out.println("E : " + E);
    }


    public static boolean recalibrateCentroids() {
        List<Centroid> updatedCentroids = new LinkedList<>();

        for (Centroid centroid : centroids) {
            double[] values = new double[centroid.values.length];

            for (int i = 0; i < values.length; i++) {
                double dpValuesSum = 0;
                for (int j = 0; j < centroid.dataPoints.size(); j++) {
                    dpValuesSum += centroid.dataPoints.get(j).getFeatures()[i];
                }
                values[i] = dpValuesSum / centroid.dataPoints.size();
            }

            updatedCentroids.add(new Centroid(values));
        }

        for (int i = 0; i < updatedCentroids.size(); i++) {
            if (!Arrays.equals(updatedCentroids.get(i).values, centroids.get(i).values)) {
                centroids = updatedCentroids;
                clusterDataPoints();
                return true;
            }
        }

        return false;
    }


    public static void clusterDataPoints() {
        for (int i = 0; i < dataPoints.size(); i++) {
            TreeMap<Double, Centroid> distances = new TreeMap<>();
            for (int j = 0; j < centroids.size(); j++) {
                double dist = euclideanDistance(dataPoints.get(i).getFeatures(), centroids.get(j).values);
                distances.put(dist, centroids.get(j));
            }
            distances.firstEntry().getValue().dataPoints.add(dataPoints.get(i));
        }
    }

    public static void initializeCentroids() {
        for (int i = 0; i < kGroups; i++) {
            double[] values = new double[dataPoints.getFirst().getFeatures().length];

            for (int j = 0; j < values.length; j++) {
                int dpRange = (int) (Math.random() * dataPoints.size());
                values[j] = dataPoints.get(dpRange).getFeatures()[j];
            }
            centroids.add(new Centroid(values));
        }


        for (int i = 0; i < centroids.size(); i++) {
            System.out.println(Arrays.toString(centroids.get(i).values));
        }
    }


    public static void loadData() {
        try {
            String[] lines = Files.readAllLines(dataFilePath).toArray(new String[0]);

            for (String line : lines) {
                String[] fields = line.split(",");
                double[] features = new double[fields.length - 1];
                String label = fields[fields.length - 1];

                for (int i = 0; i < features.length; i++) {
                    features[i] = Double.parseDouble(fields[i]);
                }

                dataPoints.add(new DataPoint(features, label));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static double euclideanDistance(double[] v1, double[] v2) {
        double[] vResult = new double[v1.length];
        double distance = 0;

        if (v1.length != v2.length) throw new RuntimeException("Vectors are not the same size");

        for (int i = 0; i < v1.length; i++) {
            vResult[i] = Math.pow((v1[i] - v2[i]), 2);

            distance += vResult[i];
        }
        distance = Math.sqrt(distance);
        return distance;
    }

}



