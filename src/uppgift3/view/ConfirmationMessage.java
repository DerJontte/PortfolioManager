package uppgift3.view;

/* ************************************************************************************************************************
 * 	Denna klass visar en dialogruta f�r att bekr�fta ett val, t.ex. vid aktiek�p. Tar in texten som skall visas som argument
 * 	till konstruktorn och l�ter svaret avl�sas med get()-metoden.
 */

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import uppgift3.control.EventHandlers;

public class ConfirmationMessage {
	Alert toShow = new Alert(AlertType.CONFIRMATION);
	
	public ConfirmationMessage(String message) {
		toShow.dialogPaneProperty().get().setOnKeyPressed(event -> EventHandlers.keyTranslator(event));
		toShow.setHeaderText(null);
		toShow.setContentText(message);
		toShow.showAndWait();
	}
	
	public String get() {
		return toShow.getResult().toString();
	}
}
