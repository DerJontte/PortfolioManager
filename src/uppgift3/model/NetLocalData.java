package uppgift3.model;

import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import uppgift3.view.GUI;
import uppgift3.view.StockGUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/* ************************************************************************************************************************* /
 * 	Denna klass laddar in den lokala filen LocalData.JSON och returnerar den till användaren. Klassen implementerar
 * 	interfacet NetworkInterface, men returnerar alltid samma data oberoende av hurudana parametrar användaren ger.
 * 
 */

public class NetLocalData implements NetworkInterface {
	public NetLocalData() {
	}

	@Override
	public String getServerData(String query) throws Exception {

		String inputLine;
		String toReturn = new String();

		try (BufferedReader in = new BufferedReader(new FileReader("LocalData.JSON"))) {
			while ((inputLine = in.readLine()) != null) {
				toReturn = toReturn.concat(inputLine);
			}
		}
		return toReturn;
	}

	@Override
	public ArrayList<String> constructQuery(ArrayList<ComboBox<String>> comboList) {
		// Denna tomma metod finns med för att implementationen skall vara kompatibel med interfacet.
		return null;
	}

	@Override
	public void fetchData() throws Exception {
		if (GUI.stockGUI.stockSymbol1.getValue().equals(StockGUI.EMPTY_ITEM) && GUI.stockGUI.stockSymbol2.getValue().equals(StockGUI.EMPTY_ITEM)) {
			return;
		}

		GUI.stockGUI.setCursor(Cursor.WAIT);
		GUI.stockGUI.jReader1 = new JDataReader(getServerData(""));
		GUI.stockGUI.jReader2 = null;

		GUI.stockGUI.dataSeries.setItems(GUI.stockGUI.jReader1.getSeriesNames()); // Populera dataSeries-rullgardinen och aktivera den
		GUI.stockGUI.dataSeries.setDisable(false);
		GUI.stockGUI.dataSeriesLabel.setDisable(false);
		GUI.stockGUI.dataSeries.setValue(GUI.stockGUI.dataSeries.getItems().get(0));
		GUI.stockGUI.stockSymbol1.setValue(GUI.stockGUI.jReader1.getStockName());
		GUI.stockGUI.setTextArea(GUI.stockGUI.jReader1.getTextComparedTo(GUI.stockGUI.jReader2));
		GUI.stockGUI.setCursor(Cursor.DEFAULT);
	}

	@Override
		public String getApiKey() {
			return "None";
		}

	@Override
		public void setApiKey(String apiKeyArg) {
		}
}