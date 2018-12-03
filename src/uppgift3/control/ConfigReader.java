package uppgift3.control;

// Denna klass läser in programmets inställningar från filen StockAnalyzer.ini, och har getters som låter klientkoden
// läsa av de diverse värdena. Inställningarna laddas från filen då ett ConfigReader-objekt skapas med konfigfilens namn
// som argument.

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uppgift3.view.ErrorMessage;
import uppgift3.view.GUI;
import uppgift3.view.StockGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class ConfigReader {
	private String configFile = new String();
	private static ArrayList<String> configData = new ArrayList<String>();
		
	//Konstruktor 
	public ConfigReader(String filename) {
		configFile = filename;
		load();
	}
	
	// Ladda in värden från filen och spara dem i textform i objektet
	private void load() {
		try (BufferedReader in = new BufferedReader(new FileReader(configFile))){
			String inLine = new String();
			while((inLine = in.readLine()) != null) {
				configData.add(inLine);
			} 
		} catch (Exception e) {
			new ErrorMessage("Settings.load(): " + e.toString());
		}
	}
	
	// I denna version av programmet går det även att spara konfigurationen för framtida bruk. Främst görs detta
	// så att aktier kan läggas till av användaren och  finnas kvar följande gång programmet startas.
	public void save() {
		String toWrite = new String();
		toWrite = toWrite.concat("SYMBOL=" + GUI.stockGUI.stockSymbol1.getItems());
		toWrite = toWrite.concat("TIME_SERIES=" + getTimeSeries().toString());
		toWrite = toWrite.concat("TIME_INTERVAL=" + getInterval().toString());
		toWrite = toWrite.concat("OUTPUT_SIZE=" + getOutputSize().toString());
		toWrite = toWrite.concat("API_KEY=" + getApiKey().toString() + "\n");
		toWrite = toWrite.replace("[", "");		// Inställningarna läggs först till som räckor i utdatan, varefter klammrarna rensas bort och
		toWrite = toWrite.replace("]", "\r\n"); // till slutet av raderna läggs radbrytningar som fungerar i såväl Windows som Linux och Mac.
		toWrite = toWrite.replace(StockGUI.EMPTY_ITEM, ""); // Koden för "tom rad" i aktielistan rensas bort från utdatan.

		try (BufferedWriter out = new BufferedWriter(new FileWriter(configFile))) {
			out.write(toWrite);
		} catch (Exception e) {
			new ErrorMessage("Settings.save(): " + e.toString());
		}
	}
	
	// Convenience-metoder för att hämta de olika inställningarna.
	public ObservableList<String> getSymbols() {
		return get("SYMBOL");
	}
	
	public ObservableList<String> getTimeSeries() {
		return get("TIME_SERIES");
	}
	
	public ObservableList<String> getInterval() {
		return get("TIME_INTERVAL");
	}
	
	public ObservableList<String> getOutputSize() {
		return get("OUTPUT_SIZE");
	}
	
	public String getApiKey() {
		String toReturn = null;
		for(String s: configData) {			
			if (s.trim().startsWith("API_KEY")) {
				toReturn = s.split("=")[1].trim();
			}
		}
		if (toReturn == null) {
			new ErrorMessage("Configuration file error: API key missing, please provide a valid API key.");
		}
		return toReturn;
	}
	
	// Getter för att returnera inställningar som består av flere värden (t.ex. rullgardinsmenyernas innehåll)
	public ObservableList<String> get(String key) {
		String tempString[] = null;
		for(String s: configData) {			
			if (s.trim().startsWith(key)) {
				tempString = s.split("=")[1].split(",");
				for(int i = 0; i < tempString.length; i++) {
					tempString[i] = tempString[i].trim();
				}
			}
		}
		if(tempString == null) {
			new ErrorMessage("Configuration file error: value " + key + " missing");
			return FXCollections.observableArrayList("");
		}
		ObservableList<String> toReturn = FXCollections.observableArrayList(tempString);
		return toReturn;
	}
	
}
