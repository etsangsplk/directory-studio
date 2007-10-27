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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.internal.search.LdapSearchPageScoreComputer;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.RDN;
import org.apache.directory.studio.ldapbrowser.core.model.URL;
import org.apache.directory.studio.ldapbrowser.core.model.schema.AttributeTypeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.schema.ObjectClassDescription;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Subschema;
import org.eclipse.search.ui.ISearchPageScoreComputer;


/**
 * Base implementation of the {@link IEntry} interface.
 * 
 * The class is optimized to save memory. It doesn't hold members to 
 * its children or attributes. Instead the {@link ChildrenInfo} and 
 * {@link AttributeInfo} instances are stored in a map in the 
 * {@link BrowserConnection} instance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public abstract class AbstractEntry implements IEntry
{

    private static final int HAS_CHILDREN_HINT_FLAG = 1;

    private static final int IS_DIRECTORY_ENTRY_FLAG = 2;

    private static final int IS_ALIAS_FLAG = 4;

    private static final int IS_REFERRAL_FLAG = 8;

    private static final int IS_SUBENTRY_FLAG = 16;

    private volatile int flags;


    /**
     * Creates a new instance of AbstractEntry.
     */
    protected AbstractEntry()
    {
        this.flags = HAS_CHILDREN_HINT_FLAG;
    }


    /**
     * Sets the parent entry.
     * 
     * @param newParent the new parent entry
     */
    protected abstract void setParent( IEntry newParent );


    /**
     * Sets the RDN.
     * 
     * @param newRdn the new RDN
     */
    protected abstract void setRdn( RDN newRdn );


    /**
     * {@inheritDoc}
     */
    public void addChild( IEntry childToAdd )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci.childrenSet == null )
        {
            ci.childrenSet = new LinkedHashSet<IEntry>();
        }
        ci.childrenSet.add( childToAdd );
        entryModified( new EntryAddedEvent( childToAdd.getBrowserConnection(), childToAdd ) );
    }


    /**
     * {@inheritDoc}
     */
    public void deleteChild( IEntry childToDelete )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );

        if ( ci != null )
        {
            ci.childrenSet.remove( childToDelete );
            if ( ci.childrenSet == null || ci.childrenSet.isEmpty() )
            {
                getBrowserConnectionImpl().setChildrenInfo( this, null );
            }
            entryModified( new EntryDeletedEvent( getBrowserConnectionImpl(), childToDelete ) );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void addAttribute( IAttribute attributeToAdd ) throws IllegalArgumentException
    {
        String oidString = attributeToAdd.getAttributeDescription().toOidString( getBrowserConnection().getSchema() );
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null )
        {
            ai = new AttributeInfo();
            getBrowserConnectionImpl().setAttributeInfo( this, ai );
        }

        if ( !equals( attributeToAdd.getEntry() ) )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__attributes_entry_is_not_myself );
        }
        else if ( ai.attributeMap.containsKey( oidString.toLowerCase() ) )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_already_exists );
        }
        else
        {
            ai.attributeMap.put( oidString.toLowerCase(), attributeToAdd );
            entryModified( new AttributeAddedEvent( getBrowserConnectionImpl(), this, attributeToAdd ) );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void deleteAttribute( IAttribute attributeToDelete ) throws IllegalArgumentException
    {
        String oidString = attributeToDelete.getAttributeDescription().toOidString( getBrowserConnection().getSchema() );
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null && ai.attributeMap != null && ai.attributeMap.containsKey( oidString.toLowerCase() ) )
        {
            IAttribute attribute = ( IAttribute ) ai.attributeMap.get( oidString.toLowerCase() );
            ai.attributeMap.remove( oidString.toLowerCase() );
            if ( ai.attributeMap.isEmpty() )
            {
                getBrowserConnectionImpl().setAttributeInfo( this, null );
            }
            entryModified( new AttributeDeletedEvent( getBrowserConnectionImpl(), this, attribute ) );
        }
        else
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__attribute_does_not_exist );
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isConsistent()
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null || ai.attributeMap == null )
        {
            return isDirectoryEntry();
        }

        // check empty attributes and empty values
        Map<String, IAttribute> aiAttributeMap = new HashMap<String, IAttribute>( ai.attributeMap );
        for ( IAttribute attribute : aiAttributeMap.values() )
        {
            if ( !attribute.isConsistent() )
            {
                return false;
            }
        }

        if ( !isDirectoryEntry() )
        {
            // check objectclass attribute
            if ( !ai.attributeMap.containsKey( IAttribute.OBJECTCLASS_ATTRIBUTE_OID.toLowerCase() ) )
            {
                return false;
            }
            IAttribute ocAttribute = ( IAttribute ) ai.attributeMap.get( IAttribute.OBJECTCLASS_ATTRIBUTE_OID
                .toLowerCase() );
            String[] ocValues = ocAttribute.getStringValues();
            boolean structuralObjectClassAvailable = false;
            for ( int i = 0; i < ocValues.length; i++ )
            {
                ObjectClassDescription ocd = getBrowserConnection().getSchema().getObjectClassDescription( ocValues[i] );
                if ( ocd.isStructural() )
                {
                    structuralObjectClassAvailable = true;
                    break;
                }
            }
            if ( !structuralObjectClassAvailable )
            {
                return false;
            }

            // check must-attributes
            AttributeTypeDescription[] mustAtds = getSubschema().getMustAttributeTypeDescriptions();
            for ( AttributeTypeDescription mustAtd : mustAtds )
            {
                if ( !ai.attributeMap.containsKey( mustAtd.getNumericOID().toLowerCase() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isDirectoryEntry()
    {
        return ( flags & IS_DIRECTORY_ENTRY_FLAG ) != 0;
    }


    /**
     * {@inheritDoc}
     */
    public void setDirectoryEntry( boolean isDirectoryEntry )
    {
        if ( isDirectoryEntry )
        {
            flags = flags | IS_DIRECTORY_ENTRY_FLAG;
        }
        else
        {
            flags = flags & ~IS_DIRECTORY_ENTRY_FLAG;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isAlias()
    {
        if ( ( flags & IS_ALIAS_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return Arrays.asList( getSubschema().getObjectClassNames() ).contains( ObjectClassDescription.OC_ALIAS );
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    public void setAlias( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_ALIAS_FLAG;
        }
        else
        {
            flags = flags & ~IS_ALIAS_FLAG;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isReferral()
    {
        if ( ( flags & IS_REFERRAL_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return Arrays.asList( getSubschema().getObjectClassNames() ).contains( ObjectClassDescription.OC_REFERRAL );
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    public void setReferral( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_REFERRAL_FLAG;
        }
        else
        {
            flags = flags & ~IS_REFERRAL_FLAG;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isSubentry()
    {
        if ( ( flags & IS_SUBENTRY_FLAG ) != 0 )
        {
            return true;
        }

        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai != null )
        {
            return Arrays.asList( getSubschema().getObjectClassNames() ).contains( ObjectClassDescription.OC_SUBENTRY );
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    public void setSubentry( boolean b )
    {
        if ( b )
        {
            flags = flags | IS_SUBENTRY_FLAG;
        }
        else
        {
            flags = flags & ~IS_SUBENTRY_FLAG;
        }
    }


    /**
     * Triggers firering of the modification event.
     * 
     * @param event
     */
    private void entryModified( EntryModificationEvent event )
    {
        EventRegistry.fireEntryUpdated( event, this );
    }


    /**
     * {@inheritDoc}
     */
    public RDN getRdn()
    {
        return getDn().getRdn();
    }


    /**
     * {@inheritDoc}
     */
    public boolean isAttributesInitialized()
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        return ai != null && ai.attributesInitialized;
    }


    /**
     * {@inheritDoc}
     */
    public void setAttributesInitialized( boolean b )
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null && b )
        {
            ai = new AttributeInfo();
            getBrowserConnectionImpl().setAttributeInfo( this, ai );
        }

        if ( ai != null )
        {
            ai.attributesInitialized = b;
        }

        if ( ai != null && !b )
        {
            ai.attributeMap.clear();
            getBrowserConnectionImpl().setAttributeInfo( this, null );
        }

        entryModified( new AttributesInitializedEvent( this ) );
    }


    /**
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes()
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null || ai.attributeMap == null )
        {
            return null;
        }
        else
        {
            return ( IAttribute[] ) ai.attributeMap.values().toArray( new IAttribute[0] );
        }
    }


    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute( String attributeDescription )
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null || ai.attributeMap == null )
        {
            return null;
        }
        else
        {
            AttributeDescription ad = new AttributeDescription( attributeDescription );
            String oidString = ad.toOidString( getBrowserConnection().getSchema() );
            return ( IAttribute ) ai.attributeMap.get( oidString.toLowerCase() );
        }
    }


    /**
     * {@inheritDoc}
     */
    public AttributeHierarchy getAttributeWithSubtypes( String attributeDescription )
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null || ai.attributeMap == null )
        {
            return null;
        }
        else
        {
            List<IAttribute> attributeList = new ArrayList<IAttribute>();

            IAttribute myAttribute = getAttribute( attributeDescription );
            if ( myAttribute != null )
            {
                attributeList.add( myAttribute );
            }

            AttributeDescription ad = new AttributeDescription( attributeDescription );
            Map<String, IAttribute> clonedAttributeMap = new HashMap<String, IAttribute>( ai.attributeMap );
            for ( IAttribute attribute : clonedAttributeMap.values() )
            {
                AttributeDescription other = attribute.getAttributeDescription();
                if ( other.isSubtypeOf( ad, getBrowserConnection().getSchema() ) )
                {
                    attributeList.add( attribute );
                }
            }

            if ( attributeList.isEmpty() )
            {
                return null;
            }
            else
            {
                IAttribute[] attributes = attributeList.toArray( new IAttribute[attributeList.size()] );
                AttributeHierarchy ah = new AttributeHierarchy( this, attributeDescription, attributes );
                return ah;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public Subschema getSubschema()
    {
        AttributeInfo ai = getBrowserConnectionImpl().getAttributeInfo( this );
        if ( ai == null )
        {
            ai = new AttributeInfo();
            getBrowserConnectionImpl().setAttributeInfo( this, ai );
        }
        if ( ai.subschema == null )
        {
            ai.subschema = new Subschema( this );
        }

        return ai.subschema;
    }


    /**
     * {@inheritDoc}
     */
    public void setChildrenInitialized( boolean b )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null && b )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }

        if ( ci != null )
        {
            ci.childrenInitialized = b;
        }

        if ( ci != null && !b )
        {
            if ( ci.childrenSet != null )
            {
                ci.childrenSet.clear();
            }
            getBrowserConnectionImpl().setChildrenInfo( this, null );
        }

        entryModified( new ChildrenInitializedEvent( this ) );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isChildrenInitialized()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null && ci.childrenInitialized;
    }


    /**
     * {@inheritDoc}
     */
    public IEntry[] getChildren()
    {
        int count = getChildrenCount();
        if ( count < 0 )
        {
            return null;
        }
        else if ( count == 0 )
        {
            return new IEntry[0];
        }
        else
        {
            IEntry[] children = new IEntry[count];
            ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
            int i = 0;
            if ( ci.childrenSet != null )
            {
                for ( IEntry child : ci.childrenSet )
                {
                    children[i] = child;
                    i++;
                }
            }
            return children;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int getChildrenCount()
    {
        if ( isSubentry() )
        {
            return 0;
        }
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            return -1;
        }
        else
        {
            return ci.childrenSet == null ? 0 : ci.childrenSet.size();
        }
    }


    /**
     * {@inheritDoc}
     */
    public void setHasMoreChildren( boolean b )
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        if ( ci == null )
        {
            ci = new ChildrenInfo();
            getBrowserConnectionImpl().setChildrenInfo( this, ci );
        }
        ci.hasMoreChildren = b;

        entryModified( new ChildrenInitializedEvent( this ) );
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasMoreChildren()
    {
        ChildrenInfo ci = getBrowserConnectionImpl().getChildrenInfo( this );
        return ci != null && ci.hasMoreChildren;
    }


    /**
     * {@inheritDoc}
     */
    public void setHasChildrenHint( boolean b )
    {
        if ( b )
        {
            flags = flags | HAS_CHILDREN_HINT_FLAG;
        }
        else
        {
            flags = flags & ~HAS_CHILDREN_HINT_FLAG;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasChildren()
    {
        return ( flags & HAS_CHILDREN_HINT_FLAG ) != 0 || getChildrenCount() > 0;
    }


    /**
     * {@inheritDoc}
     */
    public String getChildrenFilter()
    {
        return getBrowserConnectionImpl().getChildrenFilter( this );
    }


    /**
     * {@inheritDoc}
     */
    public void setChildrenFilter( String childrenFilter )
    {
        getBrowserConnectionImpl().setChildrenFilter( this, childrenFilter );
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasParententry()
    {
        return getParententry() != null;
    }


    /**
     * Gets the browser connection implementation.
     * 
     * @return the browser connection implementation
     */
    private BrowserConnection getBrowserConnectionImpl()
    {
        return ( BrowserConnection ) getBrowserConnection();
    }


    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getDn().toString();
    }


    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        // check argument
        if ( o == null || !( o instanceof IEntry ) )
        {
            return false;
        }
        IEntry e = ( IEntry ) o;

        // compare dn and connection
        return getDn() == null ? e.getDn() == null : ( getDn().equals( e.getDn() ) && getBrowserConnection().equals(
            e.getBrowserConnection() ) );
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter )
    {
        Class<?> clazz = ( Class<?> ) adapter;
        if ( clazz.isAssignableFrom( ISearchPageScoreComputer.class ) )
        {
            return new LdapSearchPageScoreComputer();
        }
        if ( clazz.isAssignableFrom( Connection.class ) )
        {
            return getBrowserConnection().getConnection();
        }
        if ( clazz.isAssignableFrom( IBrowserConnection.class ) )
        {
            return getBrowserConnection();
        }
        if ( clazz.isAssignableFrom( IEntry.class ) )
        {
            return this;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public URL getUrl()
    {
        return new URL( getBrowserConnection(), getDn() );
    }

}
