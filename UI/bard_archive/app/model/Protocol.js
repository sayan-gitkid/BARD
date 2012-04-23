/*
 * File: app/model/Protocol.js
 * Date: Mon Apr 23 2012 10:21:59 GMT-0400 (Eastern Daylight Time)
 *
 * This file was generated by Sencha Architect version 2.0.0.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.0.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.0.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('BARD.model.Protocol', {
    extend: 'Ext.data.Model',

    idProperty: 'id',

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'protocolName',
            type: 'string'
        },
        {
            name: 'protocolDocument',
            type: 'string'
        },
        {
            name: 'modifiedBy',
            type: 'string'
        },
        {
            name: 'dateCreated',
            type: 'date'
        },
        {
            name: 'lastUpdated',
            type: 'date'
        }
    ],

    belongsTo: {
        model: 'BARD.model.Assay',
        foreignKey: 'protocols'
    }
});