package uppgift3.control;

/* ************************************************************************************************************************
 * 	Dena klass inneh�ller dialogrutorna och deras st�dmetoder f�r att ladda, spara och skapa nya aktieportfolier. 
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
		// FileChooser instantierar ett f�nster med operativsystemets ruta f�r att �ppna eller spara filer.
		// Beroende p� hur man definierar parametrarna och anv�nder datan fr�n rutan s� kan den allts� anv�ndas antingen
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
	 * ************************************************ F�r att spara
	 * aktieportf�ljer anv�nds tre metoder i samarbete.
	 * 
	 * savePortfolioAs() �ppnar en dialogruta som l�ter anv�ndaren spara portf�ljen
	 * med ett nytt namn eller skriva �ver en befintlig fil.
	 * 
	 * savePortfolio() tar in ett varararg av typen File och utf�r olika funktioner
	 * beroende p� antalet filnamn som givits. Genom att anv�nda varargs s� r�cker
	 * det med en enda metod, s� beh�ver man inte h�lla reda p� flera �verlagrade
	 * metoder. Om inget argument getts s� har den f�r tillf�llet inladdade
	 * portfolien inte �nnu n�got associerat filnamn, och savePortfolio() anropar
	 * metoden savePortfolioAs().
	 * 
	 * Om exakt ett argument getts s� har portfolien redan en associerad fil, och
	 * den inladdade portfolien skrivs till den.
	 * 
	 * Om tv� eller flera argument getts har metoden anropats felaktigt, och ett
	 * felmeddelande visas till anv�ndaren.
	 * 
	 * D� savePortfolio() och savePortfolioAs() har valt r�tt rutt s� anropas
	 * metoden writeToDisk() som tar hand om att konkret skriva portfoliens
	 * inneh�ll till en fil.
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
			return true; // Om portfolien �r tom s� beh�ver inget g�ras.
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
