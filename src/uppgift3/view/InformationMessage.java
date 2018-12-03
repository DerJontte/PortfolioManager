package uppgift3.view;
/* **************************************************************************************************************************
 * 	Denna metod visar en informationsruta till användaren. Ingen metod för att läsa av data behövs.
 */

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class InformationMessage {
	public InformationMessage(String message) {
		Alert toShow = new Alert(AlertType.INFORMATION);
		toShow.setHeaderText(null);
		toShow.setContentText(message);
		toShow.showAndWait();
	}
}
