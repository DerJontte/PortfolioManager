package uppgift3.view;

/* ************************************************************************************************************************
 *  Denna klass ritar ut diagrammet med aktiernas kursförändringar.
 */
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import uppgift3.model.JDataReader;

public class StockChart extends LineChart<String, Number> {
	static CategoryAxis xAxis = new CategoryAxis();
	static NumberAxis yAxis = new NumberAxis();
	JDataReader jReader1 = GUI.stockGUI.jReader1;
	JDataReader jReader2 = GUI.stockGUI.jReader2;

	public StockChart() {
		super(xAxis, yAxis);
		setTitle(GUI.stockGUI.dataSeries.getValue());
		setTitleSide(Side.TOP);
		setAnimated(false);
		setLegendSide(Side.TOP);
		setVerticalGridLinesVisible(false);
		xAxis.setTickLabelsVisible(false);
	}

	public StockChart(String name) {
		this();
		setId(name);
	}

	void draw(Pane thisPane, String dataSeriesName) {	
		// Om det finns ett diagram sen tidigare så måste det tas bort före det nya ritas ut
		if (this != null) {
			thisPane.getChildren().remove(thisPane.lookup("#lineChart"));
			setId("lineChart");
		}

		// Checka om x-axeln skall ha datum eller klockslag och välj rätt text
		if (jReader1 != null && jReader1.isIntraDay) {
			xAxis.setLabel("Time");
		} else if (jReader1 != null) {
			xAxis.setLabel("Date");
		}

		// Checka vilken dataserie som skall ritas på y-axeln och välj rätt text
		yAxis.setLabel("Price");
		if (dataSeriesName != null) {
			if (dataSeriesName.contains("dividend")) {
				yAxis.setLabel("Dividend / share");
			} else if (dataSeriesName.contains("volume")) {
				yAxis.setLabel("Trading volume");
			} else if (dataSeriesName.contains("coefficient")) {
				yAxis.setLabel("Coefficient");
			}
		}

		// Skapa variabler för diagrammets serier och parsa dem från jReaderna
		Series<String, Number> series1 = new Series<String, Number>();
		Series<String, Number> series2 = new Series<String, Number>();
		
		if (jReader1 != null) {
			series1 = jReader1.getXYSeries(dataSeriesName);
			series1.setName(jReader1.getStockName());
		} else {
			setLegendVisible(false);
		}

		if (jReader2 != null) {			
			series2 = jReader2.getXYSeries(dataSeriesName);
			series2.setName(jReader2.getStockName());
		}

		if (jReader1.isIntraDay && !series2.getData().isEmpty()) {
			// Detta är en quick'n'dirty hack för att avhjälpa problemet att FX av nån anledning ritar linecharten helt fel
			// om det saknas värden mitt i räckan på nåndera serien. Det senast funna värdet kopieras helt sonika till den tomma
			// positionen. Värdet kunde givetvis interpoleras från de sista och följande funna värdena, men troligtvis skulle
			// inte det heller motsvara det riktiga värdet vid den givna tidpunkten.
			int max = Math.max(series1.getData().size(), series2.getData().size());
			Number last1 = series1.getData().get(0).getYValue().doubleValue();
			Number last2 = series2.getData().get(0).getYValue().doubleValue();
			for (int i = 0; i < max; i++) {
				String key1 = series1.getData().get(i).getXValue();
				String key2 = series2.getData().get(i).getXValue();
				if (key1.compareTo(key2) > 0) {
					series1.getData().add(i, new Data<String, Number>(key2, last1));
				} else if (key1.compareTo(key2) < 0) {
					series2.getData().add(i, new Data<String, Number>(key1, last2));
				}
				last1 = series1.getData().get(i).getYValue();
				last2 = series2.getData().get(i).getYValue();
			}
		}

		// Lägg till serierna till diagrammet och lägg till diagrammet till programfönstret
		if (series2.getData().isEmpty()) {
			getData().add(series1);
		} else {
			if (series1.getData().size() >= series2.getData().size()) {
				getData().add(series1);
				getData().add(series2);
			} else {
				getData().add(series2);
				getData().add(series1);				
			}
		}
		thisPane.getChildren().add(this);		
	}

	// Då programmet startas ritas ett tomt diagram. Denna metod måste användas så att inte programmet kraschar.
	public static void drawEmpty(Pane thisPane) {
		thisPane.getChildren().add(new StockChart("lineChart"));
	}


}
