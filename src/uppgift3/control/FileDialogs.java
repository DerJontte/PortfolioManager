package uppgift3.control;

/* ************************************************************************************************************************
 * 	Dena klass innehåller dialogrutorna och deras stödmetoder för att ladda, spara och skapa nya aktieportfolier. 
 */

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import uppgift3.model.Portfolio;
import uppgift3.view.ErrorMessage;
import uppgift3.view.GUI;

import java.io.*;

import static uppgift3.Main.portfolio;

public class FileDialogs {

	public static void openPortfolio() {
		// FileChooser instantierar ett fönster med operativsystemets ruta för att öppna eller spara filer.
		// Beroende på hur man definierar parametrarna och använder datan från rutan så kan den alltså användas antingen
		// till att ladda eller spara en fil.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Portfolio");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("StockAnalyzer Portfolios", "*.spf"), new ExtensionFilter("All files", "*.*"));
		File fileName = fileChooser.showOpenDialog(null);
		if (fileName != null) {
			try (ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(fileName))){
				portfolio = ((Portfolio) inStream.readObject()).fromSave();
				GUI.portfolioGUI.clearSelectedRows();
				GUI.portfolioGUI.update();
			} catch (Exception e) {
				new ErrorMessage(e.getMessage());
				e.printStackTrace();
			} 
		}
	}

	/*
	 * *****************************************************************************
	 * ************************************************ För att spara
	 * aktieportföljer används tre metoder i samarbete.
	 * 
	 * savePortfolioAs() öppnar en dialogruta som låter användaren spara portföljen
	 * med ett nytt namn eller skriva över en befintlig fil.
	 * 
	 * savePortfolio() tar in ett varararg av typen File och utför olika funktioner
	 * beroende på antalet filnamn som givits. Genom att använda varargs så räcker
	 * det med en enda metod, så behöver man inte hålla reda på flera överlagrade
	 * metoder. Om inget argument getts så har den för tillfället inladdade
	 * portfolien inte ännu något associerat filnamn, och savePortfolio() anropar
	 * metoden savePortfolioAs().
	 * 
	 * Om exakt ett argument getts så har portfolien redan en associerad fil, och
	 * den inladdade portfolien skrivs till den.
	 * 
	 * Om två eller flera argument getts har metoden anropats felaktigt, och ett
	 * felmeddelande visas till användaren.
	 * 
	 * Då savePortfolio() och savePortfolioAs() har valt rätt rutt så anropas
	 * metoden writeToDisk() som tar hand om att konkret skriva portfoliens
	 * innehåll till en fil.
	 * 
	 */

	public static boolean savePortfolio() {
		savePortfolioAs();
		return true;
	}

	public static boolean savePortfolioAs() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Portfolio As");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("StockAnalyzer Portfolios", "*.spf"), new ExtensionFilter("All files", "*.*"));
		File saveFile = fileChooser.showSaveDialog(null);
		if (saveFile != null) {
			return writeToDisk(saveFile);
		} 
		return false;
	}

	public static boolean writeToDisk(File saveFile) {
		try (FileOutputStream outFile = new FileOutputStream(saveFile);
				ObjectOutputStream outStream = new ObjectOutputStream(outFile)){
			outStream.writeObject(portfolio.toSave());
			return true;
		} catch (Exception e) {
			new ErrorMessage(e.toString());
			return false;
		}
	}

	public static boolean newPortfolio() {
		if (portfolio.isEmpty()) {
			return true; // Om portfolien är tom så behöver inget göras.
		}
		Alert alert = new Alert(AlertType.CONFIRMATION);

		alert.setTitle("Create New Portfolio");
		alert.setHeaderText(null);
		alert.setContentText("Save changes before creating a new portfolio?");

		ButtonType yes = new ButtonType("_Yes");
		ButtonType no = new ButtonType("_No");
		ButtonType cancel = new ButtonType("Cancel");

		alert.getButtonTypes().setAll(yes, no, cancel);

		alert.getDialogPane().setOnKeyPressed(event -> EventHandlers.keyTranslator(event, alert));

		ButtonType result = alert.showAndWait().get();
		if (result == cancel) {
			return false;
		} else if ((result == yes && FileDialogs.savePortfolio()) || result == no) {
			portfolio = new Portfolio();
			GUI.portfolioGUI.update();
			return true;
		}
		return false;
	}
}
