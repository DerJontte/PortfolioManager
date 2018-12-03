package uppgift3.model;

// Detta �r interfacet f�r klasser som l�ser in och delar upp aktiedata till mindre objekt.
// Flera servrar kan l�ggas till genom att implementera interfacets metoder med parsing f�r deras API och dataformat.
// JDataReader-klassen borde dock g�ras mer generisk och/eller serverspecifika JSON-datatolkar l�ggas till f�r att
// f�rs�kra att �ven andra servrar fungerar som de skall.

import javafx.scene.control.ComboBox;

import java.util.ArrayList;

public interface NetworkInterface {
	String getServerData(String query) throws Exception;
	ArrayList<String> constructQuery(ArrayList<ComboBox<String>> comboList);
	void fetchData() throws Exception;
	void setApiKey(String apiKeyArg);
	String getApiKey();
}