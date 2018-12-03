package uppgift3.model;

/* *****************************************************************************************************************
 * Denna klass r�knar ut korrellationskoefficienten d� anv�ndaren valt att visa tv� stycken aktier samtidigt.
 * 
 * Det gick helt klart mest tid till att koda denna klass, f�r oavset vilken formel jag anv�nder (jag testade fyra eller fem olika)
 * och hur jag strukturerar algoritmen, och vilka talrepresentationer (double, float, BigDecimal) och vilken precision
 * (fr�n tre �nda upp till trettio decimaler) jag anv�nder s� f�r jag inte resultatet att st�mma �verens med det som tv� olika 
 * n�tsidor ger som resultat. Jag har ocks� f�rs�kt ta bort outliers med tv� olika algoritmer, men det hj�lper inte heller...
 * 
 * De tv� sidorna jag checkar med ger f�rvisso inte likada resultat sinsemellan heller,trots att jag s�tter in exakt samma 
 * tidsintervall i dem, s� kanske det �r skillnader i hur olika tj�nster definierar t.ex. "adjusted close"?
 */

import javafx.scene.chart.XYChart.Series;
import uppgift3.view.ErrorMessage;

import java.util.ArrayList;
import java.util.Iterator;

public class PearsonCalculator {

	public static String calculate(JDataReader jReader1, JDataReader jReader2) throws Exception {
		// Vi j�mf�r aktiernas v�rde vid slutet av b�rsdagarna
		Series<String, Number> series1 = jReader1.getXYSeries(jReader1.getSeriesName(". close"));
		Series<String, Number> series2 = jReader2.getXYSeries(jReader2.getSeriesName(". close"));

		// Kolla att tidsserierna �r j�mf�rbara. Om de inte �r det s� returneras info om varf�r de inte g�r att j�mf�ra.
		String checkSeries = isComparable(series1, series2);
		if (!checkSeries.equals("OK")) {
			return checkSeries;
		}

		ArrayList<Double> valuesX = jReader1.getDoubleArray();
		ArrayList<Double> valuesY = jReader2.getDoubleArray();

		// Utr�kningen b�rjar h�r.
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
		
		// Utr�kningen �r klar och v�rdet returneras till den anropande metoden.
		return String.valueOf(corrCoef);
	}


	private static ArrayList<Double> deviationsArray(ArrayList<Double> values, double mean) {
		// R�kna ut hur mycket varje punkt avviker fr�n seriens medelv�rde
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
		// Multiplicera de v�rden som tidsm�ssigt motsvarar varandra i tv� olika serier
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
		// Denna metod kontrollerar att tv� tidsserier g�r att j�mf�ra med varandra
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
