/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.studio.openldap.config.editor.dialogs;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.common.ui.model.AllowFeatureEnum;


/**
 * The AllowFeatureDialog is used to select one feature to allow. The possible
 * features are :
 * <ul>
 * <li>bind_v2</li>
 * <li>bind_anon_cred</li>
 * <li>bind_anon_dn</li>
 * <li>update_anon</li>
 * <li>proxy_authz_anon</li>
 * </ul>
 * 
 * The dialog overlay is like :
 * 
 * <pre>
 * +----------------------------+
 * | Allowed feature            |
 * | .------------------------. |
 * | | bind_v2 :          [ ] | |
 * | | bond_anon_cred :   [ ] | |
 * | | bind_anon_dn :     [ ] | |
 * | | update_anon :      [ ] | |
 * | | proxy_authz_anon : [ ] | |
 * | '------------------------' |
 * |                            |
 * |  (Cancel)            (OK)  |
 * +----------------------------+
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AllowFeatureDialog extends AddEditDialog<AllowFeatureEnum>
{
    /** The array of buttons */
    private Button[] allowFeatureCheckboxes = new Button[5];
    
    /** The already selected allowed features */
    List<AllowFeatureEnum> features = new ArrayList<AllowFeatureEnum>();
    
    /**
     * Create a new instance of the AllowFeatureDialog
     * 
     * @param parentShell The parent Shell
     */
    public AllowFeatureDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }
    
    
    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AllowFeature.Title" ) );
    }
    

    /**
     * The listener in charge of exposing the changes when some checkbox is selected
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();
            
            if ( object instanceof Button )
            {
                Button selectedCheckbox = (Button)object;
                
                for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == allowFeatureCheckboxes[i] )
                    {
                        setEditedElement( AllowFeatureEnum.getFeature( i ) );
                    }
                    else if ( allowFeatureCheckboxes[i].isEnabled() )
                    {
                        allowFeatureCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    /**
     * Create the Dialog for AllowFeature :
     * <pre>
     * +----------------------------+
     * | Allowed feature            |
     * | .------------------------. |
     * | | bind_v2 :          [ ] | |
     * | | bond_anon_cred :   [ ] | |
     * | | bind_anon_dn :     [ ] | |
     * | | update_anon :      [ ] | |
     * | | proxy_authz_anon : [ ] | |
     * | '------------------------' |
     * |                            |
     * |  (Cancel)            (OK)  |
     * +----------------------------+
     * </pre>
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );
        
        createAllowFeatureEditGroup( composite );
        initDialog();
        
        applyDialogFont( composite );
        
        return composite;
    }


    /**
     * Creates the AllowFeature input group. This is the part of the dialog
     * where one can insert the TimeLimit values
     * 
     * <pre>
     * Allowed feature
     * .------------------------.
     * | bind_v2 :          [ ] |
     * | bond_anon_cred :   [ ] |
     * | bind_anon_dn :     [ ] |
     * | update_anon :      [ ] |
     * | proxy_authz_anon : [ ] |
     * '------------------------'
     * </pre>
     * @param parent the parent composite
     */
    private void createAllowFeatureEditGroup( Composite parent )
    {
        // Allow Feature Group
        Group allowFeatureGroup = BaseWidgetUtils.createGroup( parent, "", 2 );
        GridLayout allowFeatureGridLayout = new GridLayout( 2, false );
        allowFeatureGroup.setLayout( allowFeatureGridLayout );
        allowFeatureGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
        {
            AllowFeatureEnum allowFeature = AllowFeatureEnum.getFeature( i );
            Button button = BaseWidgetUtils.createCheckbox( allowFeatureGroup, allowFeature.getName(), 1 );
            allowFeatureCheckboxes[i] = button;
            allowFeatureCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }
    
    
    protected void initDialog()
    {
        List<AllowFeatureEnum> elements = getElements();
        
        for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
        {
            AllowFeatureEnum value = AllowFeatureEnum.getFeature( allowFeatureCheckboxes[i].getText() );
            
            // Disable the featrues already selected
            if ( elements.contains( value ) )
            {
                allowFeatureCheckboxes[i].setSelection( true );
                allowFeatureCheckboxes[i].setEnabled( false );
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( AllowFeatureEnum.UNKNOWN );
    }
}
