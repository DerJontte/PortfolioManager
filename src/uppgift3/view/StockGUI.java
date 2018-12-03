package uppgift3.view;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import uppgift3.Main;
import uppgift3.control.EventHandlers;
import uppgift3.model.JDataReader;
import uppgift3.model.PearsonCalculator;

import java.time.LocalDate;
import java.util.ArrayList;

public class StockGUI {
	public JDataReader jReader1, jReader2; // JDataReader-instanser f�r

	public StringProperty symbolObserver = new SimpleStringProperty();
	
	public ComboBox<String> dataSeries;
	public ComboBox<String> timeSeries;
	public ComboBox<String> stockSymbol1;
	public ComboBox<String> stockSymbol2;
	public ComboBox<String> timeInterval;
	public ComboBox<String> outputSize;

	public Label dataSeriesLabel = new Label("Data Series");
	public Label timeSeriesLabel = new Label("Time Series");
	public Label stockSymbol1Label = new Label("Stock Symbol 1");
	public Label stockSymbol2Label = new Label("Stock Symbol 2");
	public Label timeIntervalLabel = new Label("Time Interval");
	public Label outputSizeLabel = new Label("Output Size");

	ObservableList<String> stockSymbolItems;
	
	public ArrayList<ComboBox<String>> comboList = new ArrayList<>();

	public DatePicker startDatePicker = new DatePicker();
	public DatePicker endDatePicker = new DatePicker();
	public Label datePickersLabel = new Label("Start Date - End Date");
	public CheckBox enableDatePickers = new CheckBox();
	public Label onOffLabel = new Label("Date range on/off");

	public static final String EMPTY_ITEM = "---";

	public static Scene scene;

	protected static TextField apiKeyField;
	protected static TextField correlationValue = new TextField();
	public static TextArea textArea;

	public static VBox mainPane = new VBox();
	static HBox topPane = new HBox();
	static VBox bottomPane = new VBox();
	
