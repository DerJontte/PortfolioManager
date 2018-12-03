package uppgift3.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uppgift3.view.GUI;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;

/* ******************************************************************************************************************************
 *  En instans av klassen Transaction omfattar ett enskilt aktiek�p.
 * 
 * 	En f�r varje aktiek�p sparas information om: 
 * 		- Aktienamnets kod
 * 		- Antal k�pta aktier
 * 		- Pris per aktie
 * 		- Ink�pets totalv�rde
 * 		- Aff�rens datum
 *		- Ett identifieringsnummer
 * 
 *  Klassen har ocks� variabler f�r uppdaterat enhetspris och uppdaterat totalv�rde. Dessa kan b�de anges d� transaktionen
 *  skapas och uppdateras senare.
 */

public class Transaction implements Comparator<Transaction>, Serializable {
	// Property- och andra observable-datatyper �r praktiska d� vi vill kunna skapa lyssnare f�r t.ex. f�r�ndringar i v�rde f�r 
	// eller antal av aktier i portfolien. Dessa (och vissa andra) datatyper g�r inte att serialiseras, s� med modifieraren 
	// "transient" ber�ttar vi �t programmet att dessa variabler inte skall sparas d� vi skriver objektet till en fil. F�r att 
	// kunna spara inneh�llet i variablerna skapar vi variabler i primitiva datatyper och kopierar inneh�llet fr�n de 
	// icke-serialiserbara variablerna d� anv�ndaren sparar portfolien. D� en portfolie l�ses in fr�n h�rddisken kopieras 
	// inneh�llet fr�n de primitiva datatyperna tillbaka till de observerbara variablerna.
	
		private static final long serialVersionUID = 1L;
		private String stockName;
		private LocalDate purchaseDate;
		transient private IntegerProperty amount = new SimpleIntegerProperty(0);
		private int serializedAmount;
		private double purchaseUnitPrice;
		private double purchaseTotalValue;
		transient private DoubleProperty currentUnitPrice = new SimpleDoubleProperty(0.0);
		private double serializedUnitPrice;
		private double currentTotalValue;
		
		// Konstruktorn tar in aktienamnet, datum f�r k�pet, antal och k�pepris.
		// Lyssnare skapas f�r de variablerna som kan �ndras efter�t: aktiernas antal och aktuellt styckespris. N�r
		// n�ndera av dem �ndras s� uppdateras portfolie-fliken med de nya v�rdena.
		public Transaction(String name, LocalDate date, int howMany, double unitPrice) {
			stockName = name;
			purchaseDate = date;
			amount.set(howMany);
			purchaseUnitPrice = unitPrice;
			purchaseTotalValue = RoundFix.setDecimals(3, unitPrice * howMany);

			currentUnitPrice.addListener(event -> {
				updateTotalValue();
				GUI.portfolioGUI.update();
			});
			
			amount.addListener(event -> {
				GUI.portfolioGUI.update();
			});
		}
		
		// Getters
		
		public int getAmount() {
			return amount.get();
		}
		
		public String getStockName() {
			return stockName;
		}
		
		public LocalDate getPurchaseDate() {
			return purchaseDate;
		}
		
		public double getPurchaseUnitPrice() {
			return purchaseUnitPrice;
		}
		
		public double getPurchaseTotalValue() {
			return purchaseTotalValue;
		}
		
		public double getCurrentUnitPrice() {
			return currentUnitPrice.get();
		}
		
		public double getCurrentTotalValue() {
			return currentTotalValue;
		}

		// Setters
		
		public void setAmount(int newAmount) {
			amount.set(newAmount);
		}
		
		public void setCurrentPrice(double newPrice) {
			currentUnitPrice.set(newPrice);
		}
	
		// Metod so r�knar ut det nya totalv�rdet p� en transaktion
		public void updateTotalValue() {
			currentTotalValue = currentUnitPrice.get() * amount.get();
		}

		public void toSave() {
			// D� transaktionerna sparas tillsammans med portfolien, kopieras de icke-serialiserbara variablernas inneh�ll
			// till primitiva variabler som g�r att spara till en fil.
			serializedAmount = amount.get();
			serializedUnitPrice = currentUnitPrice.get();
		}

		public Transaction fromSave() {
			// D� transaktionerna laddas fr�n en fil kopieras de primitiva variablernas inneh�ll till de icke-serialiserbara
			// variablerna s� att det g�r att skapa lyssnare f�r dem.
			amount = new SimpleIntegerProperty(serializedAmount);
			currentUnitPrice = new SimpleDoubleProperty(serializedUnitPrice);
			return this;
		}
		
		@Override
		public int compare(Transaction p1, Transaction p2) {
			// Denna metod g�r att portfolien (och alla andra listor med Transaction-variabler likas�) g�r att sortera.
			return (p1.stockName).compareTo(p2.stockName);
		}
	}