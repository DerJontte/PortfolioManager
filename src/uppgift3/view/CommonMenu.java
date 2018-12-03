package uppgift3.view;

/* ************************************************************************************************************************
 * 	Denna klass konstruerar programmets menyrad.
 * 
 * 	File-menyn har funktioner för att skapa, ladda och spara portfolier, och settings-menyn har ett alternativ för
 * 	att välja om aktiedatan skall hämtas från AlphaVantage eller om en lokal fil skall användas.
 */

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import uppgift3.control.EventHandlers;
import uppgift3.control.FileDialogs;

public class CommonMenu {
	
	public CommonMenu() {
		menu();
	}
	
	public static Node menu() {
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu(), settingsMenu());
		return menuBar;
	}
	
	private static Menu fileMenu() {
		Menu fileMenu = new Menu("_File");

		MenuItem newPortfolio = new MenuItem("_New Portfolio");
		MenuItem openPortfolio = new MenuItem("_Open Portfolio");
		MenuItem savePortfolio = new MenuItem("_Save Portfolio");
		MenuItem savePortfolioAs = new MenuItem("Save Portfolio _As");
		MenuItem exit = new MenuItem("E_xit");		

		// Kortkommandon för de olika funktionerna i menyn
		openPortfolio.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
		newPortfolio.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+N"));
		savePortfolio.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		savePortfolioAs.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
		exit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));

		// Lambdauttryck med event handlers för de olika menyalternativen.
		openPortfolio.setOnAction(event -> {
			FileDialogs.openPortfolio();
		});
		newPortfolio.setOnAction(event -> {
			FileDialogs.newPortfolio();
		});
		savePortfolio.setOnAction(event -> FileDialogs.savePortfolio());
		savePortfolioAs.setOnAction(event -> FileDialogs.savePortfolioAs());
		exit.setOnAction(event -> EventHandlers.exitEvent());

		fileMenu.getItems().addAll(newPortfolio, openPortfolio, savePortfolio, savePortfolioAs, new SeparatorMenuItem(), new SeparatorMenuItem(), exit);
		
		return fileMenu;
	}
	
	private static Menu settingsMenu() {
		Menu settingsMenu = new Menu("_Settings");
		CheckMenuItem localData = new CheckMenuItem("Use local data in stock view");
		
		localData.setOnAction(event -> EventHandlers.setLocalData(localData.isSelected()));
		settingsMenu.getItems().addAll(localData);
		return settingsMenu;
	}
}
