package uppgift3.control;

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import uppgift3.Main;
import uppgift3.model.NetAlphaVantage;
import uppgift3.model.NetLocalData;
import uppgift3.view.GUI;

public class EventHandlers {
	static Pane parentPane;

	public EventHandlers() throws Exception {
	}

	public static void setRootPane(Pane pane) {
		parentPane = pane;
	}


	// Aktivera/avaktivera de olika alternativen som kan användas för olika tidsserier då de laddas från servern
	public static void timeSeriesHandler() {
		String timeSeriesValue = GUI.stockGUI.timeSeries.getValue().toString();
		Node timeInterval = GUI.stockGUI.timeInterval;
		Node outputSize = GUI.stockGUI.outputSize;
		Node timeIntervalLabel = GUI.stockGUI.timeIntervalLabel;
		Node outputSizeLabel = GUI.stockGUI.outputSizeLabel;

		switch (timeSeriesValue) {
		case "TIME_SERIES_INTRADAY": // Intraday
			timeInterval.setDisable(false);
			outputSize.setDisable(false);
			timeIntervalLabel.setDisable(false);
			outputSizeLabel.setDisable(false);
			break;
		case "TIME_SERIES_DAILY": // Daily
			timeInterval.setDisable(true);
			outputSize.setDisable(false);
			timeIntervalLabel.setDisable(true);
			outputSizeLabel.setDisable(false);
			break;
		case "TIME_SERIES_DAILY_ADJUSTED": // Daily_Adjusted
			timeInterval.setDisable(true);
			outputSize.setDisable(false);
			timeIntervalLabel.setDisable(true);
			outputSizeLabel.setDisable(false);
			break;
		case "TIME_SERIES_WEEKLY": // Weekly
			timeInterval.setDisable(true);
			outputSize.setDisable(true);
			timeIntervalLabel.setDisable(true);
			outputSizeLabel.setDisable(true);
			break;
		case "TIME_SERIES_WEEKLY_ADJUSTED": // Weekly_Adjusted
			timeInterval.setDisable(true);
			outputSize.setDisable(true);
			timeIntervalLabel.setDisable(true);
			outputSizeLabel.setDisable(true);
			break;
		case "TIME_SERIES_MONTHLY": // Monthly
			timeInterval.setDisable(true);
			outputSize.setDisable(true);
			timeIntervalLabel.setDisable(true);
			outputSizeLabel.setDisable(true);
			break;
		case "TIME_SERIES_MONTHLY_ADJUSTED": // Monthly_Adjusted
			timeInterval.setDisable(true);
			outputSize.setDisable(true);
			break;
		}
	};

	// Denna event handler definierar funktioner för knappar som inte är definierade som standard i Java
	public static Object keyTranslator(KeyEvent event, Object... objects) {
		EventTarget eventTarget = event.getTarget();
		Class<?> clazz = event.getTarget().getClass();
		KeyCode keyCode = event.getCode();
		
		// Escape fungerar som cancel-knapp
		if (keyCode.equals(KeyCode.ESCAPE)) {
			for (Object o: objects) {
				if (o.getClass().equals(Alert.class)) {
					((Alert)o).close();
				}
			}
		}

		// Delete-knappen raderar markerade rader i portfolielistan
		if (keyCode.equals(KeyCode.DELETE)) {
			event.consume();
			if (GUI.portfolioGUI.isSelected()) {
				GUI.portfolioGUI.removeRows();
			}
		}
		
		// Ctrl+A markerar alla rader i portfolielistan
		if (event.isControlDown() && keyCode.equals(KeyCode.A)) {
			event.consume();
			if (GUI.portfolioGUI.isSelected()) {
				GUI.portfolioGUI.selectAllRows();
			}
		}

		// Ctrl+N avmarkerar alla rader i portfolielistan
		if (event.isControlDown() && !event.isShiftDown() && keyCode.equals(KeyCode.N)) {
			event.consume();
			if (GUI.portfolioGUI.isSelected()) {
				GUI.portfolioGUI.clearSelectedRows();
			}
		}

		// Enter aktiverar den knapp, checkbox eller rullgardinsmeny som (eventuellt) är vald för tillfället
		if (keyCode.equals(KeyCode.ENTER) || keyCode.equals(KeyCode.SPACE)) {
			if (clazz.getName().contains("Button")) {
				((Button)eventTarget).fire();
			}
			if (clazz.equals(CheckBox.class)) {
				((CheckBox)event.getTarget()).fire();
			}

			for (Object o: objects) {
				if (clazz.equals(ComboBox.class)) {
					((ComboBox<?>)o).show();
				}
			}
		}
		return null;
	}

	// Om något behöver städas upp då programmet avslutas så görs det i exitEvent()-metoden.
	public static void exitEvent() {
		//TODO: check portfolio save state and other cleaning up
		System.exit(0);
	}

	public static void setLocalData(boolean useLocal) {
		// NetworkInterface har två implementerande klasser, en för AlphaVantage och en för lokal data. Dessa kan bytas i programmenyn.
		if (useLocal) {
			Main.networking = new NetLocalData();
		} else {
			Main.networking = new NetAlphaVantage("https://www.alphavantage.co/", Main.configs.getApiKey());
		}
	}

}
