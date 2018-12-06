package uppgift3.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.util.Pair;
import uppgift3.org.json.JSONArray;
import uppgift3.org.json.JSONObject;
import uppgift3.view.ErrorMessage;
import uppgift3.view.GUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class JDataReader implements Iterable<Pair<String, JSONObject>> {
	private ArrayList<Pair<String, JSONObject>> dataPoints = new ArrayList<Pair<String, JSONObject>>();
	private StringProperty stockSymbol = new SimpleStringProperty();
	private StringProperty startDate = new SimpleStringProperty("0");
	private StringProperty endDate = new SimpleStringProperty("9");
	private String timeSeriesName;
	public boolean isIntraDay;
	private static String timeInterval;
	private static String outputSize;

	public JDataReader(String inDataString) throws Exception {
		// D� klientkoden skapar ett objekt s� ges en akties JSON-data (i Stringform) som argument.
		// Aktiev�rdedatat parsas till en l�ttare anv�ndbar form, och en del anv�ndbar information
		// extraheras �ven fr�n metadatan.
		JSONObject inDataObject = new JSONObject(inDataString);
		JSONArray topLevelObjects = inDataObject.names();
		JSONObject metaDataObject;

		GUI.stockGUI.symbolObserver.bind(stockSymbol);

		GUI.stockGUI.enableDatePickers.selectedProperty().addListener(listener -> {
			// N�r datumv�ljarna �r aktiverade binds variablerna f�r start- och slutdatum i denna klass till datumv�ljarnas
			// v�rden, s� uppdateras intervallet automatiskt. N�r datumv�ljarna �r inaktiverade s�tts denna klass
			// datumvariabler till s�dana v�rden som �r lexigrafiskt mindre och st�rre �n n�got datum som kan f�rekomma i datan.
			if (GUI.stockGUI.enableDatePickers.isSelected()) {
				startDate.bind(GUI.stockGUI.startDatePicker.valueProperty().asString());
				endDate.bind(GUI.stockGUI.endDatePicker.valueProperty().asString());
			} else {
				startDate.unbind();
				endDate.unbind();
				startDate.setValue("0");
				endDate.setValue("9");
			}
		});

		// Om n�got g�tt fel med datah�mtningen visas ett felmeddelande.
		if (topLevelObjects.toString().contains("Error Message")) {
			new ErrorMessage("The server returned an error. Try again later.");
			return;
		}

		// Dela upp JSON-datan i metadata och egentlig data och spara dem i egna objekt
		// Ordningen vara kan hur som helst, s� vi checkar f�rst vilken del av datan som �r vilken.
		try {
			if (topLevelObjects.get(0).equals("Meta Data")) {
				metaDataObject = inDataObject.getJSONObject(topLevelObjects.get(0).toString());
				inDataObject = inDataObject.getJSONObject(topLevelObjects.get(1).toString());
			} else {
				metaDataObject = inDataObject.getJSONObject(topLevelObjects.get(1).toString());
				inDataObject = inDataObject.getJSONObject(topLevelObjects.get(0).toString());
			}

			// Den boolska variabeln isIntraDay anv�nds f�r att v�lja r�tt text i listan med v�rden l�ngre fram i programmet
			if(metaDataObject.toString().contains("Intraday")) {
				isIntraDay = true;
			} else {
				isIntraDay = false;
			}

			stockSymbol.setValue(metaDataObject.getString("2. Symbol"));

		} catch (Exception e) {
			new Thread(() -> {
				new ErrorMessage("Error while parsing metadata: " + e.getMessage());
				return;
			});
		}

		// Sortera datan i tidsordning
		try {
			ArrayList<String> timeStamps = new ArrayList<String>(inDataObject.keySet());
			timeStamps.sort(Collections.reverseOrder());
			for(String timeStamp: timeStamps) {
				JSONObject timeObject = inDataObject.getJSONObject(timeStamp);
				dataPoints.add(new Pair<String, JSONObject>(timeStamp, timeObject));			
			}
		} catch (Exception e) {
			new Thread(() -> {
				new ErrorMessage("Error while sorting data: " + e.getMessage());
			});
		}
	}

	public String getSeriesName(String query) throws Exception {
		// S�k upp (om det finns) en dataserie som inneh�ller en given teckenstr�ng. Anv�nds f�r att hitta close-serien
		// d� korrelationskoefficienten f�r tv� aktier r�knas ut.
		for (String s: getSeriesNames()) {
			if (s.contains(query)) {
				return s;
			}
		}
		return null;
	}

	public ObservableList<String> getSeriesNames() {
		// H�mta namnen p� de dataserier som finns i datan. Anv�nds till att populera dataSeries-rullgardinen.
		if (dataPoints.size() == 0) {
			return null;
		}
		ObservableList<String> toReturn = FXCollections.observableArrayList(JSONObject.getNames(dataPoints.get(0).getValue()));
		toReturn.sort(null);
		return toReturn;
	}

	public String getStockName() {
		return stockSymbol.getValue();
	}

	public Series<String, Number> getXYSeries(String dataSeriesName) {
		// Formatera och returnera XY-serien som diagrammet anv�nder
		Series<String, Number> series = new Series<String, Number>();
		forEach(dataPoint -> {
			if (startDate.get().compareTo(dataPoint.getKey()) <= 0 && (endDate.get().compareTo(dataPoint.getKey()) >= 0)) {
				double value = RoundFix.setDecimals(3, dataPoint.getValue().getDouble(dataSeriesName));
				series.getData().add(0, new XYChart.Data<String, Number>(dataPoint.getKey(), value));
			}
		});
		return series;	
	}
	
	public ArrayList<Double> getDoubleArray() throws Exception {
		// Returnera en r�cka med aktiekurser i double-format.
		String series = getSeriesName(". close");
		ArrayList<Double> toReturn = new ArrayList<Double>();
		forEach(dataPoint -> {
			if (startDate.get().compareTo(dataPoint.getKey()) <= 0 && (endDate.get().compareTo(dataPoint.getKey()) >= 0)) {
				toReturn.add(RoundFix.setDecimals(3, dataPoint.getValue().getDouble(series)));
			}
		});
		return toReturn;
	}

	// Returnera det nyaste v�rdet i den inladdade datan
	public double getLatestValue() throws Exception {
		int i = dataPoints.size();
		if(i > 0) {
			return dataPoints.get(i - 1).getValue().getDouble(getSeriesName(". close"));
		} else {
			return 0.0;
		}
	}

	public String getText() {
		// Convenience-metod f�r fall d� bara en aktie �r vald
		return getTextComparedTo(null);
	}

	public String getTextComparedTo(JDataReader secondary) {
		// H�r formatteras aktiernas v�rdeinformation till en lista i String-format
		String series = GUI.stockGUI.dataSeries.getValue().toString();
		final AtomicReference<String> toReturn = new AtomicReference<String>("===== Showing data for " + series + " =====\n");
		@SuppressWarnings("unused")
		String dateTimeLabel = new String(); 

		if(isIntraDay) {
			dateTimeLabel = "Time";
		} else {
			dateTimeLabel = "Date";
		}

		if (secondary == null) {
			dataPoints.forEach(currentPoint -> {
				if (startDate.get().compareTo(currentPoint.getKey()) <= 0 && (endDate.get().compareTo(currentPoint.getKey()) >= 0)) {
					String nextLine = currentPoint.getKey() + "\t\t" + getStockName() + " " + RoundFix.setDecimals(3, currentPoint.getValue().getDouble(series)) + "\n";
					toReturn.set(toReturn.get().concat(nextLine));
				}
			});
		} else {
			ArrayList<Pair<String, JSONObject>> primTemp;
			ArrayList<Pair<String, JSONObject>> secTemp;
			String primName;
			String secName;
			int loops;

			if (dataPoints.size() >= secondary.dataPoints.size()) {
				primTemp = dataPoints;
				primName = getStockName();
				secTemp = secondary.dataPoints;
				secName = secondary.getStockName();
			} else {
				primTemp = secondary.dataPoints;
				primName = secondary.getStockName();
				secTemp = dataPoints;
				secName = getStockName();
			}
			loops = primTemp.size() - 1;

			// TODO: skriv om f�ljande block till n�t effektivare �n N^2!
			for (int i = 0; i <= loops; i++) {
				String nextKey = primTemp.get(i).getKey();
				if (startDate.get().compareTo(nextKey) <= 0 && (endDate.get().compareTo(nextKey) >= 0)) {
					String nextValue1 = primName + " " + RoundFix.setDecimals(3, primTemp.get(i).getValue().getDouble(series));
					String nextValue2 = "";
					for (Pair<String, JSONObject> curr: secTemp) {
						if (curr.getKey().equals(nextKey)) {
							nextValue2 = secName + " " + RoundFix.setDecimals(3, curr.getValue().getDouble(series));
							break;
						}
					}
					String nextLine = nextKey + "\t\t" + nextValue1 + padding(nextValue1, 30) + "\t\t" + nextValue2 + "\n";
					toReturn.set(toReturn.get().concat(nextLine));
				}
			}
		}
		return toReturn.get();
	}

	String padding(String text, int amount) {
		String toReturn = new String("");
		int textLen = text.length();
		if (textLen < amount) {
			for (int i = 0; i < (amount - textLen); i++) {
				toReturn = toReturn.concat(" ");
			}
		}
		return toReturn;
	}
	
	@Override
	public Iterator<Pair<String, JSONObject>> iterator() {
		// En iterator �ver datan i objektet
		Iterator<Pair<String, JSONObject>> iter = new Iterator<Pair<String, JSONObject>>() {
			private int size = dataPoints.size();
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < size && dataPoints.get(index) != null;
			}

			@Override
			public Pair<String,JSONObject> next() {
				return dataPoints.get(index++);
			}

		};
		return iter;
	}

	public String getTimeSeriesName() {
		return timeSeriesName;
	}

	public String getTimeInterval() {
		return timeInterval;
	}

	public String getOutputSize() {
		return outputSize;
	}

	public void setTimeSeriesName(String name) {
		timeSeriesName = name;
	}

	public void setTimeInterval(String interval) {
		timeInterval = interval;
	}

	public void setOutputSize(String size) {
		outputSize = size;
	}

	public int size() {
		return dataPoints.size();
	}

	public boolean isEmpty() {
		return dataPoints.size() == 0;
	}

	// Metod f�r att h�mta ett enskilt �ldre v�rde. Anv�nds vid k�p av aktier f�r att f� ink�pspriset om aff�ren gjorts i det f�rflutna.
	public double getOlderValue(String transactionDate) {
		AtomicReference<Double> toReturn = new AtomicReference<Double>(0.0);
		dataPoints.forEach(pair -> {
			if (pair.getKey().compareTo(transactionDate) <= 0) {
				double temp = pair.getValue().getDouble("4. close");
				if (temp > 0) {
					toReturn.set(pair.getValue().getDouble("4. close"));
				}
			} else {
				return;
			}
		});
		return toReturn.get();
	}
}