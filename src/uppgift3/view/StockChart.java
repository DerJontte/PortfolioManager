package uppgift3.view;

/* ************************************************************************************************************************
 *  Denna klass ritar ut diagrammet med aktiernas kursf�r�ndringar.
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
		// Om det finns ett diagram sen tidigare s� m�ste det tas bort f�re det nya ritas ut
		if (this != null) {
			thisPane.getChildren().remove(thisPane.lookup("#lineChart"));
			setId("lineChart");
		}

		// Checka om x-axeln skall ha datum eller klockslag och v�lj r�tt text
		if (jReader1 != null && jReader1.isIntraDay) {
			xAxis.setLabel("Time");
		} else if (jReader1 != null) {
			xAxis.setLabel("Date");
		}

		// Checka vilken dataserie som skall ritas p� y-axeln och v�lj r�tt text
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

		// Skapa variabler f�r diagrammets serier och parsa dem fr�n jReaderna
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
			// Detta �r en quick'n'dirty hack f�r att avhj�lpa problemet att FX av n�n anledning ritar linecharten helt fel
			// om det saknas v�rden mitt i r�ckan p� n�ndera serien. Det senast funna v�rdet kopieras helt sonika till den tomma
			// positionen. V�rdet kunde givetvis interpoleras fr�n de sista och f�ljande funna v�rdena, men troligtvis skulle
			// inte det heller motsvara det riktiga v�rdet vid den givna tidpunkten.
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

		// L�gg till serierna till diagrammet och l�gg till diagrammet till programf�nstret
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

	// D� programmet startas ritas ett tomt diagram. Denna metod m�ste anv�ndas s� att inte programmet kraschar.
	public static void drawEmpty(Pane thisPane) {
		thisPane.getChildren().add(new StockChart("lineChart"));
	}


}
