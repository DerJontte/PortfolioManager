package uppgift3.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import uppgift3.Main;
import uppgift3.view.ErrorMessage;
import uppgift3.view.GUI;
import uppgift3.view.StockGUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/* ************************************************************************************************************************* /
 * 	Denna klass anv�nds f�r att koppla upp mot AlphaVantage och har metoder specifika f�r att h�mta data fr�n den servern.
 * 	Konstruktorn lagrar serverns adress och en APIKey i klassvariabler, och med dessa plus den formaterade queryn h�mtas data
 * 	fr�n servern.
 */

public class NetAlphaVantage implements NetworkInterface {
	String server;
	private String apiKey;
	
	public NetAlphaVantage(String serverArg, String APIKeyArg) {
		server = serverArg;
		setApiKey(APIKeyArg);
	}

	// getServerData tar en query-string som argument och sammanfogar server och query till en HTTP-request. Denna
	// generaliserade form g�r att �teranv�nda med andra servrar och f�r olika typer av requests. Serverns svar
	// returneras som en String till den anropande metoden.
	@Override
	public String getServerData(String query) throws Exception {

		String inputLine;
		String toReturn = new String();
		URL url = new URL(server + query);

		URLConnection wwwSession = url.openConnection();
		wwwSession.setReadTimeout(30000);

		BufferedReader in = new BufferedReader(new InputStreamReader(wwwSession.getInputStream()));
		while ((inputLine = in.readLine()) != null) {
			toReturn = toReturn.concat(inputLine);
		}
		return toReturn;
	}

	// constructQuery g�r igenom en lista med programmets rullgardinsmenyer och formaterar en eller tv� queries till servern. 
	// Med returv�rdet som en ArrayList �r det m�jligt att �ndra programmet att visa upp s� m�nga aktier som det beh�vs
	// eller �nskas. Det g�r ocks� relativt enkelt att �ndra s� att anv�ndaren kan v�lja dynamiskt i sj�lva programmet hur m�nga
	// aktier som skall visas.
	@Override
	public ArrayList<String> constructQuery(ArrayList<ComboBox<String>> comboList) {
		ArrayList<String> toReturn = new ArrayList<String>();
		String tsQuery = new String();
		String ssQuery1 = new String();
		String ssQuery2 = new String();
		String tiQuery = new String();
		String osQuery = new String();
		String ssCheck = new String();
		
		for(ComboBox<?> cb: comboList) {
			switch(cb.getId()) {
			case "timeSeries":
				tsQuery = "query?function=" + cb.getValue();
				break;
			case "stockSymbol1":
				ssCheck = cb.getValue().toString();
				if(ssCheck != StockGUI.EMPTY_ITEM) {
					ssQuery1 = "&symbol=" + ssCheck;
				} else {
					ssQuery1 = null;
				}
				break;
			case "stockSymbol2":
				ssCheck = cb.getValue().toString();
				if(ssCheck != StockGUI.EMPTY_ITEM) {
					ssQuery2 = "&symbol=" + ssCheck;
				} else {
					ssQuery2 = null;
				}
				break;
			case "timeInterval":
				if (!cb.isDisabled()) {
					tiQuery = "&interval=" + cb.getValue();
				}
				break;
			case "outputSize":
				if (!cb.isDisabled()) {
					osQuery = "&outputsize=" + cb.getValue();
				}
				break;
			}
		}
		
		if(ssQuery1 != null) {
			toReturn.add(tsQuery + ssQuery1 + tiQuery + osQuery + apiKey);
		}
		if(ssQuery2 != null) {
			toReturn.add(tsQuery + ssQuery2 + tiQuery + osQuery + apiKey);
		}
		return toReturn;
	}
	
	// fetchData() h�mtar data fr�n servern om �tminstone den ena aktiekoden �r n�got annat �n "rad utan v�rde".
	@Override
	public void fetchData() throws Exception {
		if (GUI.stockGUI.stockSymbol1.getValue().equals(StockGUI.EMPTY_ITEM) && GUI.stockGUI.stockSymbol2.getValue().equals(StockGUI.EMPTY_ITEM)) {
			return;
		}
		
		final BooleanProperty running = new SimpleBooleanProperty(true);
		final AtomicReference<String> errorMessage = new AtomicReference<String>();

		final ArrayList<String> queries = constructQuery(GUI.stockGUI.comboList); // Queryn skapas av AlphaVantage-objektet p� basen av ComboBoxarnas v�rden

		GUI.stockGUI.setCursor(Cursor.WAIT);

		GUI.setBusyMessage("Getting data from server", running);

		new Thread(() -> {
			try {
				GUI.stockGUI.jReader1 = new JDataReader(getServerData(queries.get(0)));

				if (queries.size() == 2) {
					// Om b�da stockSymbol-menyerna �r inst�llda att h�mta samma aktie s� h�mtar vi den bara en g�ng 
					if(queries.get(0).equals(queries.get(1))) {
						GUI.stockGUI.jReader2 = null;
					} else {
						// Om anv�ndaren vill ha tv� aktier s� v�ntar vi f�rst en sekund, s� blir inte servern (och dess admin) irriterade f�r att vi floodar
						Thread.sleep(1000); 
						GUI.stockGUI.jReader2 = new JDataReader(getServerData(queries.get(1)));
					}
				} else {
					GUI.stockGUI.jReader2 = null;
				}
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				running.set(false);
				GUI.stockGUI.setCursor(Cursor.DEFAULT);
			}
			Platform.runLater(() -> {
				if (GUI.stockGUI.jReader1 != null) {
					GUI.stockGUI.dataSeries.setItems(GUI.stockGUI.jReader1.getSeriesNames()); // Populera dataSeries-rullgardinen och aktivera den
					GUI.stockGUI.dataSeries.setDisable(false);
					GUI.stockGUI.dataSeriesLabel.setDisable(false);
					if (!GUI.stockGUI.dataSeries.getItems().isEmpty()) {
						GUI.stockGUI.dataSeries.setValue(GUI.stockGUI.dataSeries.getItems().get(0));
					}
					GUI.stockGUI.setTextArea(GUI.stockGUI.jReader1.getTextComparedTo(GUI.stockGUI.jReader2));

					// Om den inladdade aktieportfolien inneh�ller samma aktie(r) som just laddats in s� uppdateras deras v�rden
					Main.portfolio.getInventory().forEach(stock -> {
						if (stock.getStockName().equals(GUI.stockGUI.jReader1.getStockName())) {
							try {
								stock.setCurrentPrice(GUI.stockGUI.jReader1.getLatestValue());
							} catch (Exception e) {
							}
						}
						if (GUI.stockGUI.jReader2 != null) {
							if (stock.getStockName().equals(GUI.stockGUI.jReader2.getStockName())) {
								try {
									stock.setCurrentPrice(GUI.stockGUI.jReader2.getLatestValue());
								} catch (Exception e) {
								}
							}
						}
					});
				} else {
					GUI.stockGUI.setTextArea("No data.");
					if (errorMessage.get() == null) {
						new ErrorMessage("Server returned no data, please try again later.");
					} else {
						new ErrorMessage(errorMessage.get());
					}
				}
			});
		}).start();
	}

	// Getter och setter f�r API-keyn
	@Override
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public void setApiKey(String apiKeyArg) {
		apiKey = "&apikey=" + apiKeyArg;
	}	
}