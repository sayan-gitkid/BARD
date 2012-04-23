/*
 * File: app/store/assayStore.js
 * Date: Mon Apr 23 2012 15:20:11 GMT-0400 (EDT)
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

Ext.define('BARD.store.assayStore', {
    extend: 'Ext.data.Store',
    requires: [
        'BARD.model.Assay'
    ],

    constructor: function(cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            autoLoad: true,
            storeId: 'assayStore',
            model: 'BARD.model.Assay',
            autoCreated: 'false',
            proxy: {
                type: 'ajax',
                filterParam: 'assayId',
                url: 'api/assay',
                reader: {
                    type: 'json',
                    root: 'data',
                    totalProperty: 'count'
                }
            },
            fields: [
                {
                    name: 'id',
                    type: 'int'
                },
                {
                    name: 'assayName',
                    type: 'string'
                }
            ]
        }, cfg)]);
    }
});