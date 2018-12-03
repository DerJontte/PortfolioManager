package uppgift3.model;

/* *****************************************************************************************************************
 * Denna klass räknar ut korrellationskoefficienten då användaren valt att visa två stycken aktier samtidigt.
 * 
 * Det gick helt klart mest tid till att koda denna klass, för oavset vilken formel jag använder (jag testade fyra eller fem olika)
 * och hur jag strukturerar algoritmen, och vilka talrepresentationer (double, float, BigDecimal) och vilken precision
 * (från tre ända upp till trettio decimaler) jag använder så får jag inte resultatet att stämma överens med det som två olika 
 * nätsidor ger som resultat. Jag har också försökt ta bort outliers med två olika algoritmer, men det hjälper inte heller...
 * 
 * De två sidorna jag checkar med ger förvisso inte likada resultat sinsemellan heller,trots att jag sätter in exakt samma 
 * tidsintervall i dem, så kanske det är skillnader i hur olika tjänster definierar t.ex. "adjusted close"?
 */

import javafx.scene.chart.XYChart.Series;
import uppgift3.view.ErrorMessage;

import java.util.ArrayList;
import java.util.Iterator;

public class PearsonCalculator {

	public static String calculate(JDataReader jReader1, JDataReader jReader2) throws Exception {
		// Vi jämför aktiernas värde vid slutet av börsdagarna
		Series<String, Number> series1 = jReader1.getXYSeries(jReader1.getSeriesName(". close"));
		Series<String, Number> series2 = jReader2.getXYSeries(jReader2.getSeriesName(". close"));

		// Kolla att tidsserierna är jämförbara. Om de inte är det så returneras info om varför de inte går att jämföra.
		String checkSeries = isComparable(series1, series2);
		if (!checkSeries.equals("OK")) {
			return checkSeries;
		}

		ArrayList<Double> valuesX = jReader1.getDoubleArray();
		ArrayList<Double> valuesY = jReader2.getDoubleArray();

		// Uträkningen börjar här.
		double meanX = avgOf(valuesX);
		double meanY = avgOf(valuesY);

		ArrayList<Double> cvFactor1 = deviationsArray(valuesX, meanX);
		ArrayList<Double> cvFactor2 = deviationsArray(valuesY, meanY);
		ArrayList<Double> cvValues = multiply(cvFactor1, cvFactor2);
		int n = cvValues.size();
		double cvNumerator = sumOf(cvValues);
		double covariance = cvNumerator / ( n - 1);

		double varianceX = sumOf(multiply(cvFactor1, cvFactor1)) / (n - 1);
		double varianceY = sumOf(multiply(cvFactor2, cvFactor2)) / (n - 1);

		double stDevX = Math.sqrt(varianceX);
		double stDevY = Math.sqrt(varianceY);
		
		double corrCoef = RoundFix.setDecimals(14, (covariance / (stDevX * stDevY)));
		
		// Uträkningen är klar och värdet returneras till den anropande metoden.
		return String.valueOf(corrCoef);
	}


	private static ArrayList<Double> deviationsArray(ArrayList<Double> values, double mean) {
		// Räkna ut hur mycket varje punkt avviker från seriens medelvärde
		ArrayList<Double> toReturn = new ArrayList<Double>();
		values.stream().forEach(value -> toReturn.add(value-mean));
		return toReturn;
	}


	private static double sumOf(ArrayList<Double> values) {
		// Addera alla tal i serien och returnera summan
		return values.stream().mapToDouble(value -> value).sum();
	}

	private static double avgOf(ArrayList<Double> values) {
		// Ta medeltalet av alla tal i serien
		return values.stream().mapToDouble(value -> value).sum() / values.size();
	}


	private static ArrayList<Double> multiply(ArrayList<Double> valuesX, ArrayList<Double> valuesY) {
		// Multiplicera de värden som tidsmässigt motsvarar varandra i två olika serier
		if (valuesX.size() != valuesY.size()) {
			new ErrorMessage("Error: can only multiply two vectors of equal size.");
			return null;
		}
		ArrayList<Double> toReturn = new ArrayList<Double>();
		Iterator<Double> iterX = valuesX.iterator();
		Iterator<Double> iterY = valuesY.iterator();
		while(iterX.hasNext() && iterY.hasNext()) {
			toReturn.add(iterX.next() * iterY.next());
		}
		return toReturn;
	}

	private static String isComparable(Series<?,?> A, Series<?,?> B) {
		// Denna metod kontrollerar att två tidsserier går att jämföra med varandra
		try {
			if (A.getData().size() != B.getData().size()) {
				return "Time series are of unequal size. Choose a different time interval.";
			}
			if (!A.getData().get(0).getXValue().equals(B.getData().get(0).getXValue())) {
				return "Time series do not start at the same date. Choose another starting point.";
			}
		} catch (IndexOutOfBoundsException e) {
			return "Empty time series. Choose a different time interval.";
		}
		return "OK";
	}
}
