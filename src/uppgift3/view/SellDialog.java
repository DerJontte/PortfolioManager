package uppgift3.view;

/* ****************************************************************************************************************************
 *  Denna klass skapar en dialogruta för att sälja aktier från portfolien. Då användaren matat in antal aktier och bekräftat
 *  försäljningen, räknas vinsten/förlusten ut och berättas åt användaren. Portfoliens innehåll och programfönstrets aktielista
 *  uppdateras givetvis också, men detta tar den anropande metoden hand om.
 */

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import uppgift3.control.EventHandlers;
import uppgift3.model.RoundFix;
import uppgift3.model.Transaction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static uppgift3.Main.portfolio;

public class SellDialog {
	Transaction purchase;
	
	public SellDialog(Transaction purchase) {
		this.purchase = purchase;
	}
	
	public Transaction show() {
		TextInputDialog dialog = new TextInputDialog();
		String profitLoss = new String("profit");
		AtomicBoolean validInput = new AtomicBoolean(false);
		
		dialog.dialogPaneProperty().get().setOnKeyPressed(event -> EventHandlers.keyTranslator(event));
		dialog.setTitle("Sell stock");
		dialog.setHeaderText(null);
		dialog.setContentText("How many shares would you like to sell from post " + (portfolio.getInventory().indexOf(purchase) + 1) + ", " + purchase.getStockName() + "?\n");
		while (!validInput.get()) {
			((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setOnAction(event -> validInput.set(true));
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				try {
					int amount = Integer.valueOf(result.get());

					if (amount < 0) {
						throw new Exception("You can't sell a negative number of shares.\nYou may want to \"buy\" instead.");
					}

					if (amount > purchase.getAmount()) {
						throw new Exception("This application does not support futures.\nPlease sell only stuff you actually own!");
					}

					double profit = (purchase.getCurrentUnitPrice() - purchase.getPurchaseUnitPrice()) * amount;
					if (profit < 0) {
						profitLoss = "loss";
					}
					purchase.setAmount(purchase.getAmount() - amount);
					purchase.updateTotalValue();
					new InformationMessage(result.get() + " units of " + purchase.getStockName() + " sold with a net " + profitLoss + " of " + RoundFix.setDecimals(3, Math.abs(profit)) + " euros.");
					validInput.set(true);
				} catch (Exception e) {
					new ErrorMessage("Error: " + e.getMessage());
					validInput.set(false);
				}
			}
		}
		return purchase;
	}
}
