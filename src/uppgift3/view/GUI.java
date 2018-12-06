package uppgift3.view;

/* *************************************************************************************************************************
 *	GUI-klassen är rotklass för användargränssnittet. Här skapas bottnen för programfönstret med menyraden, flikarna
 *	för aktie- och portfoliedelarna av programmet (flikarnas innehåll ritas av klasserna StockGUI och PortfolioGUI) samt
 *	statustexten med information om vad programmet har för sig.
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
			scene.getStylesheets().addAll("application.css"); // Stylesheet för vissa delar av gränssnittet

			// TabPane skapar en botten för de olika flikarna i programmet. Det går lätt att lägga till så många
			// flikar som behövs genom att lägga till instanser av Tab-klassen och definiera nya klasser som
			// innehåll i dem.
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

			// Statusraden definieras här. Metoder för att ändra texten och för dynamiska texter finns längre ner.
			statusMessage = new TextField("All systems are GO");
			statusMessage.relocate(240, 30);
			statusMessage.setPrefWidth(400);
			statusMessage.setFocusTraversable(false);
			statusMessage.setEditable(false);
			statusMessage.setStyle("-fx-background-color: transparent"); // CSS kan användas för att ändra på de grafiska komponenterna i JavaFX

			tabPane.getTabs().addAll(stockTab, portfolioTab);
			VBox content = new VBox();
			content.getChildren().addAll(CommonMenu.menu(), tabPane);
			root.getChildren().addAll(content, statusMessage);

			// KeyTranslatorn igen för att kunna använda enter till att bekräfta val och trycka på knappar.
			scene.setOnKeyPressed(event -> EventHandlers.keyTranslator(event));
			primaryStage.setOnCloseRequest(value -> EventHandlers.exitEvent()); // Denna rad kapar åt sig programmets exit-händelse och skickar den till en eventhandler.
			primaryStage.setResizable(false); // Storleken på programfönstret går inte att ändra, så är det enklare att konstruera användargränssnittet. 
			primaryStage.setTitle("StockAnalyzer");
			primaryStage.setScene(scene);
			primaryStage.show();
			// TimeSeries är troligtvis det som användaren först vill ändra på, så programmets fokus sätts till den rullgardinsmenyn:
			GUI.stockGUI.timeSeries.requestFocus();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// setBusyMessage låter klientkoden sätta en text med tre "animerade" punkter i statustexten.
	// Efter att den tråd som anropat denna metod har kört färdigt, så byts texten till ett statiskt "Ready".
	// Denna metod vet när den anropande tråden är klar genom att kolla BooleanPropertyn running, som
	// klientkoden skickat som argument och är ansvarig för att uppdatera.
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
	
	// Metod för att sätta ett statiskt statusmeddelande
	public static void setStatusMessage(String message) {
		statusMessage.setText(message);
	}

	// Metod för att lägga till text eller tecken till slutet på statusmeddelandet.
	// Används t.ex. av setBusyMessage för att lägga till punkter till den dynamiska statustexten.
	public static void appendStatusMessage(String message) {
		statusMessage.setText(statusMessage.getText().concat(message));
	}
}
