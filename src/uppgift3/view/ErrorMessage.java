package uppgift3.view;

// Klass för att visa en dialogruta med ett felmeddelande. Har överlagrade konstruktorer som möjliggör att skicka antingen
// en "rå" Exception eller en vanlig textsträng till klassen.
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
