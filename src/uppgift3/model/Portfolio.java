package uppgift3.model;

/* ****************************************************************************************************************************
 * 	Instanser av denna klass skapar objekt f�r aktieportf�ljer och inneh�ller metoder f�r att uppdatera portf�ljerna. 
 */

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import uppgift3.Main;
import uppgift3.view.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static uppgift3.Main.portfolio;

public class Portfolio extends Thread implements Serializable {
	// �ven h�r har vi icke-serialiserbara transient-variabler som m�ste skrivas om till andra datatyper d� vi sparar och
	// �ppnar portfolier. En del variabler (t.ex. toRemove, listan p� rader som skall raderas) sparas inte alls.
	private transient ArrayList<Integer> toRemove = new ArrayList<Integer>();
	private transient ObservableList<Transaction> inventory = FXCollections.observableArrayList();
	transient BooleanProperty updated = new SimpleBooleanProperty();
	private ArrayList<Transaction> serializedInventory = new ArrayList<Transaction>();
	private static final long serialVersionUID = 1L;

	public Portfolio() {
		// L�gg till en lyssnare som reagerar p� f�r�ndringar i portfolien och uppdaterar programf�nstret
		getInventory().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				GUI.portfolioGUI.update();
			}
		});		
	}

	// Tv� add-metoder f�r att l�gga till nya aktiek�p i inventoryn. Ena tar in ett f�rdigt transaktionsobjekt,
	// och den senare metoden explicit tar in parametrarna i konstruktorn.
	public void add(Transaction purchase) {
		getInventory().add(purchase);
	}

	public void add(String name, LocalDate date, int amount, double unitPrice) {
		getInventory().add(new Transaction(name, date, amount, unitPrice));
	}

	// Metod f�r att s�lja aktier. Metoden skapar en instans av SellDialog-klassen som visar en popup dit anv�ndaren kan
	// fylla i hur m�nga aktier som skall s�ljas. Om aktiernas antal blir noll s� kan anv�ndaren v�lja mellan att beh�lla
	// eller ta bort raden ur portfolielistan.
	public void sell(int index) {
		Transaction t = new SellDialog(portfolio.getInventory().get(index)).show();
		getInventory().set(index, t);
		if (t.getAmount() == 0) {
			String a = (new ConfirmationMessage("No shares left of this transaction.\nDo You want to delete row?")).get();
			if (a.contains("OK")) {
				getToRemove().add(index);
			}
		}
	}

	// remove() tar bort rader utan att r�kna ut f�rs�ljningsvinst/f�rlust
	public void remove(ArrayList<Integer> removeList) {
		if (removeList == null) {
			return;
		}

		// Remove-listan sorteras att b�rja fr�n h�gsta numret s� att vi kan ta bort rader i tur och ordning.
		// D� vi b�rjar med det st�rsta numret s� st�mmer indexen f�r efterf�ljande rader som skall tas bort.
		ArrayList<Integer> selectedRows = GUI.portfolioGUI.getSelectedRows();
		removeList.sort(Collections.reverseOrder());

		removeList.forEach(number -> {
			int index = number.intValue();
			portfolio.getInventory().remove(index);
			if (selectedRows.contains(index)) {
				GUI.portfolioGUI.selectedRows.remove((Object)index);
			}
			selectedRows.forEach(row -> {
				int rowValue = row.intValue();
				if (rowValue > index) {
					GUI.portfolioGUI.selectedRows.remove((Object)rowValue);
					GUI.portfolioGUI.selectedRows.add(rowValue - 1);
				}
			});
		});
		GUI.portfolioGUI.update();
		removeList.clear();
	}

	public void buyNew() {
		new BuyNewDialog();
	}

	public double getTotalValue() {
		// R�kna ut och returnera det totala v�rdet p� portfolien
		AtomicReference<Double> toReturn = new AtomicReference<Double>(0.0);
		getInventory().forEach(purchase -> {
			toReturn.set(toReturn.get() + purchase.getCurrentTotalValue());
		});
		return toReturn.get();
	}

	public ArrayList<String> getSymbolList() {
		// Skapa och returnera till anv�ndaren en lista �ver alla aktier som finns i en portfolie. Dubletter kan f�rekomma.
		ArrayList<String> toReturn = new ArrayList<String>();
		getInventory().forEach(purchase -> toReturn.add(purchase.getStockName()));
		return toReturn;		
	}

	public ArrayList<String> getUniqueList() {
		// Skapa och returnera till anv�ndaren en lista �ver unika aktier som finns i en portfolie. Dubletter tas inte med.
		ArrayList<String> toReturn = new ArrayList<String>();
		GUI.portfolioGUI.getSelectedRows().forEach(index -> {
			String name = getInventory().get(index).getStockName();
			if (!toReturn.contains(name)) {
				toReturn.add(name);	
			}
		});
		return toReturn;		
	}

	public boolean isEmpty() {
		return getInventory().isEmpty();
	}

	public void updateValues() {
		// Denna metod  uppdaterar v�rdena f�r aktierna i den inladdade portfolien. En akties aktuella v�rde f�s fr�n det
		// senaste v�rdet i intraday-serien. Server-queryn �r �n s� l�nge h�rdkodad.
		final AtomicBoolean updateError = new AtomicBoolean(false);
		final AtomicReference<String> errorMessage = new AtomicReference<String>("");
		final AtomicInteger pending = new AtomicInteger();
		final AtomicInteger updatesOK = new AtomicInteger(0);
		final AtomicInteger updatesNOK = new AtomicInteger(0);

		if (GUI.portfolioGUI.getSelectedRows().isEmpty()) {
			return;
		}
		
		GUI.portfolioGUI.setEditable(false);
		GUI.portfolioGUI.setCursor(Cursor.WAIT); // Muspekaren �ndras s� att anv�ndaren ser att n�t h�nder

		// Variabeln pending h�ller reda p� hur m�nga aktier det �r kvar att h�mta.
		pending.set(getUniqueList().size());

		getUniqueList().forEach(stockName -> {
			final String query = "query?function=TIME_SERIES_INTRADAY&symbol=" + stockName + "&interval=1min" + Main.networking.getApiKey();
			new Thread(() -> {
				GUI.setStatusMessage("Updating portfolio stock values: " + pending.get() + " unique stocks pending");
				try {
					JDataReader jReader = new JDataReader(Main.networking.getServerData(query));
					final double latestValue = jReader.getLatestValue();
					portfolio.getInventory().forEach(purchase -> {
						if (purchase.getStockName().equals(stockName)) {
							purchase.setCurrentPrice(latestValue);
						}
					});
					updatesOK.incrementAndGet();
				} catch (Exception e) {
					updateError.set(true);
					errorMessage.set("Symbol \"" + stockName + "\"\n" + e.toString() + "\n" + errorMessage.get() +"\nPossible cause: an invalid stock symbol or network error.\nPlease try again later!");
					updatesNOK.incrementAndGet();
				} finally {
					pending.decrementAndGet();
				}
				Platform.runLater(() -> {		// Varje tr�d f�r en runlater-funktion, men endast den tr�d som �r sist f�rdig
					if (pending.get() == 0) {	// och som s�tter r�knaren till noll exekverar detta kodblock.
						String errorsText = new String();
						switch (updatesNOK.get()) {
						case 0:
							errorsText = "no errors.";
							break;
						case 1:
							errorsText = "1 error.";
							break;
						default:
							errorsText = updatesNOK.get() + " errors.";
						}
						GUI.portfolioGUI.update();
						GUI.portfolioGUI.setEditable(true);
						GUI.portfolioGUI.setCursor(Cursor.DEFAULT);
						GUI.setStatusMessage((updatesOK.get() + updatesNOK.get()) + " stock values updated with " + errorsText);
						if (updateError.getAndSet(false)) {
							new ErrorMessage("Error updating values: " + errorMessage);
						}
					}
				});
			}).start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	
	public Portfolio toSave() {
		// Kopiera icke-serialiserbara variablers inneh�ll till variabler som kan serialiseras (och d�rigenom ochs� sparas 
		// till en fil)
		serializedInventory = new ArrayList<Transaction>(getInventory());
		serializedInventory.forEach(purchase -> {
			purchase.toSave();
		});
		return this;
	}

	public Portfolio fromSave() {
		// Kopiera inladdade serialiserade variabler till variabler av datatyper som l�mpar sig f�r multithreading.
		setInventory(FXCollections.observableArrayList(serializedInventory));
		getInventory().forEach(purchase -> {
			purchase = purchase.fromSave();
		});
		return this;
	}

	public ArrayList<Integer> getToRemove() {
		return toRemove;
	}

	public void setToRemove(ArrayList<Integer> toRemove) {
		this.toRemove = toRemove;
	}

	public ObservableList<Transaction> getInventory() {
		return inventory;
	}

	public void setInventory(ObservableList<Transaction> inventory) {
		this.inventory = inventory;
	}
}
