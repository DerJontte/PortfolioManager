package uppgift3.view;

/* ************************************************************************************************************************
 * 	Denna klass visar en dialogruta för att bekräfta ett val, t.ex. vid aktieköp. Tar in texten som skall visas som argument
 * 	till konstruktorn och låter svaret avläsas med get()-metoden.
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
