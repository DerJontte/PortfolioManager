package uppgift3;

/* **************************************************************************************************************************
 * 	Denna klass innehåller main-metoden.
 * 
 * 	Vi har löst alla moment och utvecklat MVC-mönstret ytterligare från uppgift 2. Vi har slopat de globala variablerna
 * 	och använder istället hela namnen för publika metoder och variabler då de används (t.ex. GUI.stockGUI.jReaderX för
 * 	objektena med JSON-data). Vi har skrivit om en stor del av variabeltyperna till JavaFX's observable-typer och infört
 * 	lyssnare som automatiskt uppdaterar gränssnittets grafik och en del interna variabler. Många metoder har vi gjort till
 * 	flertrådade så att inte hela programmet hänger upp sig då det arbetar med t.ex. uppdatering av aktiekurser.
 * 
 */

import javafx.application.Application;
import javafx.stage.Stage;
import uppgift3.control.*;
import uppgift3.model.NetworkInterface;
import uppgift3.model.Portfolio;
import uppgift3.view.GUI;

import java.util.Locale;

public class Main extends Application {
	public static ConfigReader configs = new ConfigReader("StockAnalyzer.ini"); // Konfigurationsfilens objekt
	public static NetworkInterface networking;
	public static Portfolio portfolio = new Portfolio(); // Aktieportfolien

	@Override
	public void start(Stage primaryStage) {
		Locale.setDefault(Locale.forLanguageTag("FI")); // Sätt lokaliteten till "FI", inverkar på bl.a. datumformatet i datumväljarna
		EventHandlers.setLocalData(false);
		new GUI(primaryStage); // Rita programfönstret
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
