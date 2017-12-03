package org.jkiss.dbeaver.debug.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.debug.core.DebugCore;
import org.jkiss.dbeaver.ui.UIUtils;

public class DatabaseTab extends AbstractLaunchConfigurationTab {

    private Text datasourceText;
    private Text databaseText;

    /**
     * Modify listener that simply updates the owning launch configuration
     * dialog.
     */
    protected ModifyListener modifyListener = new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent evt)
        {
            scheduleUpdateJob();
        }
    };

    @Override
    public void createControl(Composite parent)
    {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
        comp.setLayout(new GridLayout(1, true));
        comp.setFont(parent.getFont());

        createComponents(comp);
    }

    protected void createComponents(Composite comp)
    {
        createDatasourceComponent(comp);
        createDatabaseComponent(comp);
    }

    protected void createDatasourceComponent(Composite comp)
    {
        Group datasourceGroup = UIUtils.createControlGroup(comp, "Connection", 2, GridData.FILL_HORIZONTAL,
                SWT.DEFAULT);

        datasourceText = UIUtils.createLabelText(datasourceGroup, "Connection", "");
        datasourceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        datasourceText.addModifyListener(modifyListener);
        datasourceText.setEditable(false);
    }

    protected void createDatabaseComponent(Composite comp)
    {
        Group databaseGroup = UIUtils.createControlGroup(comp, "Database", 2, GridData.FILL_HORIZONTAL, SWT.DEFAULT);

        databaseText = UIUtils.createLabelText(databaseGroup, "Database", "");
        databaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        databaseText.addModifyListener(modifyListener);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
    {
        configuration.setAttribute(DebugCore.ATTR_DATASOURCE, DebugCore.ATTR_DATASOURCE_DEFAULT);
        configuration.setAttribute(DebugCore.ATTR_DATABASE, DebugCore.ATTR_DATABASE_DEFAULT);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration)
    {
        initializeDatasource(configuration);
        initializeDatabase(configuration);
    }

    protected void initializeDatasource(ILaunchConfiguration configuration)
    {
        String extracted = DebugCore.extractDatasource(configuration);
        datasourceText.setText(extracted);
    }

    protected void initializeDatabase(ILaunchConfiguration configuration)
    {
        String extracted = DebugCore.extractDatabase(configuration);
        databaseText.setText(extracted);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration)
    {
        configuration.setAttribute(DebugCore.ATTR_DATASOURCE, datasourceText.getText());
        configuration.setAttribute(DebugCore.ATTR_DATABASE, databaseText.getText());
    }

    @Override
    public String getName()
    {
        return "&Main";
    }

}
