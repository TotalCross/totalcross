package tc.samples.ui.dynsc;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.DynamicScrollContainer.AbstractView;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/**
 * Test {@link DynamicScrollContainer} and it's ability to display thousands of rows.
 * 
 * Performance is determined by cost of object creation of implemented {@link AbstractView} and calculating {@link AbstractView#getHeight()} and number of views to create.
 * </br>
 * Once determined the scroll container easily handles the scrolling of the components.
 * 
 */
public class DynamicScrollContainerTest extends MainWindow
{

	private Button goButton;
	private Edit evenHeightEdit;
	private Edit oddHeightEdit;
	private Edit rowCountEdit;
	private DynamicScrollContainer vsc;
	private Label statusLabel;
	private Check dynamicChk;

	public void initUI()
	{
		Settings.fingerTouch = true;

		super.initUI();
		Label l = new Label("Number of rows to create: ");
		add(l, LEFT, TOP, PREFERRED, PREFERRED);
		rowCountEdit = new Edit();
		rowCountEdit.setKeyboard(Edit.KBD_NUMERIC);
		add(rowCountEdit, RIGHT, SAME, 80, PREFERRED);
		Label oddHeight = new Label("Odd view height:");
		oddHeightEdit = new Edit();
		oddHeightEdit.setKeyboard(Edit.KBD_NUMERIC);
		add(oddHeight, LEFT, AFTER, PREFERRED, PREFERRED);
		add(oddHeightEdit, RIGHT, SAME, rowCountEdit.getWidth(), PREFERRED);
		Label evenHeight = new Label("Even view height:");
		evenHeightEdit = new Edit();
		evenHeightEdit.setKeyboard(Edit.KBD_NUMERIC);
		add(evenHeight, LEFT, AFTER, PREFERRED, PREFERRED);
		add(evenHeightEdit, RIGHT, SAME, rowCountEdit.getWidth(), PREFERRED);

		Label dynamicHeight = new Label("Dynamic height");
		add(dynamicHeight, LEFT, AFTER, PREFERRED, PREFERRED);
		dynamicChk = new Check("");
		add(dynamicChk, RIGHT, SAME, PREFERRED, PREFERRED);

		goButton = new Button("Generate");
		add(goButton, RIGHT, AFTER, rowCountEdit.getWidth(), PREFERRED);

		statusLabel = new Label("Time to gen:");
		add(statusLabel, LEFT, AFTER, FILL, PREFERRED);

		vsc = new DynamicScrollContainer();
		vsc.setBackColor(Color.WHITE);
		vsc.setBorderStyle(BORDER_SIMPLE);
		add(vsc, LEFT, AFTER + 5, FILL, FILL);

		rowCountEdit.setText("2000");
		oddHeightEdit.setText("30");
		evenHeightEdit.setText("30");
	}

	public void onEvent(Event event)
	{
		if (event.type == ControlEvent.PRESSED && event.target == goButton)
		{
			int rowCount = 1000;
			int oddHeight = 30;
			int evenHeight = 30;
			try
			{
				rowCount = Convert.toInt(rowCountEdit.getText());
			}
			catch (Exception e)
			{
				rowCountEdit.setText(rowCount + "");
			}
			try
			{
				oddHeight = Convert.toInt(oddHeightEdit.getText());
			}
			catch (Exception e)
			{
				oddHeightEdit.setText(oddHeight + "");
			}
			try
			{
				evenHeight = Convert.toInt(evenHeightEdit.getText());
			}
			catch (Exception e)
			{
				evenHeightEdit.setText(evenHeight + "");
			}

			ProgressBox pb = new ProgressBox("Generating", "Creating datasource, please wait...", null);
			pb.popupNonBlocking();
			DynamicScrollContainer.DataSource datasource = new DynamicScrollContainer.DataSource(rowCount);

			int start = Vm.getTimeStamp();
			for (int i = 0; i < rowCount; i++)
			{
				TestView view = new TestView(i, font);
				view.height = i % 2 == 0 ? evenHeight : oddHeight;
				datasource.addView(view);
			}

			pb.unpop();
			vsc.setDataSource(datasource);
			vsc.scrollToView(datasource.getView(0));
			statusLabel.setText("Time to create datasource: " + (Vm.getTimeStamp() - start));
		}
		if (event.type == ControlEvent.PRESSED && event.target == dynamicChk)
		{
			TestView.dynamicHeight = dynamicChk.isChecked();

		}
	}
}
