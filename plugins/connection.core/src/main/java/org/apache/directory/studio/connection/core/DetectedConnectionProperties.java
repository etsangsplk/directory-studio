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

package org.apache.directory.studio.connection.core;


import java.util.List;


/**
 * This class contains all the properties that were detected for a connection
 * during the first connection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DetectedConnectionProperties
{
    /** The key for the connection parameter "Vendor name" */
    private static final String CONNECTION_PARAMETER_VENDOR_NAME = "detectedProperties.vendorName";

    /** The key for the connection parameter "Vendor version" */
    private static final String CONNECTION_PARAMETER_VENDOR_VERSION = "detectedProperties.vendorVersion";

    /** The key for the connection parameter "Server type" */
    private static final String CONNECTION_PARAMETER_SERVER_TYPE = "detectedProperties.serverType";

    /** The key for the connection parameter "Supported LDAP versions" */
    private static final String CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS = "detectedProperties.supportedLdapVersions";

    /** The key for the connection parameter "Supported SASL mechanisms" */
    private static final String CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS = "detectedProperties.supportedSaslMechanisms";

    /** The key for the connection parameter "Supported controls" */
    private static final String CONNECTION_PARAMETER_SUPPORTED_CONTROLS = "detectedProperties.supportedControls";

    /** The key for the connection parameter "Supported extensions" */
    private static final String CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS = "detectedProperties.supportedExtensions";

    /** The key for the connection parameter "Supported features" */
    private static final String CONNECTION_PARAMETER_SUPPORTED_FEATURES = "detectedProperties.supportedFeatures";

    /** The connection */
    private Connection connection;


    /**
     * Creates a new instance of DetectedConnectionProperties.
     *
     * @param connection the associated connection
     */
    public DetectedConnectionProperties( Connection connection )
    {
        this.connection = connection;
    }


    public String getVendorName()
    {
        return connection.getConnectionParameter().getExtendedProperty( CONNECTION_PARAMETER_VENDOR_NAME );
    }


    public void setVendorName( String vendorName )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_VENDOR_NAME, vendorName );
    }


    public String getVendorVersion()
    {
        return connection.getConnectionParameter().getExtendedProperty( CONNECTION_PARAMETER_VENDOR_VERSION );
    }


    public void setVendorVersion( String vendorVersion )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_VENDOR_VERSION, vendorVersion );
    }


    public Object getServerType()
    {
        return connection.getConnectionParameter().getExtendedProperty( CONNECTION_PARAMETER_SERVER_TYPE );
    }


    public void setServerType( Object serverType )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_SERVER_TYPE,
            serverType.toString() );
    }


    public List<String> getSupportedLdapVersions()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS );
    }


    public void setSupportedLdapVersions( List<String> supportedLdapVersions )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS,
            supportedLdapVersions );
    }


    public List<String> getSupportedSaslMechanisms()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS );
    }


    public void setSupportedSaslMechanisms( List<String> supportedSaslMechanisms )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS,
            supportedSaslMechanisms );
    }


    public List<String> getSupportedControls()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_CONTROLS );
    }


    public void setSupportedControls( List<String> supportedControls )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_CONTROLS,
            supportedControls );
    }


    public List<String> getSupportedExtensions()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS );
    }


    public void setSupportedExtensions( List<String> supportedExtensions )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS,
            supportedExtensions );
    }


    public List<String> getSupportedFeatures()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_FEATURES );
    }


    public void setSupportedFeatures( List<String> supportedFeatures )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_FEATURES,
            supportedFeatures );
    }
}