package uppgift3.view;

/* ****************************************************************************************************************************
 * 	Denna klass skapar och uppdaterar programfliken för aktieportföljer.
 */

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import uppgift3.control.EventHandlers;
import uppgift3.model.RoundFix;
import uppgift3.model.Transaction;

import java.util.ArrayList;

import static uppgift3.Main.portfolio;

public class PortfolioGUI {
	public ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
	Node portfolioList;
	static Stage primaryStage;
	protected HBox mainPane = new HBox();
	protected VBox rightPane = new VBox();
	BorderPane listBorder = new BorderPane();
	boolean isSelected = false;

	public PortfolioGUI(Stage primStage) throws Exception {
		primaryStage = primStage;
	}

	public Node draw() {
		mainPane.setOnKeyPressed(event -> EventHandlers.keyTranslator(event));
		// Den nästlade klassen PortfolioList används för att skapa listan över aktierna.
		portfolioList = PortfolioList.makeList();
		portfolioList.setId("portfolioList");

		// Lägg till de olika elementen till deras respektive containers
		rightPane.getChildren().addAll(totalValue(), buttons());
		mainPane.getChildren().addAll(portfolioList, rightPane);

		mainPane.setPadding(new Insets(15, 20, 40, 10));
		mainPane.setSpacing(20);

		return mainPane;
	}

	private Label totalValue() {
		Label toReturn = new Label("Portfolio total value: " + RoundFix.setDecimals(3, portfolio.getTotalValue()) + " euro");
		toReturn.setPadding(new Insets(5, 0, 15, 0));
		return toReturn;
	}

	public void update() {
		// Metod för att uppdatera innehållet i fönstret utan att rita om allting
		portfolioList = PortfolioList.makeList();
		portfolioList.setId("portfolioList");
		int i = mainPane.getChildren().indexOf(mainPane.lookup("#portfolioList"));
		try {
			mainPane.getChildren().remove(i);
			mainPane.getChildren().add(i, portfolioList);
			rightPane.getChildren().remove(0);
			rightPane.getChildren().add(0, totalValue());
		} catch (Exception e) {
			// Trådarna trasslar ihop sig rejält här, och jag kom inte på något bättre sätt att få ordning på dem.
			// Denna "tuggumi och silvertejp" -metod låter helt enkelt trådarna försöka uppdatera portfolielistan,
			// och om någon annan tråd redan håller på med det så får det uppstå en exception som vi i praktiken bara
			// avfärdar.
			return; 
		}
	}

	private Node buttons() {
		// Här skapas portfolieflikens knappar. BigButton är en nästlad klass med denna fliks knappars gemensama egenskaper.
		VBox toReturn = new VBox();
		toReturn.setSpacing(10);

		Button buy = new BigButton("New purchase");
		Button sell = new BigButton("Sell from selected");
		Button updateValues = new BigButton("Update selected");
		Button updateAll = new BigButton("Update all");
		Button removeSelected = new BigButton("Remove selected");

		toReturn.getChildren().addAll(buy, sell, updateValues, updateAll, removeSelected);

		// Händelsehanterare för då användaren trycker på knapparna
		buy.setOnAction(event -> portfolio.buyNew());

		sell.setOnAction(event -> {
			portfolio.setToRemove(new ArrayList<Integer>());
			selectedRows.sort(null);
			selectedRows.forEach(row -> {
				portfolio.sell(row);
			});
			portfolio.remove(portfolio.getToRemove());
		});

		updateValues.setOnAction(event -> {
			new Thread(() -> {
				portfolio.updateValues();
			}).start();
		});

		updateAll.setOnAction(event -> {
			final ArrayList<Integer> savedRows = GUI.portfolioGUI.getSelectedRows();
			GUI.portfolioGUI.selectAllRows();
			new Thread(() -> {
				portfolio.updateValues();
				Platform.runLater(() -> {
					GUI.portfolioGUI.clearSelectedRows();
					GUI.portfolioGUI.setSelectedRows(savedRows);
				});
			}).start();
		});

		removeSelected.setOnAction(event -> removeRows());

		PortfolioList.isEditable.addListener((editable) -> {
			toReturn.getChildren().forEach((node) -> {
				if (node.getClass().getName().contains("Button")) {
					Button button = (Button) node;
					if (!((BooleanProperty) editable).getValue()) {
						button.setOnMousePressed((event) -> {
							button.disarm();
						});
					} else {
						button.setOnMousePressed((event) -> {
							button.arm();
						});
					}
				}
			});
		});

		return toReturn;
	}

