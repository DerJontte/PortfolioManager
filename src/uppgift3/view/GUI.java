package uppgift3.view;

/* *************************************************************************************************************************
 *	GUI-klassen �r rotklass f�r anv�ndargr�nssnittet. H�r skapas bottnen f�r programf�nstret med menyraden, flikarna
 *	f�r aktie- och portfoliedelarna av programmet (flikarnas inneh�ll ritas av klasserna StockGUI och PortfolioGUI) samt
 *	statustexten med information om vad programmet har f�r sig.
 */

import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uppgift3.control.EventHandlers;

import java.util.concurrent.atomic.AtomicInteger;

public class GUI {
	public static Stage primaryStage;
	public static StockGUI stockGUI;
	public static PortfolioGUI portfolioGUI;
	private static TextField statusMessage;
	public static Scene scene;

	public GUI(Stage stage) {
		primaryStage = stage;
		draw(primaryStage);
	}

	public void draw(Stage primaryStage) {
		try {
			Group root = new Group();
			scene = new Scene(root, 1200, 900);
			scene.getStylesheets().addAll("application.css"); // Stylesheet f�r vissa delar av gr�nssnittet

			// TabPane skapar en botten f�r de olika flikarna i programmet. Det g�r l�tt att l�gga till s� m�nga
			// flikar som beh�vs genom att l�gga till instanser av Tab-klassen och definiera nya klasser som
			// inneh�ll i dem.
			TabPane tabPane = new TabPane();
			tabPane.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, null, null)));
			tabPane.prefHeightProperty().bind(scene.heightProperty());
			tabPane.prefWidthProperty().bind(scene.widthProperty());
			tabPane.setTabMinWidth(100);
			tabPane.setTabMaxWidth(100);
			tabPane.setFocusTraversable(false);

			Tab stockTab = new Tab("Stock viewer");
			stockGUI = new StockGUI();
			stockTab.setContent(stockGUI.draw());
			stockTab.setClosable(false);

			Tab portfolioTab = new Tab("Portfolio Viewer");
			portfolioGUI = new PortfolioGUI(primaryStage);
			portfolioTab.setContent(portfolioGUI.draw());
			portfolioTab.setClosable(false);
			portfolioTab.selectedProperty().addListener(state -> {
				GUI.portfolioGUI.setSelected(portfolioTab.isSelected());
			});

			// Statusraden definieras h�r. Metoder f�r att �ndra texten och f�r dynamiska texter finns l�ngre ner.
			statusMessage = new TextField("All systems are GO");
			statusMessage.relocate(240, 30);
			statusMessage.setPrefWidth(400);
			statusMessage.setFocusTraversable(false);
			statusMessage.setEditable(false);
			statusMessage.setStyle("-fx-background-color: transparent"); // CSS kan anv�ndas f�r att �ndra p� de grafiska komponenterna i JavaFX

			tabPane.getTabs().addAll(stockTab, portfolioTab);
			VBox content = new VBox();
			content.getChildren().addAll(CommonMenu.menu(), tabPane);
			root.getChildren().addAll(content, statusMessage);

			// KeyTranslatorn igen f�r att kunna anv�nda enter till att bekr�fta val och trycka p� knappar.
			scene.setOnKeyPressed(event -> EventHandlers.keyTranslator(event));
			primaryStage.setOnCloseRequest(value -> EventHandlers.exitEvent()); // Denna rad kapar �t sig programmets exit-h�ndelse och skickar den till en eventhandler.
			primaryStage.setResizable(false); // Storleken p� programf�nstret g�r inte att �ndra, s� �r det enklare att konstruera anv�ndargr�nssnittet. 
			primaryStage.setTitle("StockAnalyzer");
			primaryStage.setScene(scene);
			primaryStage.show();
			// TimeSeries �r troligtvis det som anv�ndaren f�rst vill �ndra p�, s� programmets fokus s�tts till den rullgardinsmenyn:
			GUI.stockGUI.timeSeries.requestFocus();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// setBusyMessage l�ter klientkoden s�tta en text med tre "animerade" punkter i statustexten.
	// Efter att den tr�d som anropat denna metod har k�rt f�rdigt, s� byts texten till ett statiskt "Ready".
	// Denna metod vet n�r den anropande tr�den �r klar genom att kolla BooleanPropertyn running, som
	// klientkoden skickat som argument och �r ansvarig f�r att uppdatera.
	public static void setBusyMessage(String message, BooleanProperty running) {
		GUI.setStatusMessage(message);
		final AtomicInteger counter = new AtomicInteger(0);
		new Thread(() -> {
			while(running.get()) {
				try {
					Thread.sleep(500);
					if (counter.getAndIncrement() < 3) {
						GUI.appendStatusMessage(".");
					} else {
						GUI.setStatusMessage(message);
						counter.set(0);
					}
				} catch (Exception e) {
					// Do nothing. No biggie if this thread fails.
				}
			}
			GUI.setStatusMessage("Ready");
		}).start();		
	}
	
	// Metod f�r att s�tta ett statiskt statusmeddelande
	public static void setStatusMessage(String message) {
		statusMessage.setText(message);
	}

	// Metod f�r att l�gga till text eller tecken till slutet p� statusmeddelandet.
	// Anv�nds t.ex. av setBusyMessage f�r att l�gga till punkter till den dynamiska statustexten.
	public static void appendStatusMessage(String message) {
		statusMessage.setText(statusMessage.getText().concat(message));
	}
}
