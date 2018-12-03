package uppgift3.view;

/* ************************************************************************************************************************
 * 	Denna klass visar ett popupfönster för att göra ett nytt aktieköp till portfolien.
 * 
 * 	Användaren kan mata in aktiens kod, pris, antal och datum för köpet. Det går även att skriva in koden och välja att
 * 	validera den, varvid programmet sköer upp koden och det valda datumets värde på aktien. Om en ogiltig kod givits, 
 * 	informerar programmet om detta.
 * 
 * 	Efter att användaren valt att köpa ett givet antal aktier uppdateras portfolien automatiskt.
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import uppgift3.Main;
import uppgift3.control.EventHandlers;
import uppgift3.model.JDataReader;
import uppgift3.model.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static uppgift3.Main.portfolio;

public class BuyNewDialog {
	private AtomicBoolean validated = new AtomicBoolean(false);
	private ArrayList<FormattedBox<?>> boxArray = new ArrayList<FormattedBox<?>>();
	private VBox root = new VBox();
	
	public BuyNewDialog() {
		// Lambdauttryck och trådar kräver i många fall variabler deklarerade som final. Atomic-datatyper lämpar sig
		// att användas eftersom deras innehåll kan ändras även om de är deklarerade som final.
		//
		// Det går också att använda Observable-datatyper (som har inbyggt stöd för lyssnare), men enligt dokumentationen är
		// de inte lika lämpade för multithreading som atomära typer.
		
		final AtomicReference<Double> currentPrice = new AtomicReference<Double>();
		final AtomicReference<Double> purchasePrice = new AtomicReference<Double>();

		Stage stage = new Stage();
		Scene scene = new Scene(root, 375, 375);
		stage.setScene(scene);
		stage.setTitle("Buy new stock");
		stage.setResizable(false);

		// Den egna keyhandlern (som t.ex. tillåter enter att användas som, ja, enter, i användargränssnittet) måste explicit 
		// läggas till varje scen (i praktiken alltså varje fönster och popup) som den behövs .
		scene.setOnKeyPressed(event -> EventHandlers.keyTranslator(event));

		root.setSpacing(20);
		root.setPadding(new Insets(20, 20, 35, 20));
		root.setAlignment(Pos.BASELINE_CENTER);

		// FormattedBox och FormattedButton är nästlade klasser med generisk layout-data specifikt för denna klass.
		FormattedBox<String> stockInput = new FormattedBox<>("Stock Symbol");
		FormattedBox<Double> amountInput = new FormattedBox<>("Number of shares");
		FormattedBox<Double> priceInput = new FormattedBox<>("Price per share");

		// Datumväljare för aktieköpet.
		// Om et datum i framtiden väljs, så returnerar en lyssnare datumet till dagens datum.
		DatePicker transactionDate = new DatePicker();
		transactionDate.valueProperty().addListener(listener -> {
			if (transactionDate.getValue().isAfter(LocalDate.now())) {
				transactionDate.setValue(LocalDate.now());
			}
		});
		transactionDate.setValue(LocalDate.now());

		FormattedButton validateButton = new FormattedButton("Validate stock and get value");
		FormattedButton buyButton = new FormattedButton("Buy without closing dialog");
		FormattedButton buyCloseButton = new FormattedButton("Buy and close dialog");
		FormattedButton justClose = new FormattedButton("Close dialog without buying");

		// Valideringen görs med en hårdkodad query. 
		// TODO: Detta bör ändras om möjligt.
		validateButton.setOnAction(event -> {
			String query = "query?function=TIME_SERIES_DAILY&symbol=" + stockInput.getText() + "&outputsize=full" + Main.networking.getApiKey();
			try {
				JDataReader jReader = new JDataReader(Main.networking.getServerData(query));
				currentPrice.set(jReader.getLatestValue());
				purchasePrice.set(jReader.getOlderValue(transactionDate.getValue().toString()));
				stockInput.setText(stockInput.getText().toUpperCase());
				priceInput.setText(purchasePrice.get());
				validated.set(true);
				new InformationMessage("Success! Stock validated.");
			} catch (Exception e) {
				new ErrorMessage("Could not validate stock name.\nMake sure you entered a valid name and\nhave a working internet connection.");
			} 
		});

		// Då användaren trycker på endera köp-knappen kollar programmet att alla fält är ifyllda och visar ett felmeddelande
		// om något saknas.
		buyButton.setOnAction(event -> {
			if (!validInput()) {
				new ErrorMessage("Invalid input, please fill in all fields!");
				return;
			}
			String choice = new ConfirmationMessage(
					"Confirm purchase of " + amountInput.getInteger() + " shares of " + stockInput.getText()).get();
			if (choice.contains("OK")) {
				Transaction purchase = new Transaction(stockInput.getText(), transactionDate.getValue(),
						amountInput.getInteger(), Double.valueOf(priceInput.getText()));
				if (validated.get()) {
					purchase.setCurrentPrice(currentPrice.get());
				}
				// Om allt är OK så läggs transaktionen till portfolien och mellanbladet med portfolielistan uppdateras.
				portfolio.add(purchase);
				GUI.portfolioGUI.update();
			}
		});

		// "Buy and close"-knappen triggar "buy without closing"-knappen och stänger därefter popupfönstret.
		buyCloseButton.setOnAction(event -> {
			buyButton.fire();
			if (validInput()) {
				stage.close();
			}
		});

		justClose.setCancelButton(true);
		justClose.setOnAction(event -> stage.close());

		root.getChildren().addAll(stockInput, amountInput, priceInput, transactionDate, validateButton, buyCloseButton,
				buyButton, justClose);

		stage.show();
	}
	
	// validInput kollar att alla fält är ifyllda och returnerar true omm de är det.
	private boolean validInput() {
		for (FormattedBox<?> b : boxArray) {
			if (b.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	// Nästlade klasser med textboxar och knappar formaterade specifikt för detta fönster.
	private class FormattedBox<T> extends HBox {
		private TextField userInput = new TextField();

		public FormattedBox(String descriptArg) {
			setMaxWidth(220);
			setAlignment(Pos.CENTER);
			setSpacing(20);
			Label label = new Label(descriptArg);
			label.setPrefWidth(100);
			userInput.setPrefWidth(80);
			getChildren().addAll(label, userInput);
			boxArray.add(this);
		}

		public void setText(Object newText) {
			userInput.setText(newText.toString());
		}

		public String getText() {
			return userInput.getText().toUpperCase();
		}

		public int getInteger() {
			return Integer.parseInt(userInput.getText());
		}

		public boolean isEmpty() {
			return userInput.getText().isEmpty();
		}
	}

	private class FormattedButton extends Button {
		public FormattedButton(String caption) {
			super(caption);
			setPrefWidth(root.getWidth());
			setAlignment(Pos.CENTER);
			setTextAlignment(TextAlignment.CENTER);
		}
	}
}