	public void removeRows() {
		// Radera markerade aktieköp från portfolien
		if (getSelectedRows().isEmpty()) {
			return;
		}
		String confirmText = "Do you want to remove the " + getSelectedRows().size() + " selected rows?\nThis action can not be undone.";
		if (new ConfirmationMessage(confirmText).get().contains("OK")) {
			portfolio.remove(getSelectedRows());
			clearSelectedRows();
			GUI.portfolioGUI.update();
		}
	}

	public void setSelectedRows(ArrayList<Integer> setRows) {
		// Metod för att markera de rader som finns i setRows. Används för att återställa användarens markeringar
		// efter att en updatering av samtliga aktiekurser i portfolien gjorts.
		clearSelectedRows();
		setRows.forEach(index -> {
			selectedRows.add(index);
		});
	}

	// Välj alla rader. Görs med CTRL+A eller genom att klicka på den översta checkboxen i portfolielistningen.
	public void selectAllRows() {
		clearSelectedRows();
		for (int i = 0; i < portfolio.getInventory().size(); i++) {
			selectedRows.add(i);
		}
	}

	// Returnera en lista på de markerade raderna till klientkoden
	public ArrayList<Integer> getSelectedRows() {
		return new ArrayList<Integer>(selectedRows);
	}

	// Avmarkera alla rader. CTRL+N eller klicka på översta checkboxen i portfolielistningen
	public void clearSelectedRows() {
		selectedRows.clear();
	}

	// Editering av portfolielistan skall inte vara möjlig då aktiekurser hämtas
	public void setEditable(boolean value) {
		PortfolioList.isEditable.set(value);
	}

	// Metod för att ändra på muspekaren t.ex. då aktiedata hämtas från servern
	public void setCursor(Cursor cursor) {
		mainPane.setCursor(cursor);
	}

	private static class PortfolioList {
		public static BooleanProperty isEditable = new SimpleBooleanProperty(true);