	public StockGUI() throws Exception {
		// symbolObserver �r bunden till aktienamnet i JDataReader-klassen. Alltid d� en ny aktie laddas in s� checkar
		// denna lyssnare ifall aktien redan finns i programmets lista. Om den inte finns s� l�ggs den till och config-filen
		// sparas s� att aktien finns tillg�nglig d� programmet startar upp n�sta g�ng.
		symbolObserver.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				if (!stockSymbolItems.contains(symbolObserver.get()) && (symbolObserver.get() != null)) {
					stockSymbolItems.add(symbolObserver.get());
					stockSymbol1.setItems(stockSymbolItems);
					stockSymbol2.setItems(stockSymbolItems);
					Main.configs.save();
				}
			}
		});
	}

	public Node draw() throws Exception {		
		// Programf�nstret delas i tv� delar p� h�jden, och den �vre delen delas �nnu p� bredden, s� �r det
		// enklare att konstruera anv�ndargr�nssnittet d� delarna �r tydigt avskilda.
		mainPane.getChildren().addAll(topPane, bottomPane);
	
		topPane.setPadding(new Insets(50, 20, 25, 20));
		bottomPane.setPadding(new Insets(50, 20, 25, 20));
		topPane.setSpacing(40);
		bottomPane.setSpacing(40);

		topPane.getChildren().add(drawControls());

		// Rutan f�r aktiedatan i textform
		textArea = new TextArea();
		HBox textAreaBox = new HBox(textArea);
		textAreaBox.setPrefHeight(topPane.getHeight());
		textArea.setEditable(false);
		textArea.setFocusTraversable(false);
		topPane.getChildren().add(textAreaBox);
	
		topPane.setAlignment(Pos.CENTER);

		// Diagrammet ritas i nedre delen av programf�nstret �ver hela dess bredd.
//		(new StockChart()).draw(bottomPane, dataSeries.getValue());
		StockChart.drawEmpty(bottomPane);
		
		return mainPane;
	}

	private Node drawControls() {
		// Rullgardinsmenyerna och dess etiketter	
		ObservableList<String> timeSeriesItems = FXCollections.observableArrayList(Main.configs.getTimeSeries());
		stockSymbolItems = FXCollections.observableArrayList(Main.configs.getSymbols());
		ObservableList<String> timeIntervalItems = FXCollections.observableArrayList(Main.configs.getInterval());
		ObservableList<String> outputSizeItems = FXCollections.observableArrayList(Main.configs.getOutputSize());

		stockSymbolItems.removeAll(""); // Ta bort tomma rader (f�rorsakade av t.ex. felaktig rad i konfigurationsfilen) fr�n aktielistan.
		stockSymbolItems.sort(null); // Sortera i bokstavsordning
		stockSymbolItems.add(0, EMPTY_ITEM); // L�gg till en "rad utan v�rde" f�rst i listan.
		
		dataSeries = new ComboBox<String>();
		timeSeries = new ComboBox<String>(timeSeriesItems);
		stockSymbol1 = new ComboBox<String>(stockSymbolItems);
		stockSymbol2 = new ComboBox<String>(stockSymbolItems);
		timeInterval = new ComboBox<String>(timeIntervalItems);
		outputSize = new ComboBox<String>(outputSizeItems);

		dataSeries.setId("dataSeries");
		timeSeries.setId("timeSeries");
		stockSymbol1.setId("stockSymbol1");
		stockSymbol2.setId("stockSymbol2");
		timeInterval.setId("timeInterval");
		outputSize.setId("oututSize");

		dataSeries.setOnAction(event -> {
			try {
				updateVisuals();
			} catch (Exception e1) {
			}
		});

		timeSeries.setOnAction(event -> EventHandlers.timeSeriesHandler());

		// Query-knappens utseende och funktion
		Button queryButton = new Button("Make Query");
		queryButton.setPrefWidth(200);
		HBox queryButtonBox = new HBox(queryButton);
		queryButtonBox.setAlignment(Pos.BASELINE_CENTER);
		queryButton.setTextAlignment(TextAlignment.CENTER);
		queryButton.setOnAction(event -> {
			try {
				Main.networking.fetchData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		// API keyns textf�lt, knapp och knappens funktion
		apiKeyField = new TextField(Main.configs.getApiKey());
		Label apiKeyLabel = new Label("API Key");
		Button apiKeyButton = new Button("Update");
		apiKeyField.setFocusTraversable(true);
		apiKeyButton.setOnAction(event -> {
			Main.networking.setApiKey(apiKeyField.getText());
		});
		
		// Korrelationskoefficientens textruta. Inneh�llet best�ms efter att en query med tv� aktier har gjorts.
		Label correlationLabel = new Label("Pearson Correlation");
		correlationValue.setFocusTraversable(false);
		correlationValue.setEditable(false);

		// Datumv�ljarnas on/off-checkbox, dess etikett och en event handler som gr�ar ut komponenterna d�
		// datumv�ljarna �r avst�ngda.
		onOffLabel.setTranslateY(1); // Finjustering av etikettens placering p� h�jden... ;)
		HBox dpCheckBox = new HBox();
		dpCheckBox.setTranslateY(6);
		enableDatePickers.setTranslateX(-7);
		dpCheckBox.setMinWidth(150);
		dpCheckBox.getChildren().addAll(enableDatePickers, onOffLabel);
		enableDatePickers.setOnAction(event -> {
			startDatePicker.setDisable(!startDatePicker.isDisabled());
			endDatePicker.setDisable(!endDatePicker.isDisabled());
			datePickersLabel.setDisable(!datePickersLabel.isDisabled());
			try {
				this.updateVisuals();
			} catch (Exception e) {
			}
		});

		// Datumv�ljarna.
		startDatePicker.setValue(LocalDate.now().minusYears(10));
		startDatePicker.setFocusTraversable(true);
		startDatePicker.setPrefWidth(135);
		startDatePicker.setOnAction(event -> {
			try {
				updateVisuals();
			} catch (Exception e) {
			}
		});
		// Lyssnare som st�ller tillbaka datumet till dagen f�re endDatePickers v�rde ifall anv�ndaren f�rs�ker v�lja en 
		// startdag som ligger efter slutdatumet. Eftersom endDatePicker inte kan s�ttas i framtiden, s� kan inte heller
		// startdatumet ligga i framtiden.
		startDatePicker.valueProperty().addListener(listener -> {
			if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
				startDatePicker.setValue(endDatePicker.getValue().minusDays(1));
			}
		});	

		endDatePicker.setValue(LocalDate.now());
		endDatePicker.setFocusTraversable(true);
		endDatePicker.setPrefWidth(135);
		endDatePicker.setOnAction(event -> {
			try {
				updateVisuals();
			} catch (Exception e) {
			}
		});
		// Lyssnare som returnerar datumet till dagens datum om anv�ndaren f�rs�ker v�lja en dag som �r i framtiden, och
		// till datumet efter startdatumet ifall anv�ndaren f�rs�ker v�lja ett slutdatum som �r tidigare �n startdatumet.
		endDatePicker.valueProperty().addListener(listener -> {
			if (endDatePicker.getValue().isAfter(LocalDate.now())) {
				endDatePicker.setValue(LocalDate.now());
			}
			if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
				endDatePicker.setValue(startDatePicker.getValue().plusDays(1));
			}
		});	

		HBox datePickers = new HBox();
		datePickers.setSpacing(25);
		datePickers.getChildren().addAll(startDatePicker, endDatePicker);

		// GridPanen leftBoxes inneh�ller alla rullgardinsmenyer, knappar och datumv�ljare som finns i den v�nstra halvan
		// ovanf�r diagrammet.
		GridPane leftBoxes = new GridPane();
		leftBoxes.setHgap(25); // 25 pixels �r ett passligt avst�nd mellan elementen.
		leftBoxes.setVgap(25);

		int gridY = 0;
		leftBoxes.addRow(gridY++, apiKeyLabel, apiKeyField, apiKeyButton);
		leftBoxes.addRow(gridY++, dataSeriesLabel, dataSeries);
		leftBoxes.addRow(gridY++, timeSeriesLabel, timeSeries);
		leftBoxes.addRow(gridY++, stockSymbol1Label, stockSymbol1);
		leftBoxes.addRow(gridY++, stockSymbol2Label, stockSymbol2);
		leftBoxes.addRow(gridY++, timeIntervalLabel, timeInterval);
		leftBoxes.addRow(gridY++, outputSizeLabel, outputSize);
		leftBoxes.addRow(gridY++, datePickersLabel, datePickers, dpCheckBox);
		leftBoxes.add(queryButtonBox, 0, gridY++, 3, 1);
		leftBoxes.addRow(gridY++, correlationLabel, correlationValue);
		
		comboList.add(dataSeries);
		comboList.add(timeSeries);
		comboList.add(stockSymbol1);
		comboList.add(stockSymbol2);
		comboList.add(timeInterval);
		comboList.add(outputSize);
		
		dataSeries.setDisable(true); 		// I detta skede finns �nnu ingen data att v�lja serie fr�n
		dataSeriesLabel.setDisable(true);
		startDatePicker.setDisable(true);			// Datumv�ljarna �r ocks� avst�ngda d� programmet startar upp
		endDatePicker.setDisable(true);
		datePickersLabel.setDisable(true);
		
		for (ComboBox<String> cb : comboList) {	// Loopar igenom alla ComboBoxar och st�ller in f�rsta v�rdet som defaultv�rde i
			if (cb.getItems().size() > 0) {		// de boxar som har n�got inneh�ll.
				cb.setValue(cb.getItems().get(0));
			}
			cb.setOnKeyPressed(event -> EventHandlers.keyTranslator(event, cb));	// En handler som "f�ller ut" rullgardinsmenyn d� anv�ndaren trycker p� space eller enter
			cb.resize(350, 0);	// Bredden f�r alla ComboBoxar skall vara 350 
		}
		return leftBoxes;
	}

	// Metod f�r att uppdatera alla tre datavisualiserare (diagrammet, listan med v�rden samy korrelationskoefficienten) p� samma g�ng
	public void updateVisuals() throws Exception {
		if (dataSeries.getValue() != null) {
			(new StockChart()).draw(bottomPane, dataSeries.getValue().toString());
			setTextArea(jReader1.getTextComparedTo(jReader2));
			if (jReader1 != null && jReader2 != null) {
				String pc = PearsonCalculator.calculate(jReader1, jReader2);
				correlationValue.setText(pc);
			} else {
				correlationValue.clear();
			}
		}
	}

	public void setCursor(Cursor cursor) {
		mainPane.setCursor(cursor);
		apiKeyField.setCursor(cursor);
		StockGUI.correlationValue.setCursor(cursor);
	}

	public void setTextArea(String text) {
		textArea.setText(text);
		try {
			Thread.sleep(500);				// Av n�gon anledning m�ste en kort paus h�llas f�r att textrutan skall vara scrollad till b�rjan...
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		textArea.positionCaret(0);
	}

	void appendTextArea(String text) {
		textArea.appendText(text);		
	}

}