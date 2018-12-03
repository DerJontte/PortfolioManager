package uppgift3;

/* **************************************************************************************************************************
 * 	Denna klass inneh�ller main-metoden.
 * 
 * 	Vi har l�st alla moment och utvecklat MVC-m�nstret ytterligare fr�n uppgift 2. Vi har slopat de globala variablerna
 * 	och anv�nder ist�llet hela namnen f�r publika metoder och variabler d� de anv�nds (t.ex. GUI.stockGUI.jReaderX f�r
 * 	objektena med JSON-data). Vi har skrivit om en stor del av variabeltyperna till JavaFX's observable-typer och inf�rt
 * 	lyssnare som automatiskt uppdaterar gr�nssnittets grafik och en del interna variabler. M�nga metoder har vi gjort till
 * 	flertr�dade s� att inte hela programmet h�nger upp sig d� det arbetar med t.ex. uppdatering av aktiekurser.
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
		Locale.setDefault(Locale.forLanguageTag("FI")); // S�tt lokaliteten till "FI", inverkar p� bl.a. datumformatet i datumv�ljarna
		EventHandlers.setLocalData(false);
		new GUI(primaryStage); // Rita programf�nstret
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
