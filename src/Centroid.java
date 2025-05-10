import java.util.ArrayList;
import java.util.List;

public class Centroid {

    double[] values;
    List<DataPoint> dataPoints;

    public Centroid(double[] values) {
        this.values = values;
        this.dataPoints = new ArrayList<>();
    }


}
