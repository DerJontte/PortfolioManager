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
 *  En instans av klassen Transaction omfattar ett enskilt aktieköp.
 * 
 * 	En för varje aktieköp sparas information om: 
 * 		- Aktienamnets kod
 * 		- Antal köpta aktier
 * 		- Pris per aktie
 * 		- Inköpets totalvärde
 * 		- Affärens datum
 *		- Ett identifieringsnummer
 * 
 *  Klassen har också variabler för uppdaterat enhetspris och uppdaterat totalvärde. Dessa kan både anges då transaktionen
 *  skapas och uppdateras senare.
 */

public class Transaction implements Comparator<Transaction>, Serializable {
	// Property- och andra observable-datatyper är praktiska då vi vill kunna skapa lyssnare för t.ex. förändringar i värde för 
	// eller antal av aktier i portfolien. Dessa (och vissa andra) datatyper går inte att serialiseras, så med modifieraren 
	// "transient" berättar vi åt programmet att dessa variabler inte skall sparas då vi skriver objektet till en fil. För att 
	// kunna spara innehållet i variablerna skapar vi variabler i primitiva datatyper och kopierar innehållet från de 
	// icke-serialiserbara variablerna då användaren sparar portfolien. Då en portfolie läses in från hårddisken kopieras 
	// innehållet från de primitiva datatyperna tillbaka till de observerbara variablerna.
	
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
		
		// Konstruktorn tar in aktienamnet, datum för köpet, antal och köpepris.
		// Lyssnare skapas för de variablerna som kan ändras efterät: aktiernas antal och aktuellt styckespris. När
		// nåndera av dem ändras så uppdateras portfolie-fliken med de nya värdena.
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
	
		// Metod so räknar ut det nya totalvärdet på en transaktion
		public void updateTotalValue() {
			currentTotalValue = currentUnitPrice.get() * amount.get();
		}

		public void toSave() {
			// Då transaktionerna sparas tillsammans med portfolien, kopieras de icke-serialiserbara variablernas innehåll
			// till primitiva variabler som går att spara till en fil.
			serializedAmount = amount.get();
			serializedUnitPrice = currentUnitPrice.get();
		}

		public Transaction fromSave() {
			// Då transaktionerna laddas från en fil kopieras de primitiva variablernas innehåll till de icke-serialiserbara
			// variablerna så att det går att skapa lyssnare för dem.
			amount = new SimpleIntegerProperty(serializedAmount);
			currentUnitPrice = new SimpleDoubleProperty(serializedUnitPrice);
			return this;
		}
		
		@Override
		public int compare(Transaction p1, Transaction p2) {
			// Denna metod gör att portfolien (och alla andra listor med Transaction-variabler likaså) går att sortera.
			return (p1.stockName).compareTo(p2.stockName);
		}
	}