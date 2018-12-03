package uppgift3.model;

// Detta är interfacet för klasser som läser in och delar upp aktiedata till mindre objekt.
// Flera servrar kan läggas till genom att implementera interfacets metoder med parsing för deras API och dataformat.
// JDataReader-klassen borde dock göras mer generisk och/eller serverspecifika JSON-datatolkar läggas till för att
// försäkra att även andra servrar fungerar som de skall.

import javafx.scene.control.ComboBox;

import java.util.ArrayList;

public interface NetworkInterface {
	String getServerData(String query) throws Exception;
	ArrayList<String> constructQuery(ArrayList<ComboBox<String>> comboList);
	void fetchData() throws Exception;
	void setApiKey(String apiKeyArg);
	String getApiKey();
}