		public static Node makeList() {
			VBox master = new VBox();
			ScrollPane root = new ScrollPane();
			VBox stockList = new VBox();

			root.setContent(stockList);

			master.maxWidthProperty().bind(stockList.prefWidthProperty());
			root.prefHeightProperty().bind(GUI.portfolioGUI.mainPane.heightProperty().subtract(30));
			root.setHbarPolicy(ScrollBarPolicy.NEVER);
			root.setStyle("-fx-background: #FFFFFF;-fx-border-color: #FFFFFF;");

			stockList.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			stockList.setPrefHeight(primaryStage.getHeight() / 4);
			stockList.setPrefWidth(primaryStage.getWidth() - 300);

			if (portfolio != null) {
				portfolio.getInventory().forEach(item -> {
					stockList.getChildren().add(formatListRow(item));
				});
			}

			master.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, BorderStroke.THIN)));
			master.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			master.getChildren().addAll(firstRow(), new Separator(), root);
			master.setPadding(new Insets(0, 0, 0, 10));

			return master;
		}

		private static HBox firstRow() {
			CheckBox selectBox = formatCheckBox();
			selectBox.setTranslateY(7);
			selectBox.setOnAction(event -> {
				if (selectBox.isSelected()) {
					GUI.portfolioGUI.selectAllRows();
				} else {
					GUI.portfolioGUI.clearSelectedRows();
				}
			});

			isEditable.addListener((editable) -> {
				selectBox.setDisable(!((BooleanProperty) editable).getValue());
			});

			ArrayList<Text> textArray = new ArrayList<Text>();
			textArray.add(new Text("Row ID"));
			textArray.add(new Text("Stock"));
			textArray.add(new Text("Amount"));
			textArray.add(new Text("Purchase date"));
			textArray.add(new Text("Purchase\nunit price"));
			textArray.add(new Text("Purchase\ntotal value"));
			textArray.add(new Text("Current\nunit price"));
			textArray.add(new Text("Current\ntotal value"));
			textArray.forEach(element -> formatElement(element));

			HBox toReturn = new HBox();
			toReturn.setAlignment(Pos.CENTER_LEFT);
			toReturn.setPrefHeight(50);
			toReturn.getChildren().add(selectBox);
			toReturn.getChildren().addAll(textArray);
			toReturn.setPadding(new Insets(0, 0, 0, 2));
			return toReturn;
		}

		private static HBox formatListRow(Transaction purchase) {
			CheckBox selectBox = formatCheckBox();

			if (GUI.portfolioGUI.selectedRows.contains(portfolio.getInventory().indexOf(purchase))) {
				selectBox.setSelected(true);
			}

			selectBox.setOnAction(event -> {
				if (selectBox.isSelected()) {
					GUI.portfolioGUI.selectedRows.add(portfolio.getInventory().indexOf(purchase));
				} else if (!selectBox.isSelected()) {
					GUI.portfolioGUI.selectedRows.remove((Object) portfolio.getInventory().indexOf(purchase));
				}
				return;
			});

			GUI.portfolioGUI.selectedRows.addListener((ListChangeListener<? super Integer>) index -> {
				if (index.getList().contains(portfolio.getInventory().indexOf(purchase))) {
					selectBox.setSelected(true);
				} else {
					selectBox.setSelected(false);
				}
			});

			ArrayList<Text> textArray = new ArrayList<Text>();
			textArray.add(new Text(Integer.toString(portfolio.getInventory().indexOf(purchase) + 1)));
			textArray.add(new Text(purchase.getStockName()));
			textArray.add(new Text(Integer.toString(purchase.getAmount())));
			textArray.add(new Text(purchase.getPurchaseDate().toString()));
			textArray.add(new Text(Double.toString(purchase.getPurchaseUnitPrice())));
			textArray.add(new Text(Double.toString(purchase.getPurchaseTotalValue())));
			textArray.add(new Text(Double.toString(purchase.getCurrentUnitPrice())));
			textArray.add(new Text(Double.toString(RoundFix.setDecimals(3, purchase.getCurrentTotalValue()))));
			textArray.forEach(element -> formatElement(element));

			isEditable.addListener((editable) -> selectBox.setDisable(!((BooleanProperty) editable).getValue()));

			HBox toReturn = new HBox();
			toReturn.setAlignment(Pos.CENTER_LEFT);
			selectBox.setTranslateY(5);

			toReturn.setOnMousePressed(event -> {
				selectBox.arm();
			});
			toReturn.setOnMouseReleased(event -> {
				if (event.getY() > 0 && event.getY() < toReturn.getHeight()) {
					selectBox.fire();
				} else {
					selectBox.disarm();
				}
			});
			toReturn.setOnMouseEntered(
					event -> toReturn.setBackground(new Background(new BackgroundFill(Color.gray(0.9), null, null))));
			toReturn.setOnMouseExited(
					event -> toReturn.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null))));

			toReturn.getChildren().add(selectBox);
			toReturn.getChildren().addAll(textArray);
			return toReturn;

		}

		private static Text formatElement(Text element) {
			element.setTextAlignment(TextAlignment.CENTER);
			element.setWrappingWidth(100);
			element.setStyle("-fx-background-color: transparent");
			return element;
		}

		private static CheckBox formatCheckBox() {
			CheckBox toReturn = new CheckBox();
			toReturn.setPadding(new Insets(0, 10, 10, 0));
			return toReturn;
		}
	}

	private class BigButton extends Button {
		public BigButton(String caption) {
			super(caption);
			setPrefSize(250, 0);
			setAlignment(Pos.CENTER);
			setTextAlignment(TextAlignment.CENTER);
		}
	}

	public void setSelected(boolean value) {
		isSelected = value;
	}

	public boolean isSelected() {
		return isSelected;
	}
}
