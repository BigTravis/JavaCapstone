package edu.orangecoastcollege.cs272.capstone.view;

import java.io.IOException;

import edu.orangecoastcollege.cs272.capstone.controller.Controller;
import edu.orangecoastcollege.cs272.capstone.model.SceneNavigation;
import edu.orangecoastcollege.cs272.capstone.model.SleepLogEntry;
import edu.orangecoastcollege.cs272.capstone.model.User;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SleepLog implements SceneNavigation {

	private static final String FXML_FILE_NAME = "sleep_log.fxml";

	@FXML
	private ProgressIndicator lastEntryPI, averagePI;
	@FXML
	private Button addNewEntryButton, deleteEntryButton;
	@FXML
	private Text sleepHoursByAgeText;
	@FXML
	private Label noSelectedEntryLabel;
	@FXML
	private TableView<SleepLogEntry> sleepLogTV;
	
	private Controller mController;
	private User mUser;
	
	private int mRecommendedHours;
	
	ObservableList<SleepLogEntry> entries;
	
	public void initialize() 
	{
		mUser = mController.getCurrentUser();
		entries = mController.getAllSleepLogEntries();
		sleepLogTV.setItems(entries);
		
		if (mUser != null)
		{
			int userAge = mUser.getAge();
			
			if (userAge <= 2)
				mRecommendedHours = 11;
			else if (userAge <= 5)
				mRecommendedHours = 10;
			else if (userAge <= 13)
				mRecommendedHours = 9;
			else if (userAge <= 17)
				mRecommendedHours = 8;
			else
				mRecommendedHours = 7;
			
			
			sleepHoursByAgeText.setText(mRecommendedHours + " hrs");
		}
		else
		{
			System.err.println("=========> USER UNINITIALIZED. IGNORE IF USING TESTING BYPASS <=========");
		}
		if (entries.size() > 0)
		{
			SleepLogEntry latestEntry = entries.get(entries.size()-1);
			updateSleepGraphs(latestEntry);
		}
		entries.addListener(new ListChangeListener<SleepLogEntry>() {

			@Override
			public void onChanged(Change<? extends SleepLogEntry> c) {
				c.next();
				if (c.wasAdded()) 
				{
					SleepLogEntry latestEntry = c.getAddedSubList().get(0);
					updateSleepGraphs(latestEntry);
				}		
				else if (c.wasRemoved())
				{
					if (entries.size() > 0)
					{
						SleepLogEntry latestEntry = entries.get(entries.size()-1);
						updateSleepGraphs(latestEntry);
					}
					else
					{
						lastEntryPI.setProgress(0.0);
						averagePI.setProgress(0.0);
					}
				}
			}			
		});

	}
	public SleepLog()
	{
		mController = Controller.getInstance();
	}
	
	@Override
	public Scene getView() 
	{
		try {
			BorderPane bp = FXMLLoader.load(getClass().getResource(FXML_FILE_NAME));
			return new Scene(bp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	// TODO Button methods for creating and deleting entries
	@FXML
	private void addNewEntry()
	{
		try
		{
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("addSleepLogEntryForm.fxml"));		
			Pane pane = loader.load();
			
			AddSleepLogEntryForm form = loader.getController();
			stage.setScene(new Scene(pane));
			stage.initStyle(StageStyle.UTILITY);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			
			SleepLogEntry entry = form.getEntry();
			
			entries.add(entry);
		
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	@FXML
	private void deleteSelectedEntry()
	{
		SleepLogEntry selectedEntry = sleepLogTV.getSelectionModel().getSelectedItem();
		entries.remove(selectedEntry);
		mController.deleteSleepLogEntry(selectedEntry);
	}
	
	private void updateSleepGraphs(SleepLogEntry latestEntry)
	{
		lastEntryPI.setProgress(latestEntry.getHoursAsleep()/mRecommendedHours);
		
		double totalSleepHours = 0;
		for (SleepLogEntry e : entries)
			totalSleepHours += e.getHoursAsleep();
		
		averagePI.setProgress((totalSleepHours/entries.size())/mRecommendedHours);
	}


}
