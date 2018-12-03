package uppgift3.view;

// Klass f�r att visa en dialogruta med ett felmeddelande. Har �verlagrade konstruktorer som m�jligg�r att skicka antingen
// en "r�" Exception eller en vanlig textstr�ng till klassen.
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorMessage {
	public ErrorMessage(Exception e) {
		this(e.toString());
		e.printStackTrace();
	}
	
	public ErrorMessage(String e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(null);
		alert.setContentText(e.toString());
		alert.showAndWait();
	}
}
